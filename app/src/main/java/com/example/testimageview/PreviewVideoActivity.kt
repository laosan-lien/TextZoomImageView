package com.example.testimageview

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_preview_image.*
import kotlinx.android.synthetic.main.activity_preview_video.*
import java.io.FileNotFoundException

private const val VIDEO_URI = "media_uri"
private const val DEFAULT_VIDEO_URI = "default_media_uri"
private const val TAG = "NativeUi:PreviewMedia"

private const val ERROR_URI = "error_uri"

class PreviewVideoActivity : AppCompatActivity() {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context, uri: String, defaultUri: String, displayId: Int) {
            val parsedUri =
                if (PreviewUtils.parsedUri(context, PreviewUtils.CHECK_TYPE_VIDEO, uri) != null)
                    PreviewUtils.parsedUri(context, PreviewUtils.CHECK_TYPE_VIDEO, uri)
                        .toString() else ERROR_URI
            val parsedDefaultUri = if (PreviewUtils.parsedUri(
                    context,
                    PreviewUtils.CHECK_TYPE_VIDEO,
                    defaultUri
                ) != null
            )
                PreviewUtils.parsedUri(context, PreviewUtils.CHECK_TYPE_VIDEO, defaultUri)
                    .toString() else ERROR_URI
            val intent = Intent(context, PreviewVideoActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            val options = ActivityOptions.makeBasic()
            options.launchDisplayId = displayId
            intent.putExtra(VIDEO_URI, parsedUri)
            intent.putExtra(DEFAULT_VIDEO_URI, parsedDefaultUri)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_video)

        val videoUri = Uri.parse(intent.getStringExtra(VIDEO_URI))
        val defaultVideoUri = Uri.parse(intent.getStringExtra(DEFAULT_VIDEO_URI))
        if (!loadVideo(videoUri) && !loadVideo(defaultVideoUri)) {
            image_view.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.no_resources_found
                )
            )
        }
    }

    /**
     * 获取媒体文件的三种方式：
    1、文件流(use)
    2、绝对路径
    3、文件描述符
     */
    private fun loadVideo(uri: Uri): Boolean {
        Log.d(TAG, "loadVideo: ${uri}")
        try {
            video_view.apply {
                setMediaController(MediaController(context))
                setVideoURI(uri)
                start()
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(this@PreviewVideoActivity, "未找到指定视频", Toast.LENGTH_SHORT).show()
                    return@setOnErrorListener true
                }
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "the video URI:${uri} is incorrect")
            return false
        } catch (e: SecurityException) {
            Toast.makeText(this, "未打开存储权限", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }
}