package com.example.testimageview


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.hardware.display.DisplayManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

//import com.sample.mapp.uicomponent.DfsUrl

private const val TAG = "Native:PreviewUtils"
private const val HOME_DISPLAY_ID = 0

class PreviewUtils {

    companion object {
        private const val SCHEME_HTTP = "http"
        private const val SCHEME_HTTPS = "https"
        private const val SCHEME_CONTENT = "content"

        //        private const val SCHEME_DFS = "dfs"
        const val CHECK_TYPE_IMAGE = "image"
        const val CHECK_TYPE_VIDEO = "video"

        /**
         * 检查contentUri，url的格式
         */
        fun parsedUri(context: Context, checkType: String, uri: String): Uri? {
            val parsedUri = Uri.parse(uri)
            when (parsedUri.scheme?.lowercase()) {
                SCHEME_HTTP, SCHEME_HTTPS -> {
                    return parsedUri
                }
                SCHEME_CONTENT -> {
                    return if (checkContentUri(checkType, parsedUri)) parsedUri else null
                }
//                SCHEME_DFS -> {
//                    val parsedDfsUri = DfsUrl.parse(uri)
//                    if (parsedDfsUri.isValid) {
//                        parsedDfsUri.toLocalContentUri(context)
//                            ?.let { contentUri ->
//                                return if (checkContentUri(CHECK_TYPE_IMAGE, contentUri)) contentUri else null
//                            }
//                    } else {
//                        return null
//                    }
//                }
                else -> {
                    Log.d(TAG, "The scheme:${Uri.parse(uri)} of the URI is incorrect")
                }
            }
            return null
        }

        private fun checkContentUri(checkType: String, uri: Uri): Boolean {
            val uriWithoutID = removeUriId(uri)
            if (uriWithoutID == null) {
                return false
            } else {
                return when (checkType) {
                    CHECK_TYPE_IMAGE -> {
                        uriWithoutID.toString()
                            .equals(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString(), true)
                    }
                    CHECK_TYPE_VIDEO -> {
                        uriWithoutID.toString()
                            .equals(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString(), true)
                    }
                    else -> {
                        Log.d(TAG, "Do not support check this type")
                        false
                    }
                }
            }
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


        /**
         * @name checkDisplayId
         * @param context
         * @param displayId 需要检查的displayId
         */
        fun checkDisplayId(context: Context, displayId: Int): Int {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            if (displayId == 0) {
                return HOME_DISPLAY_ID
            } else {
                //DisplayManager.DISPLAY_CATEGORY_PRESENTATION:此类别可用于识别适合用作演示显示器的辅助显示器，例如外部或无线显示器。
                val presentationDisplays =
                    displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
                for (display in presentationDisplays) {
                    Log.d(TAG, "Available displayId :{${display.displayId}}")
                    if (displayId == display.displayId) {
                        return displayId
                    }
                }
                Log.d(
                    TAG,
                    "the specified displayId:{$displayId} does not exist, Default minimum available secondary " +
                            "displayId"
                )
                return HOME_DISPLAY_ID
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

}