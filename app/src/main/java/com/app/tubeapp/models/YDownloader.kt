package com.app.tubeapp.models

import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class YDownloader {

    companion object {
        suspend fun downloadVideo(url: String) {
            withContext(IO) {
                val request = YoutubeDLRequest(url)
                YoutubeDL.getInstance().execute(request).out
            }
        }
    }

    enum class Options(val option: String) {
        FORMAT("-f"),
        OUTPUT("-o"),
        LIST_THUMBNAILS("--list-thumbnails"),
        GET_ID("--get-id"),
        GET_DESC("--get-description"),
        GET_DURATION("--get-duration"),
        GET_FILENAME("--get-filename"),
        GET_FORMAT("--get-format")
    }

    enum class FileNames(val title: String) {
        TITLE_EXT("%(title)s.%(ext)s"),
        TITLE_ID_EXT("%(title)s-%(id)s.%(ext)s")
    }
}