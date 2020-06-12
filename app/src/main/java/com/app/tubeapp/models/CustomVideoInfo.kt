package com.app.tubeapp.models

import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo

class CustomVideoInfo(val videoInfo: VideoInfo?) {

    fun getPlayableUrl(): String? {
        val formats = (if (videoInfo?.formats != null) videoInfo.formats else null) ?: return null
        for (fmt: VideoFormat in formats) {
            if (fmt.formatId != null && fmt.formatId == videoInfo!!.formatId) {
                return fmt.url
            }
        }
        // fallback to mp4
        // return first mp4 link
        for (fmt in formats) {
            if ("mp4" == fmt.ext)
                return fmt.url
        }
        return null
    }
}