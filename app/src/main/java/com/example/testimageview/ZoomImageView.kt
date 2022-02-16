package com.example.testimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log


private const val TAG = "NativeUi:ZoomImageView"

class ZoomImageView : androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        init()
    }

    private lateinit var  mMatrix: Matrix
    private lateinit var viewSize: PointF
    private lateinit var imageSize: PointF

    //缩放后图片的大小
    private var scaleSize = PointF()

//    最初的宽高的缩放比例
    private lateinit var originScale :PointF


    private fun init() {
        scaleType = ScaleType.MATRIX
        mMatrix = Matrix()
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
    }

    /**
     * 将图片按照比例缩放，这里宽高缩放比例相等，所以PointF 里面的x,y是一样的
     * @param scaleXY
     */
    private fun scaleImage(scaleXY: PointF) {
        Log.d(TAG, "scaleImage: ")
        mMatrix.setScale(scaleXY.x , scaleXY.y)
        scaleSize.set(scaleXY.x * imageSize.x, scaleXY.y * imageSize.y)
        imageMatrix = mMatrix


    }



}