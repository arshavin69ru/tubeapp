package com.app.tubeapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.tubeapp.repository.MediaDownloader
import com.app.tubeapp.repository.UpdStatus
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo

class HomeViewModel(val app: Application) : AndroidViewModel(app), LifecycleObserver {
    private val videoGrabLink: MutableLiveData<String>? by lazy {
        MutableLiveData<String>()
    }
    private val mediaDownloader by lazy {
        MediaDownloader()
    }

    fun getVideoGrabLink(): LiveData<String>? {
        return videoGrabLink
    }

    fun setVideoGrabLink(str: String?) {
        videoGrabLink?.value = str
    }

    private suspend fun initYoutubeDL() {
        mediaDownloader.initYoutube(app)
    }

    suspend fun getFormats(info: VideoInfo): ArrayList<VideoFormat> {
        initYoutubeDL()
        return mediaDownloader.getDownloadFormats(info)
    }

    suspend fun getMedia(url: String): VideoInfo? {
        initYoutubeDL()
        return mediaDownloader.getMediaData(url)
    }

    suspend fun update(application: Application): UpdStatus {
        initYoutubeDL()
        return if (mediaDownloader.initYoutube(application)) mediaDownloader.update(application) else UpdStatus.FAILED
    }

    suspend fun getPlayableUrl(videoInfo: VideoInfo): String? {
        initYoutubeDL()
        return mediaDownloader.getStreamUrl(videoInfo)
    }

    suspend fun download(videoInfo: VideoInfo, path: String, format: String, callback: DownloadProgressCallback): String? {
        initYoutubeDL()
        return mediaDownloader.downloadMedia(videoInfo, path, format, callback)
    }
}