package com.app.tubeapp.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.app.tubeapp.R
import com.app.tubeapp.util.TubeApplication
import com.bumptech.glide.Glide
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.android.synthetic.main.video_download_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLConnection

class VideoDownloadFragment : Fragment(), LifecycleOwner {
    private var videoData: LiveData<VideoInfo>? = null
    private var progressFrame: FrameLayout? = null
    private var downloadCallback: DownloadCallback? = null
    private lateinit var videoView : VideoView
    private lateinit var urlData: String
    companion object {
        fun newInstance() = Fragment()
    }

    private lateinit var viewModel: VideoDownloadViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.video_download_fragment, container, false)
        progressFrame = view.findViewById(R.id.progressBarHolder)
        videoView = view.findViewById(R.id.imgVidThumbnail)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            downloadCallback = activity as DownloadCallback
        } catch (ce: ClassCastException) {
            Log.d("ClassCastException", "thrown")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = VideoDownloadViewModel(TubeApplication())

        lifecycle.addObserver(viewModel)

        Log.d("DOWNLOAD URL" , downloadUrl!!)

        if (!downloadUrl.isNullOrEmpty()) {
            progressFrame!!.visibility = View.VISIBLE
            CoroutineScope(IO).launch {

                getVideoData(viewModel)

                withContext(Main) {
                    videoData?.observe(viewLifecycleOwner, Observer {
                        txtVidInfoTitle.text = it.title
                        txtVidInfoExtraOne.text = it.description
                        txtVidInfoExtraTwo.text = it.webpageUrl
                    //    Glide.with(activity!!).load(it.thumbnail).into(imgVidThumbnail)
                        val vidUrl = viewModel.getVideoUrl(it)
                        if(vidUrl != null) videoView.setVideoURI(Uri.parse(vidUrl))
                        val controller = MediaController(activity)
                        videoView.setMediaController(controller)

                        videoView.start()
                        progressFrame!!.visibility = View.GONE
                    })

                    btnVidInfoDownload.setOnClickListener {
                        downloadCallback?.startDownload()
                    }
                }
            }
        }
    }

    private suspend fun getVideoData(viewModel: VideoDownloadViewModel) {
        videoData = viewModel.getVideoInfo(downloadUrl, activity!!.application)
    }

    private suspend fun getBitmap(urlString: String): Bitmap? {

        withContext(IO) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                val con: URLConnection = URL(urlString).openConnection() as HttpURLConnection
                con.doInput = true
                Log.d("Stream", con.toString())
                return@withContext BitmapFactory.decodeStream(con.getInputStream(), null, options)
            } catch (e: IOException) {
                Log.d("BITMAP", e.message!!)
            }
        }
        return null
    }

    interface DownloadCallback {
        fun startDownload()
    }
}