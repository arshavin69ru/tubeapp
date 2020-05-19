package com.app.tubeapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoDownloadViewModel : ViewModel(), LifecycleObserver {
    // TODO: Implement the ViewModel

    private val TAG : String = javaClass.name
    private var videoInfo: MutableLiveData<VideoInfo>? = null

//    suspend fun startDownload(url: String) {
//        val dlRequest = YoutubeDLRequest(url)
//        withContext(Dispatchers.IO) {
//            YoutubeDL.getInstance().execute(dlRequest, DownloadProgressCallback { progress, etaInSeconds ->
//                {
//
//                }
//            })
//        }
//    }


    fun getVideoInfo(url: String?, application: Application): MutableLiveData<VideoInfo>? {
        // if mutable data object is null initialize it
        if (videoInfo == null) {
            videoInfo = MutableLiveData<VideoInfo>()
        }
        if(url.isNullOrEmpty())
            return null


        Log.d(TAG, "Before viewModelScope " + Thread.currentThread().name)
        // launch a background task on another thread to get video info
        viewModelScope.launch {
            // init youtube
            Log.d(TAG, "Inside viewModelScope " + Thread.currentThread().name)
            Log.d(TAG, "coroutine context " + this.coroutineContext)

            Log.d(TAG, "get yotube instance " + Thread.currentThread().name)
            YoutubeDL.getInstance().init(application)
            Log.d(TAG, "get ffmpeg instance " + Thread.currentThread().name)
            FFmpeg.getInstance().init(application)
            Log.d(TAG, "get Video info " + Thread.currentThread().name)
            grabVideoData(url)
        }
        Log.d(TAG, "outside viewModelScope " + Thread.currentThread().name)
        return videoInfo
    }

    private suspend fun grabVideoData(urlString: String) {

        Log.d(TAG, "grabVideoData " + Thread.currentThread().name)
        val dlRequest = YoutubeDLRequest(urlString)
        var info: VideoInfo? = null

        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "grabvideo coroutine context " + this.coroutineContext)
            withContext(Dispatchers.IO) {
                Log.d(TAG, "inside with context " + Thread.currentThread().name)
                info = YoutubeDL.getInstance().getInfo(dlRequest)
            }
        }.invokeOnCompletion {
            Log.d(TAG, "insideOnCompletion " + Thread.currentThread().name)
            videoInfo?.postValue(info)
        }
    }
}