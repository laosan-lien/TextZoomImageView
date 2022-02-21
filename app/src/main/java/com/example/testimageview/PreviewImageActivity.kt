package com.example.testimageview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_preview_image.*
import java.io.FileNotFoundException

private const val MEDIA_URI = "media_uri"
private const val DEFAULT_MEDIA_URI = "default_media_uri"
private const val TAG = "NativeUi:PreviewMedia"
private const val IS_URI_LEGAL = "is_uri_legal"

private const val IMAGE_EXTERNAL_CONTENT_URI = "content://media/external/images/media"
private const val VIDEO_EXTERNAL_CONTENT_URI = "content://media/external/video/media"
private const val CHECK_TYPE_IMAGE = "image"
private const val CHECK_TYPE_VIDEO = "video"
private const val MEDIA_URI_SCHEME = "scheme"
private const val MEDIA_URI_AUTHORITY = "media"

class PreviewImageActivity : AppCompatActivity() {
    companion object {
        fun start(
            context: Context,
            imageUri: String,
            defaultImageUri: String,
            isUriLegal: Boolean
        ) {
            val intent = Intent(context, PreviewImageActivity::class.java)
            intent.putExtra(MEDIA_URI, imageUri)
            intent.putExtra(DEFAULT_MEDIA_URI, defaultImageUri)
            intent.putExtra(IS_URI_LEGAL, isUriLegal)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)

        val imageUri = intent.getStringExtra(MEDIA_URI)
        val defaultImageUri = intent.getStringExtra(DEFAULT_MEDIA_URI)
        if (imageUri != null && defaultImageUri != null) {
            previewImage(imageUri, defaultImageUri)
        } else {
            Log.d(TAG, "onCreate: uri error")
        }
    }


    /**
     * 获取媒体文件的三种方式：
    1、文件流(use)
    2、绝对路径
    3、文件描述符
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun previewImage(imageUri: String, defaultUri: String) {
        val parsedImageUri = Uri.parse(imageUri)
        val parsedDefaultUri = Uri.parse(defaultUri)
        if (!loadImage(parsedImageUri) && !loadImage(parsedDefaultUri)) {
            image_view.setImageDrawable(this.getDrawable(R.drawable.no_resources_found))
        }
    }

    private fun loadImage(uri: Uri): Boolean {
        if (!Utils.checkUri(CHECK_TYPE_IMAGE, uri.toString())) {
            return false
        }
        try {
            contentResolver.openInputStream(uri).use {
                image_view.setImageBitmap(BitmapFactory.decodeStream(it))
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "the image URI:${uri} is incorrect")
            return false
        }
        return true
    }
}