package com.example.testimageview

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
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
            val defaultImageUri = "content://media/external/images/media/48"
            val displayId = PreviewUtils.checkDisplayId(this, 3)

            PreviewImageActivity.start(
                this,
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_IMAGE, imageUri).toString(),
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_IMAGE, defaultImageUri)
                    .toString(),
                displayId
            )
        }

        previewVideo.setOnClickListener {
            val videoUri = "content://media/external/video/media/36"
            val defaultVideoUri = "content://media/external/video/media/1084"
            val displayId = PreviewUtils.checkDisplayId(this, 3)

            PreviewVideoActivity.start(
                this,
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_VIDEO, videoUri).toString(),
                PreviewUtils.parsedUri(this, PreviewUtils.CHECK_TYPE_VIDEO, defaultVideoUri)
                    .toString(),
                displayId
            )
        }
    }


}

