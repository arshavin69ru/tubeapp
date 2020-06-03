package com.app.tubeapp.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class MainActivityViewModel : ViewModel(), LifecycleObserver {

    private val TAG: String = this.javaClass.name
    private var videoInfo: MutableLiveData<VideoInfo>? = null
    private var mediaUrl: MutableLiveData<String>? = null


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



