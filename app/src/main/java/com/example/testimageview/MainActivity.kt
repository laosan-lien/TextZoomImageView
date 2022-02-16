package com.example.testimageview

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException

private const val TAG = "NativeUi:MainActivity"

class MainActivity : AppCompatActivity() {
    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

        println("**********************************")
        val imageUri = "://media/external/images/media"
        val defaultImageUri = "content://media/external/images/media"
        previewImage(imageUri, defaultImageUri)
//
        val videoUri = "conten://media/external/video/media"
        val defaultVideoUri = "content://media/external/video/media"
        previewVideo(videoUri, defaultVideoUri)

//

        //查找视频文件
        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                println(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)))
            }
        }
        println("**********************************")
    }

    /**
     * 获取媒体文件的三种方式：
    1、文件流(use)
    2、绝对路径
    3、文件描述符
     */
    private fun previewImage(imageUri: String, defaultUri: String) {
        try {
            contentResolver.openInputStream(Uri.parse(imageUri)).use {
                image_view.setImageBitmap(BitmapFactory.decodeStream(it))
            }
        } catch (e: FileNotFoundException) {

            Log.d(TAG, "the image URI is incorrect")
            contentResolver.openInputStream(Uri.parse(defaultUri)).use {
                image_view.setImageBitmap(BitmapFactory.decodeStream(it))
            }
        }
    }


    private fun previewVideo(videoUri: String, defaultUri: String) {
        video_view.setMediaController(MediaController(this))
        video_view.setVideoURI(Uri.parse(videoUri))
        video_view.start()
        video_view.requestFocus()
        video_view.setOnErrorListener { _, _, _ ->
            Toast.makeText(this@MainActivity, "出错了", Toast.LENGTH_SHORT).show()
            video_view.setVideoURI(Uri.parse(defaultUri))
            video_view.start()
            true
        }
    }
}

