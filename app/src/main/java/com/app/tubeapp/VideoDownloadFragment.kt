package com.app.tubeapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.android.synthetic.main.video_download_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class VideoDownloadFragment : DialogFragment(), LifecycleOwner {

    private var videoData: LiveData<VideoInfo>? = null
    private var progressFrame: FrameLayout? = null
    private var downloadCallback: DownloadCallback? = null

    companion object {
        fun newInstance() = DialogFragment()
    }

    private lateinit var viewModel: VideoDownloadViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.video_download_fragment, container, false)
        progressFrame = view.findViewById<FrameLayout>(R.id.progressBarHolder)
        return view
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            downloadCallback  = activity as DownloadCallback
        }catch(ce : ClassCastException){
            Log.d("ClassCastException", "thrown")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = VideoDownloadViewModel()

        lifecycle.addObserver(viewModel)

        progressFrame!!.visibility = View.VISIBLE
        btnVidInfoDownload.visibility = View.GONE
        CoroutineScope(IO).launch {

            getVideoData(viewModel)
        }.invokeOnCompletion {
            activity?.runOnUiThread {
                videoData?.observe(viewLifecycleOwner, Observer {
                    if (it == null)
                        return@Observer
                    txtVidInfoTitle.text = it.title
                    txtVidInfoExtraOne.text = it.format
                    txtVidInfoExtraTwo.text = it.fulltitle
                    Glide.with(this).load(it.thumbnail).into(imgVidThumbnail)
                    progressFrame!!.visibility = View.GONE
                    btnVidInfoDownload.visibility = View.VISIBLE
                })
            }
        }

        btnVidInfoDownload.setOnClickListener {
            downloadCallback?.startDownload()
            Log.d("downloading", "yes")
//            if (!txtVidInfoExtraOne.text.isNullOrEmpty()) {
//                CoroutineScope(Dispatchers.Default).launch {
//                    Log.d("Inside Download", "Downloading")
//                    download()
//                }.invokeOnCompletion {
//                    Log.d("Download", "Done")
//                    this.dismiss()
//                }
//            }
        }
    }

    private suspend fun getVideoData(viewModel: VideoDownloadViewModel) {
        withContext(IO) {
            videoData = viewModel.getVideoInfo(downloadUrl, activity!!.application)
        }
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