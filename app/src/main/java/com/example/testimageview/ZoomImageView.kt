package com.example.testimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View



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
        ENLARGEMENT,
        NARROW
    }

    private lateinit var mMatrix: Matrix
    private lateinit var viewSize: PointF
    private lateinit var imageSize: PointF

    //缩放后图片的大小
    private var scaleSize = PointF()

    //    最初的宽高的缩放比例
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
    private val doubleClickZoom = 3

    //当前缩放的模式
    private var zoomInMode: Int = ZoomMode.ENLARGEMENT.ordinal

    //临时坐标比例数据
    private val tempPoint = PointF()

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
                            translationImage(
                                PointF(
                                    clickPoint.x - (bitmapOriginPoint.x + tempPoint.x * scaleSize.x),
                                    clickPoint.y - (bitmapOriginPoint.y + tempPoint.y * scaleSize.y)
                                )
                            )
                            zoomInMode = ZoomMode.NARROW.ordinal
                        } else {
                            //双击还原
                            showCenter()
                            zoomInMode = ZoomMode.ORDINARY.ordinal
                        }
                    } else {
                        lastClickTime = System.currentTimeMillis()
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                //屏幕上已经有一个手指按下，第二点按下时促发该事件
            }
            MotionEvent.ACTION_POINTER_UP -> {
                //屏幕上已经有两个点按住 再松开一个点时触发该事件

            }
            MotionEvent.ACTION_MOVE -> {
                //手指移动时触发事件

            }
            MotionEvent.ACTION_UP -> {
                //手指松开时触发事件

            }
            else -> {
                Log.d(TAG, "onTouch: can not match this action type")
            }
        }
        return true
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