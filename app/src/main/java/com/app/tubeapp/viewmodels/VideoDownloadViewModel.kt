package com.app.tubeapp.viewmodels

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
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoDownloadViewModel(application: Application) : AndroidViewModel(application), LifecycleObserver {
    // TODO: Implement the ViewModel
    var downloadUrl: String? = null
    private val tagName: String = javaClass.name
    var mediaGrabLink : MutableLiveData<String>? = null
    var videoInfo: MutableLiveData<VideoInfo>? = null
    //private var mediaData: MutableLiveData<String>? = null

    /**
     * Checks for internet connectivity on the device
     * @return true if internet is available, else false
     */

//    private suspend fun getVideoData(viewModel: VideoDownloadViewModel, url: String) {
//        videoData = viewModel.getVideoInfo(url, requireActivity().application)
//    }


    /**
     * Check for internet connectivity
     * @return whether an active connection to internet is available
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

    fun getMediaLink(){
        if(mediaGrabLink == null) mediaGrabLink = MutableLiveData()

        if(downloadUrl != null) mediaGrabLink?.value = downloadUrl
    }

    /**
     * get instance of youtube-dl and ffmpeg, must be called before
     * doing using either objects, else it will throw null pointer exception.
     * @param application an instance of application
     */
    fun initialize(application: Application) {
        YoutubeDL.getInstance().init(application)
        FFmpeg.getInstance().init(application)
    }

    /**
     * update the youtube-dl binary
     * @param application
     */
    suspend fun update(application: Application) {
        YoutubeDL.getInstance().updateYoutubeDL(application)
    }

    fun getVideoInfo(url: String?, application: Application) {
        // if mutable data object is null initialize it
        if (url.isNullOrEmpty())
            return
        if (videoInfo == null) {
            videoInfo = MutableLiveData<VideoInfo>()
        }

        // launch a background task on another thread to get video info
        viewModelScope.launch(Dispatchers.IO) {
            videoInfo?.postValue(grabVideoData(url))
        }
    }

    /***
     * gets video streaming link which can be played
     * @param: info pass video info to object for extraction of streaming url
     * @return: playable url string
     */
    fun getVideoUrl(videoInfo: VideoInfo?): String? {
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

    private suspend fun grabVideoData(urlString: String): VideoInfo? {
        val dlRequest = YoutubeDLRequest(urlString)
        var info: VideoInfo? = null
        try {
            return YoutubeDL.getInstance().getInfo(dlRequest)
        } catch (ex: YoutubeDLException) {
            Log.d("Exception", ex.message!!)
        }
        return null
    }

    suspend fun download(application: Application) {

    }
}
