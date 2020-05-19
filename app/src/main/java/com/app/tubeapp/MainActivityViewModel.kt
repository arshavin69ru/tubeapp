package com.app.tubeapp

import android.util.Log
import androidx.lifecycle.*
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityViewModel : ViewModel() , LifecycleObserver{

    private var videoInfo: MutableLiveData<VideoInfo>? = null

    suspend fun startDownload(url: String) {
        val dlRequest = YoutubeDLRequest(url)
        withContext(Dispatchers.IO) {
            YoutubeDL.getInstance().execute(dlRequest, DownloadProgressCallback { progress, etaInSeconds ->
                {

                }
            })
        }
    }

    fun getVideoInfo(url: String): MutableLiveData<VideoInfo>? {
        CoroutineScope(Dispatchers.Main).launch {
            createVideoInfo(url)
        }
        return videoInfo
    }

    private fun createVideoInfo(url: String) {
        val dlRequest = YoutubeDLRequest(url)
        var info : VideoInfo? = null
        if (videoInfo == null) {
            videoInfo = MutableLiveData()
        }

        viewModelScope.launch(IO) {
            Log.d("ViewModel", Thread.currentThread().name)
            info = YoutubeDL.getInstance().getInfo(dlRequest)
        }.invokeOnCompletion {
            Log.d("JOB", "FINISHED")
            videoInfo?.postValue(info)
        }
    }
}



