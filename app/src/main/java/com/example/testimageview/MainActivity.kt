package com.example.testimageview

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException

private const val TAG = "NativeUi:MainActivity"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)

        println("**********************************")
        val imageUri = "://media/external/images/media"
        val defaultImageUri = "content://media/external/images/media"
        previewImage(imageUri, defaultImageUri)

        val videoUri = "content://media/external/video/media"
        val defaultVideoUri = "content://media/external/video/media"
        previewVideo(videoUri ,defaultVideoUri)
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
        video_view.setVideoURI(Uri.parse(videoUri))
        try {
            contentResolver.openInputStream(Uri.parse(videoUri)).use {

            }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "the video URI is incorrect")

//            contentResolver.openInputStream(Uri.parse(defaultUri)).use {
//                video_view.set(BitmapFactory.decodeStream(it))
////            }
        }
    }

}