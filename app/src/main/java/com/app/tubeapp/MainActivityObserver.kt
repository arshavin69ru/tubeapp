package com.app.tubeapp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel(), LifecycleObserver {



    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public fun startWork(){

    }
}