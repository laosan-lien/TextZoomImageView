package com.example.testimageview

import android.content.Context

class MediaPreviewerManager(context: Context) {

    private var context: Context? = context
    private val previewerMap = HashMap<Int, MediaPreviewerActivity>()

    fun preview(){

    }

    fun destroy() {
        context = null
        previewerMap.clear()
    }

}