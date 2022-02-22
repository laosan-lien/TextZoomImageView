package com.example.testimageview

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_preview_image.*
import java.io.FileNotFoundException

private const val IMAGE_URI = "media_uri"
private const val DEFAULT_IMAGE_URI = "default_media_uri"
private const val TAG = "NativeUi:PreviewMedia"

private const val ERROR_URI = "error_uri"

class PreviewImageActivity : AppCompatActivity() {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context, uri: String, defaultUri: String, displayId: Int) {
            val parsedUri =
                if (PreviewUtils.parsedUri(context, PreviewUtils.CHECK_TYPE_IMAGE, uri) != null)
                    PreviewUtils.parsedUri(context, PreviewUtils.CHECK_TYPE_IMAGE, uri)
                        .toString() else ERROR_URI
            val parsedDefaultUri = if (PreviewUtils.parsedUri(
                    context,
                    PreviewUtils.CHECK_TYPE_IMAGE,
                    defaultUri
                ) != null
            )
                PreviewUtils.parsedUri(context, PreviewUtils.CHECK_TYPE_IMAGE, defaultUri)
                    .toString() else ERROR_URI
            val intent = Intent(context, PreviewImageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            val options = ActivityOptions.makeBasic()
            options.launchDisplayId = displayId
            intent.putExtra(IMAGE_URI, parsedUri)
            intent.putExtra(DEFAULT_IMAGE_URI, parsedDefaultUri)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview_image)

        val imageUri = Uri.parse(intent.getStringExtra(IMAGE_URI))
        val defaultImageUri = Uri.parse(intent.getStringExtra(DEFAULT_IMAGE_URI))
        if (!loadImage(imageUri) && !loadImage(defaultImageUri)) {
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
    private fun loadImage(uri: Uri): Boolean {
        try {
            contentResolver.openInputStream(uri)
                .use { image_view.setImageBitmap(BitmapFactory.decodeStream(it)) }
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "the image URI:${uri} is incorrect")
            return false
        } catch (e: SecurityException) {
            Toast.makeText(this, "未打开存储权限", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        return true
    }
}