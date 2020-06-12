package com.app.tubeapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.tubeapp.models.CustomVideoInfo
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import java.io.File

class SharedViewModel(val app: Application) : AndroidViewModel(app), LifecycleObserver {
    private val videoGrabLink: MutableLiveData<String>? by lazy {
        MutableLiveData<String>()
    }
    private val videoFormat: MutableLiveData<ArrayList<String>>? by lazy {
        MutableLiveData<ArrayList<String>>()
    }
    private val selection: MutableLiveData<String>? by lazy {
        MutableLiveData<String>()
    }

    fun getVideoGrabLink(): LiveData<String>? {
        return videoGrabLink
    }

    fun getSelection(): LiveData<String>? {
        return selection
    }

    fun setSelection(str: String) {
        selection?.value = str
    }

    fun setVideoGrabLink(str: String?) {
        videoGrabLink?.value = str
    }

    fun getVideoFormatList(): LiveData<ArrayList<String>>? {
        return videoFormat
    }

    fun setVideoFormatsList(list: ArrayList<String>?) {
        videoFormat?.value = list
    }

    // initialize youtube-dl
    fun initYoutubeDL() {
        YoutubeDL.getInstance().init(app)
        FFmpeg.getInstance().init(app)
    }

    /**
     * update the youtube-dl binary
     * @param application
     */
    suspend fun update(application: Application) {
        YoutubeDL.getInstance().updateYoutubeDL(application)
    }


    // get VideoInfo
    fun getVideoInfo(url: String?): CustomVideoInfo? {
        if (!url.isNullOrEmpty()) {
            val vidInfo = YoutubeDL.getInstance().getInfo(YoutubeDLRequest(url))
            return CustomVideoInfo(vidInfo)
        }
        return null
    }

    // for youtube only
    fun download(url: String?, format: String, path: File, progress: DownloadProgressCallback?) {

        val request = YoutubeDLRequest(url)
        request.addOption("-o", path.absolutePath + "/%(title)s.%(ext)s")
        when {
            format.contains("133") -> request.addOption("-f", "133+140")
            format.contains("134") -> request.addOption("-f", "134+1440")
            format.contains("135") -> request.addOption("-f", "135+140")
            format.contains("136") -> request.addOption("-f", "136+140")
            format.contains("137") -> request.addOption("-f", "137+140")
            format.contains("137") -> request.addOption("-f", "137+140")

            else -> request.addOption("-f", "bestvideo+bestaudio")
        }
        YoutubeDL.getInstance().execute(request, progress)
    }
}