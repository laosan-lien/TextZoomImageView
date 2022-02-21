package com.example.testimageview

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_preview_image.*
import kotlinx.android.synthetic.main.activity_preview_video.*
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

class PreviewVideoActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_preview_video)

        val videoUri = intent.getStringExtra(MEDIA_URI)
        val defaultVideoUri = intent.getStringExtra(DEFAULT_MEDIA_URI)
        if (videoUri != null && defaultVideoUri != null) {
            previewVideo(videoUri, defaultVideoUri)
        }
        println("**********************************")
        image_view.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.no_resources_found
            )
        )

    }

    private fun previewVideo(videoUri: String, defaultUri: String) {

        video_view.apply {
            setMediaController(MediaController(context))
            setVideoURI(Uri.parse(videoUri))
            start()
            setOnErrorListener { _, _, _ ->
                Toast.makeText(this@PreviewVideoActivity, "未找到指定视频", Toast.LENGTH_SHORT).show()
                setVideoURI(Uri.parse(defaultUri))
                start()
                return@setOnErrorListener true
            }
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