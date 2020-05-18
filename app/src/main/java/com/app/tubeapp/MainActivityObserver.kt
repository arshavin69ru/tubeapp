package com.app.tubeapp

import android.webkit.URLUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.yausername.youtubedl_android.YoutubeDLRequest

class MainActivityViewModel : ViewModel(), LifecycleObserver {

    private val currentUrl: String? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun startWork() {

    }

//    public fun startDownload(url: String?): String {
//        if (!url.isNullOrEmpty() && URLUtil.isValidUrl(url)) {
//
//            val dlRequest = YoutubeDLRequest(url)
//
//            //dlRequest.addOption()
//        }
//    }
}