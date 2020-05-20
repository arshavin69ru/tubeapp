package com.app.tubeapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tubeapp.models.DownloadProgress
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

var downloadProgress: DownloadProgress? = null

class MainActivityViewModel : ViewModel(), LifecycleObserver {
    private val TAG: String = this.javaClass.name

    private var videoInfo: MutableLiveData<VideoInfo>? = null

    suspend fun download(path: String) {
        withContext(IO) {
            val request = YoutubeDLRequest(downloadUrl)
            val dir = "$path/youtube-dl/%(title)s.%(ext)s"
            request.addOption("-o", dir)
            val out = YoutubeDL.getInstance().execute(request) { progress, etaInSeconds ->
                downloadProgress = DownloadProgress(progress, etaInSeconds)
            }
            Log.d("Error", out.err)
        }
    }

    private fun getVideoInfo(url: String?, application: Application): MutableLiveData<VideoInfo>? {
        // if mutable data object is null initialize it
        if (videoInfo == null) {
            videoInfo = MutableLiveData<VideoInfo>()
        }
        if (url.isNullOrEmpty())
            return null
        // launch a background task on another thread to get video info
        viewModelScope.launch {
            grabVideoData(url)
        }
        return videoInfo
    }

    private suspend fun grabVideoData(urlString: String) {
        val dlRequest = YoutubeDLRequest(urlString)
        var info: VideoInfo? = null

        CoroutineScope(IO).launch {
            withContext(IO) {
                info = YoutubeDL.getInstance().getInfo(dlRequest)
            }
        }.invokeOnCompletion {
            videoInfo?.postValue(info)
        }
    }
}



