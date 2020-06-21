package com.app.tubeapp.ui

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.app.tubeapp.R

private const val PERMISSION_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE


class MainActivity : AppCompatActivity(), LifecycleOwner {
    private val tag: String = this.javaClass.name
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

    }
}


