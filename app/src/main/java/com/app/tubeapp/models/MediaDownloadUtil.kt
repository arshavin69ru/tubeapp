package com.app.tubeapp.models

import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest

class MediaDownloadUtil {

    companion object {
        fun downloadVideoWithRequest(progressCallback: DownloadProgressCallback, request: YoutubeDLRequest): String {
            return YoutubeDL.getInstance().execute(request, progressCallback).out
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