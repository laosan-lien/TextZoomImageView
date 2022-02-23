package com.example.testimageview

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.UriMatcher
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.annotation.RequiresApi
import java.io.FileNotFoundException

private const val VIDEO_URI = "media_uri"
private const val DEFAULT_VIDEO_URI = "default_media_uri"
private const val TAG = "NativeUi:VideoPreviewer"

class VideoPreviewerActivity : MediaPreviewerActivity() {
    private lateinit var videoView: VideoView

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun start(
            context: Context, uri: String
            ?, defaultUri: String?, displayId: Int
        ) {
            val parsedUri = if (uri.isNullOrEmpty()) ERROR_URI else uri
            val parsedDefaultUri = defaultUri ?: ERROR_URI
            val intent = Intent(context, VideoPreviewerActivity::class.java)
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
        setContentView(R.layout.activity_video_previewer)
        videoView = findViewById(R.id.video_view)
        val uri = intent.getStringExtra(VIDEO_URI)
        val defaultUri = intent.getStringExtra(DEFAULT_VIDEO_URI)
        val validUri = if (isValidUri(this, uri!!)) uri else ERROR_URI
        val validDefaultUri = if (isValidUri(this, defaultUri!!)) defaultUri else ERROR_URI
        loadVideo(Uri.parse(validUri), Uri.parse(validDefaultUri))

    }

    private fun loadVideo(uri: Uri, defaultUri: Uri) {
        Log.d(TAG, "loadVideo: $uri")
        try {
            videoView.apply {
                setMediaController(MediaController(context))
                setVideoURI(uri)
                start()
                setOnErrorListener { _, _, _ ->
                    setVideoURI(defaultUri)
                    start()
                    return@setOnErrorListener true
                }
            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "the video URI:${uri} is incorrect")

        } catch (e: SecurityException) {
            Toast.makeText(this, "未打开存储权限", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun getMediaUriMatcher(): UriMatcher {
        val baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        UriMatcher(UriMatcher.NO_MATCH).also {
            it.addURI(baseUri.authority, "${baseUri.path}/#", VALID_URI)
            return it
        }
    }
}