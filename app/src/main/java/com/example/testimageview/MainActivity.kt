package com.example.testimageview

import android.annotation.SuppressLint
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException

private const val TAG = "NativeUi:MainActivity"
private const val IMAGE_EXTERNAL_CONTENT_URI = "content://media/external/images/media"
private const val VIDEO_EXTERNAL_CONTENT_URI = "content://media/external/video/media"
private const val CHECK_TYPE_IMAGE = "image"
private const val CHECK_TYPE_VIDEO = "video"
private const val MEDIA_URI_SCHEME = "scheme"
private const val MEDIA_URI_AUTHORITY = "media"
//private const val


class MainActivity : AppCompatActivity() {
    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        println("**********************************")
        val imageUri = "content://media/external/images/media/4"
        val defaultImageUri = "content://media/external/images/media/48"
        val parsedUri = Uri.parse(defaultImageUri)
        println(removeUriId(parsedUri))
        previewImage(imageUri, defaultImageUri)

        val videoUri = "content://media/external/video/media/36"
        val defaultVideoUri = "content://media/external/video/media/36"
        previewVideo(videoUri, defaultVideoUri)
        Utils.printMediaDetails(this, "image")
        Utils.printMediaDetails(this, "video")
        println("**********************************")
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
        if (!checkUri(CHECK_TYPE_IMAGE, uri)) {
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

    private fun checkUri(checkType: String, uri: Uri): Boolean {
        val uriWithoutID = removeUriId(uri)
        if (uriWithoutID == null) {
            return false
        } else {
            when (checkType) {
                CHECK_TYPE_IMAGE -> {
                    if (uriWithoutID.toString() != IMAGE_EXTERNAL_CONTENT_URI) {
                        return false
                    }
                }
                CHECK_TYPE_VIDEO -> {
                    if (uriWithoutID.toString() != VIDEO_EXTERNAL_CONTENT_URI) {
                        return false
                    }
                }
                else -> {
                    Log.d(TAG, "Do not support check this type")
                    return false
                }
            }
        }
        try {
        } catch (e: NumberFormatException) {
            Log.d(TAG, "the uri:${uri} is not legal")
            return false
        }
        return true
    }


    private fun removeUriId(uri: Uri): Uri? {
        try {
            if (uri.lastPathSegment == null) {
                return null
            } else {
                uri.lastPathSegment!!.toLong()
            }
            val segments = uri.pathSegments
            val builder = uri.buildUpon()
            builder.path(null)
            for (index in (0..segments.size - 2)) {
                builder.appendPath(segments[index])
            }
            return builder.build()
        } catch (e: NumberFormatException) {
            Log.d(TAG, "the uri:${uri} does not contain ID")
            return null
        }
    }

    //TODO:填上内容
    private fun checkHttpUrl(httpUrl: Uri): Boolean {
        return true
    }

    //TODO：通过fragment实现一个activity同时支持展示image和video
    private fun previewVideo(videoUri: String, defaultUri: String) {
        video_view.apply {
            setMediaController(MediaController(context))
            setVideoURI(Uri.parse(videoUri))
            start()
            setOnErrorListener { _, _, _ ->
                Toast.makeText(this@MainActivity, "未找到指定视频", Toast.LENGTH_SHORT).show()
                setVideoURI(Uri.parse(defaultUri))
                start()
                return@setOnErrorListener true
            }
        }
    }
}

