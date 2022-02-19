package com.example.testimageview

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore

class Utils {
    companion object {
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