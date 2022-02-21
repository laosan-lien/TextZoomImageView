package com.example.testimageview

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "NativeUi:MainActivity"


class MainActivity : AppCompatActivity() {
    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewImage.setOnClickListener {
            val imageUri = "content://media/external/images/media/1085"
            val defaultImageUri = "content://media/external/images/media/48"
            PreviewMediaActivity.start(this, imageUri, defaultImageUri, true)
        }

        previewVideo.setOnClickListener {
            val videoUri = "content://media/external/video/media/36"
            val defaultVideoUri = "content://media/external/video/media/1084"
            PreviewMediaActivity.start(this, videoUri, defaultVideoUri, true)
        }
    }


}

