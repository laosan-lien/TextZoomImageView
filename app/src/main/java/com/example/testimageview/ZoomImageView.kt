package com.example.testimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.opengl.Matrix
import android.util.AttributeSet
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.view.View.MeasureSpec


class ZoomImageView : androidx.appcompat.widget.AppCompatImageView {
    constructor(context: Context) : super(context){
        init()
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet){
        init()
    }
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ){
        init()
    }

    //    private lateinit var  matrix:Matrix
    private lateinit var viewSize: PointF
    private lateinit var imageSize: PointF

    private fun init() {
        scaleType = ScaleType.MATRIX
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        viewSize = PointF(width.toFloat(), height.toFloat())

        val drawable = drawable
        imageSize = PointF(
            drawable.minimumWidth.toFloat(),
            drawable.minimumHeight.toFloat()
        )
    }
//
//    private fun showCenter(){
//        val scaleX = viewSize.x / imageSize.x
//        val scaleY =
//    }




}
