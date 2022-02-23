package com.example.testimageview

import android.annotation.SuppressLint
import android.content.UriMatcher
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "NativeUi:MainActivity"


class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewImage.setOnClickListener {
            val imageUri = "content://media/external/images/media/1085"
            val defaultImageUri = "content://media/external/images/media/80"
            val displayId = PreviewUtils.checkDisplayId(this, 3)

            ImagePreviewerActivity.start(
                this,
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_IMAGE, imageUri).toString(),
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_IMAGE, defaultImageUri)
                    .toString(),
                displayId
            )
            PreviewUtils.printMediaDetails(this, "image")
        }

        previewVideo.setOnClickListener {
            val videoUri = "content://media/external/video/media"
            val defaultVideoUri = "content://media/external/video/media/"
            val displayId = PreviewUtils.checkDisplayId(this, 3)

            val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
            val baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            uriMatcher.addURI(baseUri.authority, "${baseUri.path}/#", 1)
            Log.d(TAG, "onCreate:${uriMatcher.match(Uri.parse(videoUri))}")
            Log.d(TAG, "onCreate:${uriMatcher.match(Uri.parse(defaultVideoUri))}")


            VideoPreviewerActivity.start(
                this,
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_VIDEO, videoUri).toString(),
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_VIDEO, defaultVideoUri)
                    .toString(),
                displayId
            )
            PreviewUtils.printMediaDetails(this, "video")
        }
    }


}

