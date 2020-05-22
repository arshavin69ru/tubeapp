package com.app.tubeapp

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import com.app.tubeapp.util.TubeApplication
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoDownloadViewModel(application: Application) : AndroidViewModel(application), LifecycleObserver {
    // TODO: Implement the ViewModel

    private val TAG: String = javaClass.name

    private var videoInfo: MutableLiveData<VideoInfo>? = null

    /**
     * Checks for internet connectivity on the device
     * @return true if internet is available, else false
     */
    private fun hasInternetConnection(): Boolean {
        val connectivity = getApplication<TubeApplication>().getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivity.activeNetwork ?: return false
            val capabilities = connectivity.getNetworkCapabilities(activeNetwork) ?: return false

            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            connectivity.activeNetworkInfo.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
    }

    fun getVideoInfo(url: String?, application: Application): LiveData<VideoInfo>? {
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
                } catch (ex: YoutubeDLException) {
                    Log.d("Exception", ex.message!!)
                }
            }
        }.invokeOnCompletion {
            videoInfo?.postValue(info)
        }
    }
}