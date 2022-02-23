package com.example.testimageview

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.UriMatcher
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.io.FileNotFoundException

private const val IMAGE_URI = "media_uri"
private const val DEFAULT_IMAGE_URI = "default_media_uri"
private const val TAG = "NativeUi:PreviewMedia"

class ImagePreviewerActivity : MediaPreviewerActivity() {
    private lateinit var imageView: ImageView

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun start(context: Context, uri: String?, defaultUri: String?, displayId: Int) {
            val parsedUri = if (uri.isNullOrEmpty()) ERROR_URI else uri
            val parsedDefaultUri = defaultUri ?: ERROR_URI
            val intent = Intent(context, ImagePreviewerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            val options = ActivityOptions.makeBasic()
            options.launchDisplayId = displayId
            intent.putExtra(MEDIA_URI, parsedUri)
            intent.putExtra(DEFAULT_IMAGE_URI, parsedDefaultUri)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_previewer)
        imageView = findViewById(R.id.image_view)
        val uri = intent.getStringExtra(MEDIA_URI)
        val defaultUri = intent.getStringExtra(DEFAULT_IMAGE_URI)
        val validUri = if (isValidUri(this, uri!!)) uri else ERROR_URI
        val validDefaultUri = if (isValidUri(this, defaultUri!!)) defaultUri else ERROR_URI
        if (!loadImage(Uri.parse(validUri)) && !loadImage(Uri.parse(validDefaultUri))) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_resources_found))
        }
    }

    private fun loadImage(uri: Uri): Boolean {
        try {
            contentResolver.openInputStream(uri)
                .use { imageView.setImageBitmap(BitmapFactory.decodeStream(it)) }
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

    override fun getMediaUriMatcher(): UriMatcher {
        val baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        UriMatcher(UriMatcher.NO_MATCH).also {
            it.addURI(baseUri.authority, "${baseUri.path}/#", VALID_URI)
            return it
        }
    }
}