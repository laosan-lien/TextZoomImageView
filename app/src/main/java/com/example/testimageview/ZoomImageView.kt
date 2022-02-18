package com.example.testimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt


private const val TAG = "NativeUi:ZoomImageView"

class ZoomImageView : androidx.appcompat.widget.AppCompatImageView, View.OnTouchListener {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr) {
        init()
    }

    enum class ZoomMode {
        ORDINARY,
        DOUBLE_CLICK_ZOOM,
        DOUBLE_FINGER_ZOOM
    }

    private lateinit var mMatrix: Matrix

    //ImageView的大小
    private lateinit var viewSize: PointF

    //图片的大小
    private lateinit var imageSize: PointF

    //缩放后图片的大小
    private var scaleSize = PointF()

    //最初的宽高的缩放比例
    private var originScale = PointF()

    //imageview中bitmap的xy实时坐标
    private val bitmapOriginPoint = PointF()

    //点击的点
    private val clickPoint = PointF()

    //设置的双击检查时间间隔
    private val doubleClickTimeSpan: Long = 250L

    //上次点击的时间
    private var lastClickTime: Long = 0

    //双击放大的倍数
    private val doubleClickZoom = 2

    //当前缩放的模式
    private var zoomInMode: Int = ZoomMode.DOUBLE_CLICK_ZOOM.ordinal

    //临时坐标比例数据
    private val tempPoint = PointF()

    //最大缩放比例
    private val maxScroll = 2F

    //两点之间的距离
    private var doublePointInstance = 1F

    //双指缩放的时候的中心点
    private var doublePointCenter = PointF()

    //双指缩放的比例
    private var doubleFingerScroll = 0F

    //上次触碰的手指数量
    private var lastFingerNum = 0

    private fun init() {
        scaleType = ScaleType.MATRIX
        mMatrix = Matrix()
        setOnTouchListener(this)
    }

    //TODO：更改
    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d(TAG, "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        viewSize = PointF(width.toFloat(), height.toFloat())
        imageSize = PointF(drawable.minimumWidth.toFloat(), drawable.minimumHeight.toFloat())
        showCenter()
    }

    /**
     * 设置图片居中等比显示
     */
    private fun showCenter() {
        Log.d(TAG, "showCenter")
        val scaleX = viewSize.x / imageSize.x
        val scaleY = viewSize.y / imageSize.y
        val scale = if (scaleX < scaleY) scaleX else scaleY
        scaleImage(PointF(scale, scale))
        //移动图片，并保存最初的图片左上角（即原点）所在坐标
        if (scaleX < scaleY) {
            translationImage(PointF(0f, viewSize.y / 2 - scaleSize.y / 2))
            bitmapOriginPoint.x = 0f
            bitmapOriginPoint.y = viewSize.y / 2 - scaleSize.y / 2
        } else {
            translationImage(PointF(viewSize.x / 2 - scaleSize.x / 2, 0f))
            bitmapOriginPoint.x = viewSize.x / 2 - scaleSize.x / 2
            bitmapOriginPoint.y = 0f
        }
        //保存下最初的缩放比例
        originScale.set(scale, scale)
        doubleFingerScroll = scale
    }

    /**
     * 将图片按照比例缩放，这里宽高缩放比例相等，所以PointF 里面的x,y是一样的
     * 通过matrix来控制图片显示的位置和缩放比例
     * @param scaleXY
     */
    private fun scaleImage(scaleXY: PointF) {
        Log.d(TAG, "scaleImage: ")
        mMatrix.setScale(scaleXY.x, scaleXY.y)
        scaleSize.set(scaleXY.x * imageSize.x, scaleXY.y * imageSize.y)
        imageMatrix = mMatrix
    }

    private fun translationImage(pointF: PointF) {
        mMatrix.postTranslate(pointF.x, pointF.y)
        imageMatrix = mMatrix
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouch1:")
        when (event?.action?.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                //TODO:设置图片范围内点击有效，缩放动画效果，贼丑
                //手指按下事件
                //记录被点击的点的坐标
                clickPoint.set(event.x, event.y)
                //判断屏幕上此时被按住的点的个数，当前屏幕只有一个点被点击的时候触发
                if (event.pointerCount == 1) {
                    Log.d(TAG, "onTouch2:")
                    //设置一个点击的间隔时长，来判断是不是双击
                    if (System.currentTimeMillis() - lastClickTime <= doubleClickTimeSpan) {
                        //如果图片此时缩放模式是普通模式，就触发双击放大
                        if (zoomInMode == ZoomMode.ORDINARY.ordinal) {
                            //分别记录被点击的点到图片左上角x,y轴的距离与图片x,y轴边长的比例，方便在进行缩放后，算出这个点对应的坐标点
                            tempPoint.set(
                                (clickPoint.x - bitmapOriginPoint.x) / scaleSize.x,
                                (clickPoint.y - bitmapOriginPoint.y) / scaleSize.y
                            )
                            //进行缩放
                            scaleImage(
                                PointF(
                                    originScale.x * doubleClickZoom,
                                    originScale.y * doubleClickZoom
                                )
                            )
                            //获取缩放后，图片左上角的xy坐标
                            getBitmapOffset()
                            //平移图片，使得被点击的点的位置不变。这里是计算缩放后被点击的xy坐标，与原始点击的位置的xy标值，计算出差值，然后做平移动作
                            println(
                                "2clickPoint.x = ${clickPoint.x} - (bitmapOriginPoint.x = ${bitmapOriginPoint.x}" +
                                        " tempPoint.x =${tempPoint.x} *scaleSize.x${scaleSize.x}"
                            )
                            println(
                                "clickPoint.y = ${clickPoint.y} - (bitmapOriginPoint.y = ${bitmapOriginPoint.y}" +
                                        " tempPoint.y =${tempPoint.y} *scaleSize.y =${scaleSize.y}"
                            )

                            translationImage(
                                PointF(
                                    clickPoint.x - (bitmapOriginPoint.x + tempPoint.x * scaleSize.x),
                                    clickPoint.y - (bitmapOriginPoint.y + tempPoint.y * scaleSize.y)
                                )
                            )
                            zoomInMode = ZoomMode.DOUBLE_FINGER_ZOOM.ordinal
                            doubleFingerScroll = originScale.x * doubleClickZoom
                        } else {
                            //双击还原
                            showCenter()
                            zoomInMode = ZoomMode.ORDINARY.ordinal
                            doubleFingerScroll = originScale.x
                        }
                        //双击放大后记录缩放比例
                    } else {
                        lastClickTime = System.currentTimeMillis()
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //屏幕上已经有一个手指按下，第二点按下时促发该事件
                //计算最初的两个手指之间的距离
                doublePointInstance = getDoublePointInstance(event)
                Log.d(TAG, "onTouch: doublePointInstance:${doublePointInstance}")
            }
            MotionEvent.ACTION_POINTER_UP -> {
                //屏幕上已经有两个点按住 再松开一个点时触发该事件
                //当有一个手指离开屏幕后，就修改状态，这样如果双击屏幕就能恢复到初始大小
                zoomInMode = ZoomMode.DOUBLE_CLICK_ZOOM.ordinal
                //记录此时双指缩放的比例
                doubleFingerScroll = scaleSize.x / imageSize.x
                lastFingerNum = 1
                //判断缩放后的比例，如果小于最初的那个比例，就恢复到最初的大小
                if (scaleSize.x < viewSize.x && scaleSize.y < viewSize.y) {
                    zoomInMode = ZoomMode.ORDINARY.ordinal
                    showCenter()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                //手指移动时触发事件
                //移动
                if (zoomInMode != ZoomMode.ORDINARY.ordinal) {
                    //如果是多指，计算中心点为假设的点击的点
                    var currentX = 0f
                    var currentY = 0f
                    //获取此时屏幕上被触碰的点有多少个
                    val pointCount = event.pointerCount
                    //计算出中间点所在的坐标
                    for (i in 0 until pointCount) {
                        currentX += event.getX(i)
                        currentY += event.getY(i)
                    }
                    currentX /= pointCount.toFloat()
                    currentY /= pointCount.toFloat()
                    //当屏幕被触碰的点的数量变化时，将最新算出来的中心点看作是被点击的点
                    if (lastFingerNum != event.pointerCount) {
                        clickPoint.x = currentX
                        clickPoint.y = currentY
                        lastFingerNum = event.pointerCount
                    }
                    //将移动手指时，实时计算出来的中心点坐标，减去被点击点的坐标就得到了需要移动的距离
                    val moveX = currentX - clickPoint.x
                    val moveY = currentY - clickPoint.y
                    //计算边界，使得不能已出边界，但是如果是双指缩放时移动，因为存在缩放效果，
                    //所以此时的边界判断无效
                    val moveFloat: FloatArray = moveBorderDistance(moveX, moveY)
                    //处理移动图片的事件
                    translationImage(PointF(moveFloat[0], moveFloat[1]))
                    clickPoint.set(currentX, currentY)
                }
                //缩放
                //判断当前是两个手指触摸到屏幕才处理缩放事件
                if (event.pointerCount == 2) {
                    //如果此时缩放后的大小，大于等于了设置的最大缩放的大小，就不处理
                    if (!((scaleSize.x / imageSize.x >= originScale.x * maxScroll ||
                                scaleSize.y / imageSize.y >= originScale.y * maxScroll)
                                && getDoublePointInstance(event) - doublePointInstance > 0
                                )
                    ) {
                        //这里设置当双指缩放的的距离变化量大于50，并且当前不是在双指缩放状态下，
                        //就计算中心点，等一些操作
                        Log.d(TAG, "onTouch: 3")
                        if (abs(getDoublePointInstance(event) - doublePointInstance) > 50
                            && zoomInMode != ZoomMode.DOUBLE_FINGER_ZOOM.ordinal
                        ) {
                            Log.d(TAG, "onTouch: 4")
                            //计算两个手指之间的中心点，当作放大的中心点
                            doublePointCenter.set(
                                (event.getX(0) + event.getX(1)) / 2,
                                (event.getY(0) + event.getY(1)) / 2
                            )
                            //将双指的中心假设为点击的点
                            clickPoint.set(doublePointCenter)
                            //下面就和双击放大基本一样
                            getBitmapOffset()
                            //分别记录被点击的点到图片左上角x，y之间的距离与图片x，y轴边长的比例
                            //方便在缩放后算出这个点对应的坐标点
                            tempPoint.set(
                                (clickPoint.x - bitmapOriginPoint.x) / scaleSize.x,
                                (clickPoint.y - bitmapOriginPoint.y) / scaleSize.y
                            )
                            //设置进入双指放大状态
                            zoomInMode = ZoomMode.DOUBLE_FINGER_ZOOM.ordinal
                        }
                        //如果已经进入双指放大状态，就直接计算缩放的比例，并进行位移
                        if (zoomInMode == ZoomMode.DOUBLE_FINGER_ZOOM.ordinal) {
                            Log.d(TAG, "onTouch: 5")
                            //当前的缩放的比例与当前双指之间的缩放比例相乘，就得到图片的应该缩放的比例
                            val scroll =
                                doubleFingerScroll * getDoublePointInstance(event) / doublePointInstance
                            //这里和双击放大是一样的
                            scaleImage(PointF(scroll, scroll))
                            getBitmapOffset()
                            println(
                                "2clickPoint.x = ${clickPoint.x} - (bitmapOriginPoint.x = ${bitmapOriginPoint.x}" +
                                        " tempPoint.x =${tempPoint.x} *scaleSize.x${scaleSize.x}"
                            )
                            println(
                                "clickPoint.y = ${clickPoint.y} - (bitmapOriginPoint.y = ${bitmapOriginPoint.y}" +
                                        " tempPoint.y =${tempPoint.y} *scaleSize.y =${scaleSize.y}"
                            )
                            translationImage(
                                PointF(
                                    clickPoint.x - (bitmapOriginPoint.x + tempPoint.x * scaleSize.x),
                                    clickPoint.y - (bitmapOriginPoint.y + tempPoint.y * scaleSize.y)
                                )
                            )
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                //手指松开时触发事件
                lastFingerNum = 0

            }
            else -> {
                Log.d(TAG, "onTouch: can not match this action type")
            }
        }
        return true
    }

    private fun moveBorderDistance(moveX: Float, moveY: Float): FloatArray {
        var moveDistanceX = moveX
        var moveDistanceY = moveY
        //计算bitmap的左上角坐标
        getBitmapOffset()
        //计算bitmap的右下角坐标
        val bitmapRightBottomX = bitmapOriginPoint.x + scaleSize.x
        val bitmapRightBottomY = bitmapOriginPoint.y + scaleSize.y

        if (moveDistanceY > 0) {
            //向下滑
            if (bitmapOriginPoint.y + moveDistanceY > 0) {
                moveDistanceY = if (bitmapOriginPoint.y < 0) {
                    -bitmapOriginPoint.y
                } else {
                    0f
                }
            }
        } else if (moveDistanceY < 0) {
            //向上滑
            if (bitmapRightBottomY + moveDistanceY < viewSize.y) {
                moveDistanceY = if (bitmapRightBottomY > viewSize.y) {
                    -(bitmapRightBottomY - viewSize.y)
                } else {
                    0f
                }
            }
        }

        if (moveDistanceX > 0) {
            //向右滑
            if (bitmapOriginPoint.x + moveDistanceX > 0) {
                moveDistanceX = if (bitmapOriginPoint.x < 0) {
                    -bitmapOriginPoint.x
                } else {
                    0f
                }
            }
        } else if (moveDistanceX < 0) {
            //向左滑
            if (bitmapRightBottomX + moveDistanceX < viewSize.x) {
                moveDistanceX = if (bitmapRightBottomX > viewSize.x) {
                    -(bitmapRightBottomX - viewSize.x)
                } else {
                    0f
                }
            }
        }
        return floatArrayOf(moveDistanceX, moveDistanceY)
    }

    /**
     * 计算两个手指之间的距离
     */
    private fun getDoublePointInstance(event: MotionEvent): Float {
        val distanceX = event.getX(0) - event.getX(1)
        val distanceY = event.getY(0) - event.getY(1)
        return sqrt((distanceX * distanceX + distanceY * distanceY).toDouble()).toFloat()
    }

    /**
     * 获取view中bitmap的坐标点
     */
    private fun getBitmapOffset() {
        Log.d(TAG, "getBitmapOffset: ")
        val value = FloatArray(9)
        val offset = FloatArray(2)
        imageMatrix.getValues(value)
        offset[0] = value[2]
        offset[1] = value[5]
        bitmapOriginPoint.set(offset[0], offset[1])
    }
}