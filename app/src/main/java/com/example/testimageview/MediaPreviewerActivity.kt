package com.example.testimageview

import android.content.Context
import android.content.UriMatcher
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val SCHEME_HTTP = "http"
private const val SCHEME_HTTPS = "https"
private const val SCHEME_CONTENT = "content"
private const val SCHEME_DFS = "dfs"
const val ERROR_URI = "error_uri"
const val MEDIA_URI = "error_uri"
private const val TAG = "NativeUi:MediaPreviewer"
const val VALID_URI = 1

abstract class MediaPreviewerActivity : AppCompatActivity() {

    protected fun isValidUri(context: Context, uri: String): Boolean {
        val parsedUri = Uri.parse(uri)
        when (parsedUri.scheme?.lowercase()) {
            SCHEME_HTTP, SCHEME_HTTPS -> {
                return true
            }
            SCHEME_CONTENT -> {
                return isValidContentUri(parsedUri)
            }
//            SCHEME_DFS -> {
//                val parsedDfsUri = DfsUrl.parse(uri)
//                if (parsedDfsUri.isValid) {
//                    parsedDfsUri.toLocalContentUri(context)
//                        ?.let { contentUri ->
//                            return isValidContentUri(contentUri)
//                        }
//                } else {
//                    return false
//                }
//            }
            else -> {
                Log.d(TAG, "The scheme:${Uri.parse(uri)} of the URI is incorrect")
                return false
            }
        }
        return false
    }

    private fun isValidContentUri(uri: Uri): Boolean {
        return getMediaUriMatcher().match(uri) == VALID_URI
    }

    protected abstract fun getMediaUriMatcher(): UriMatcher
}