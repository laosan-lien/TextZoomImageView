package com.example.testimageview

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

private const val TAG = "Loasan:Utils"

private const val IMAGE_EXTERNAL_CONTENT_URI = "content://media/external/images/media"
private const val VIDEO_EXTERNAL_CONTENT_URI = "content://media/external/video/media"
private const val CHECK_TYPE_IMAGE = "image"
private const val CHECK_TYPE_VIDEO = "video"
private const val MEDIA_URI_SCHEME = "scheme"
private const val MEDIA_URI_AUTHORITY = "media"

class Utils {
    companion object {

        /**
         * checkUri
         * @param type: String ：检查的uri类型， image or video
         * @param uri : String ：将要被检查的uri
         */
        fun checkUri(type: String, uri: String): Boolean {
            var isValid = false
            if (uri.isNotEmpty()) {
                when (Uri.parse(uri).scheme?.lowercase()) {
                    "http", "https" -> {
                        isValid = checkHttpUrl(uri)
                    }
                    "content" -> {
                        isValid = checkContentUri(type, uri)
                    }
                    else -> {
                        Log.d(TAG, "The scheme:${Uri.parse(uri).scheme} of the URI is incorrect")
                    }
                }
            }
            return isValid
        }

        private fun checkContentUri(checkType: String, uri: String): Boolean {
            val uriWithoutID = removeUriId(Uri.parse(uri))
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
        private fun checkHttpUrl(httpUrl: String): Boolean {
            if (httpUrl.isNotEmpty()) {
                return Uri.parse(httpUrl).scheme.equals("http") || Uri.parse(httpUrl).scheme.equals(
                    "https"
                )
            }
            return true
        }
    }

    /**
     * 打印设备中的媒体文件
     */
    @SuppressLint("Recycle")
    fun printMediaDetails(context: Context, uriType: String) {
        if (uriType.isNotEmpty()) {
            var cursor: Cursor? = null
            when (uriType) {
                "image" -> {
                    cursor = context.contentResolver.query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null
                    )
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val id =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                            val parsedUri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
                            )
                            println("parsedUri:${parsedUri}")
                        }
                    }
                }
                "video" -> {
                    cursor = context.contentResolver.query(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null, null, null, null
                    )
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            val id =
                                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                            val parsedUri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id
                            )
                            println("parsedUri:${parsedUri}")
                        }
                    }
                }
            }
        }
    }
}