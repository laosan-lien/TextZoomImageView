package zoomimageview;

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateInterpolator
import android.widget.OverScroller
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.roundToInt


class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AppCompatImageView(context, attrs, defStyleAttr), OnGlobalLayoutListener {
    private var mIsOneLoad = true

    //初始化的比例,也就是最小比例
    private var mInitScale = 0f

    //图片最大比例
    private var mMaxScale = 0f

    //双击能达到的最大比例
    private var mMidScale = 0f
    private val mScaleMatrix: Matrix

    //捕获用户多点触控
    private val mScaleGestureDetector: ScaleGestureDetector

    //移动
    private val gestureDetector: GestureDetector

    //双击
    private var isEnlarge = false //是否放大
    private var mAnimator: ValueAnimator? = null//双击缩放动画

    //滚动
    private val scroller: OverScroller
    private var mCurrentX = 0
    private var mCurrentY = 0

    private var translationAnimation: ValueAnimator? = null    //惯性移动动画
    private var onClickListener: OnClickListener? = null    //单击

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    /**
     * imageView加载完成后调用，获取imageView加载完成后的图片大小
     */
    override fun onGlobalLayout() {
        if (mIsOneLoad) {
            //得到控件的宽和高
            val width = width
            val height = height
            //获取图片,如果没有图片则直接退出
            val d: Drawable = drawable ?: return
            //获取图片的宽和高
            val imageWidth = d.intrinsicWidth
            val imageHeight = d.intrinsicHeight
            var scale = 1.0f
            if (imageWidth > width && imageHeight <= height) {
                scale = width * 1.0f / imageWidth
            }
            if (imageWidth <= width && imageHeight > height) {
                scale = height * 1.0f / imageHeight
            }
            if (imageWidth <= width && imageHeight <= height || imageWidth >= width && imageHeight >= height) {
                scale = Math.min(width * 1.0f / imageWidth, height * 1.0f / imageHeight)
            }
            //图片原始比例，图片回复原始大小时使用
            mInitScale = scale
            //图片双击后放大的比例
            mMidScale = mInitScale * 2
            //手势放大时最大比例
            mMaxScale = mInitScale * 4
            //设置移动数据,把改变比例后的图片移到中心点
            val translationX = width * 1.0f / 2 - imageWidth / 2
            val translationY = height * 1.0f / 2 - imageHeight / 2
            mScaleMatrix.postTranslate(translationX, translationY)
            mScaleMatrix.postScale(mInitScale, mInitScale, width * 1.0f / 2, height * 1.0f / 2)
            imageMatrix = mScaleMatrix
            mIsOneLoad = false
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mScaleGestureDetector.onTouchEvent(event) or
                gestureDetector.onTouchEvent(event)
    }

    //手势操作（缩放）
    fun scale(detector: ScaleGestureDetector) {
        val drawable: Drawable = getDrawable() ?: return
        val scale = scale
        //获取手势操作的值,scaleFactor>1说明放大，<1则说明缩小
        val scaleFactor = detector.scaleFactor
        //获取手势操作后的比例，当放操作后比例在[mInitScale,mMaxScale]区间时允许放大
        mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
        imageMatrix = mScaleMatrix
        removeBorderAndTranslationCenter()
    }

    //手势操作结束
    fun scaleEnd(detector: ScaleGestureDetector) {
        var scale = scale
        scale *= detector.scaleFactor
        if (scale < mInitScale) {
            scaleAnimation(
                mInitScale,
                (width / 2).toFloat(),
                (height / 2).toFloat()
            )
        } else if (scale > mMaxScale) {
            scaleAnimation(
                mMaxScale,
                (width / 2).toFloat(),
                (height / 2).toFloat()
            )
        }
    }

    //手势操作（移动）
    private fun onTranslationImage(distanceX: Float, distanceY: Float) {
        var transLateDistanceX = distanceX
        var translateDistanceY = distanceY
        if (drawable == null) return
        val rect = matrixRectF

        //图片宽度小于控件宽度时不允许左右移动
        if (rect!!.width() <= width) transLateDistanceX = 0.0f
        //图片高度小于控件宽度时，不允许上下移动
        if (rect.height() <= height) translateDistanceY = 0.0f

        //移动距离等于0，那就不需要移动了
        if (transLateDistanceX == 0.0f && translateDistanceY == 0.0f) return
        mScaleMatrix.postTranslate(transLateDistanceX, translateDistanceY)
        imageMatrix = mScaleMatrix
        //去除移动边界
        removeBorderAndTranslationCenter()
    }

    //消除控件边界和把图片移动到中间
    private fun removeBorderAndTranslationCenter() {
        val rectF = matrixRectF ?: return
        val width = getWidth()
        val height = getHeight()
        val widthF = rectF.width()
        val heightF = rectF.height()
        val left = rectF.left
        val right = rectF.right
        val top = rectF.top
        val bottom = rectF.bottom
        var translationX = 0.0f
        var translationY = 0.0f
        if (left > 0) {
            //左边有边界
            translationX = if (widthF > width) {
                //图片宽度大于控件宽度，移动到左边贴边
                -left
            } else {
                //图片宽度小于控件宽度，移动到中间
                width * 1.0f / 2f - (widthF * 1.0f / 2f + left)
            }
        } else if (right < width) {
            //右边有边界
            translationX = if (widthF > width) {
                //图片宽度大于控件宽度，移动到右边贴边
                width - right
            } else {
                //图片宽度小于控件宽度，移动到中间
                width * 1.0f / 2f - (widthF * 1.0f / 2f + left)
            }
        }
        if (top > 0) {
            //顶部有边界
            translationY = if (heightF > height) {
                //图片高度大于控件高度，去除顶部边界
                -top
            } else {
                //图片高度小于控件宽度，移动到中间
                height * 1.0f / 2f - (top + heightF * 1.0f / 2f)
            }
        } else if (bottom < height) {
            //底部有边界
            translationY = if (heightF > height) {
                //图片高度大于控件高度，去除顶部边界
                height - bottom
            } else {
                //图片高度小于控件宽度，移动到中间
                height * 1.0f / 2f - (top + heightF * 1.0f / 2f)
            }
        }
        mScaleMatrix.postTranslate(translationX, translationY)
        imageMatrix = mScaleMatrix
    }

    /**
     * 双击改变大小
     *
     * @param x 点击的中心点
     * @param y 点击的中心点
     */
    private fun onDoubleDropScale(x: Float, y: Float) {
        //如果缩放动画已经在执行，那就不执行任何事件
        if (mAnimator != null && mAnimator!!.isRunning) return
        val dropScale = doubleDropScale
        //执行动画缩放，不然太难看了
        scaleAnimation(dropScale, x, y)
    }

    /**
     * 缩放动画
     *
     * @param dropScale 缩放的比例
     * @param x         中心点
     * @param y         中心点
     */
    private fun scaleAnimation(dropScale: Float, x: Float, y: Float) {
        if (mAnimator != null && mAnimator!!.isRunning) return
        mAnimator = ObjectAnimator.ofFloat(scale, dropScale).also {
            it.duration = 300
            it.interpolator = AccelerateInterpolator()
            it.addUpdateListener(AnimatorUpdateListener { animation ->
                val value = animation.animatedValue as Float / scale
                mScaleMatrix.postScale(value, value, x, y)
                imageMatrix = mScaleMatrix
                removeBorderAndTranslationCenter()
            })
            it.start()
        }
    }

    /**
     *缩小//放大//如果等于mMidScale，则判断放大或者缩小
     *判断是放大或者缩小，如果上次是放大，则继续放大，缩小则继续缩小
     *当前大小不等于mMidScale,则调整到mMidScale
     *返回双击后改变的大小比例(我们希望缩放误差在deviation范围内)
     */
    private val doubleDropScale: Float
        private get() {
            val deviation = 0.05f
            var dropScale = 1.0f
            var scale = scale
            if (abs(mInitScale - scale) < deviation) scale = mInitScale
            if (abs(mMidScale - scale) < deviation) scale = mMidScale
            if (abs(mMaxScale - scale) < deviation) scale = mMaxScale
            if (scale != mMidScale) {
                //当前大小不等于mMidScale,则调整到mMidScale
                dropScale = mMidScale
                isEnlarge = scale < mMidScale
            } else {
                //如果等于mMidScale，则判断放大或者缩小
                //判断是放大或者缩小，如果上次是放大，则继续放大，缩小则继续缩小
                dropScale = if (isEnlarge) {
                    //放大
                    mMaxScale
                } else {
                    //缩小
                    mInitScale
                }
            }
            return dropScale
        }

    //获取图片宽高以及左右上下边界
    private val matrixRectF: RectF?
        private get() {
            val drawable: Drawable = getDrawable() ?: return null
            val rectF =
                RectF(0F, 0F, drawable.minimumWidth.toFloat(), drawable.minimumHeight.toFloat())
            val matrix: Matrix = imageMatrix
            matrix.mapRect(rectF)
            return rectF
        }

    /**
     * 获取当前图片的缩放值
     *
     * @return
     */
    private val scale: Float
        private get() {
            val values = FloatArray(9)
            mScaleMatrix.getValues(values)
            return values[Matrix.MSCALE_X]
        }

    /**
     * 解决和父控件滑动冲突 只要图片边界超过控件边界，返回true
     *
     * @param direction
     * @return true 禁止父控件滑动
     */
    override fun canScrollHorizontally(direction: Int): Boolean {
        val rect = matrixRectF
        if (rect == null || rect.isEmpty) return false
        return if (direction > 0) {
            rect.right >= width + 1
        } else {
            rect.left <= 0 - 1
        }
    }

    /**
     * 同楼上
     *
     * @param direction
     * @return
     */
    override fun canScrollVertically(direction: Int): Boolean {
        val rect = matrixRectF
        if (rect == null || rect.isEmpty) return false
        return if (direction > 0) {
            rect.bottom >= height + 1
        } else {
            rect.top <= 0 - 1
        }
    }

    init {
        //记住，一定要把ScaleType设置成ScaleType.MATRIX，否则无法缩放
        scaleType = ScaleType.MATRIX
        scroller = OverScroller(context)
        mScaleMatrix = Matrix()
        //手势缩放
        mScaleGestureDetector =
            ScaleGestureDetector(context, object : SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scale(detector)
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    scaleEnd(detector)
                }
            })

        //滑动和双击监听
        gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                //滑动监听
                onTranslationImage(-distanceX, -distanceY)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                //双击监听
                onDoubleDropScale(e.x, e.y)
                return true
            }

            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {

                //滑动惯性处理
                mCurrentX = e2.x.toInt()
                mCurrentY = e2.y.toInt()
                val rectF = matrixRectF ?: return false
                //startX为当前图片左边界的x坐标
                val startX = mCurrentX
                val startY = mCurrentY
                val minX = 0
                var maxX = 0
                val minY = 0
                var maxY = 0
                val vX = velocityX.roundToInt()
                val vY = velocityY.roundToInt()
                maxX = rectF.width().roundToInt()
                maxY = rectF.height().roundToInt()
                if (startX != maxX || startY != maxY) {
                    /**
                     *调用fling方法，然后我们可以通过调用getCurX和getCurY来获得当前的x和y坐标
                     *这个坐标的计算是模拟一个惯性滑动来计算出来的，我们根据这个x和y的变化可以模拟
                     *出图片的惯性滑动
                     */
                    scroller.fling(startX, startY, vX, vY, 0, maxX, 0, maxY, maxX, maxY)
                }
                if (translationAnimation != null && translationAnimation!!.isStarted) translationAnimation!!.end()
                translationAnimation = ObjectAnimator.ofFloat(0f, 1f).also {

                    it.duration = 500
                    it.addUpdateListener(AnimatorUpdateListener {
                        if (scroller.computeScrollOffset()) {
                            //获得当前的x坐标
                            val newX = scroller.currX
                            val dx = newX - mCurrentX
                            mCurrentX = newX
                            //获得当前的y坐标
                            val newY = scroller.currY
                            val dy = newY - mCurrentY
                            mCurrentY = newY
                            //进行平移操作
                            if (dx != 0 && dy != 0) onTranslationImage(dx.toFloat(), dy.toFloat())
                        }
                    })
                    it.start()
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                //单击事件
                onClickListener?.onClick(this@ZoomImageView)
                return true
            }
        })
    }
}