package com.app.tubeapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoDownloadViewModel : ViewModel(), LifecycleObserver {
    // TODO: Implement the ViewModel

    private val TAG: String = javaClass.name
    private var videoInfo: MutableLiveData<VideoInfo>? = null

    fun getVideoInfo(url: String?, application: Application): MutableLiveData<VideoInfo>? {
        // if mutable data object is null initialize it
        if (videoInfo == null) {
            videoInfo = MutableLiveData<VideoInfo>()
        }

        if (url.isNullOrEmpty())
            return null
        // launch a background task on another thread to get video info
        viewModelScope.launch {
            // init youtube
            YoutubeDL.getInstance().init(application)
            FFmpeg.getInstance().init(application)
            grabVideoData(url)
        }
        return videoInfo
    }

    private suspend fun grabVideoData(urlString: String) {
        val dlRequest = YoutubeDLRequest(urlString)
        var info: VideoInfo? = null

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                try {
                    info = YoutubeDL.getInstance().getInfo(dlRequest)
                }catch (ex: YoutubeDLException){
                    Log.d("Exception", ex.message!!)
                }
            }
        }.invokeOnCompletion {
            videoInfo?.postValue(info)
        }
    }
}