package com.app.tubeapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_download_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class VideoDownloadFragment : DialogFragment(), LifecycleOwner {

    companion object {
        fun newInstance() = DialogFragment()
    }

    private lateinit var viewModel: VideoDownloadViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_download_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //viewModel = ViewModelProviders.of(this).get(VideoDownloadViewModel::class.java)
        viewModel = VideoDownloadViewModel()
        lifecycle.addObserver(viewModel)
        Log.d("MainActivity", "starting task")
        var imageUrl: String? = null
        viewModel.getVideoInfo(downloadUrl, activity!!.application)?.observe(viewLifecycleOwner, Observer {
            txtVidTitle.text = it.title
            for(f in it.formats){
                txtDemo.text = f.format
            }

            var bitmap: Bitmap? = null
            imageUrl = it.thumbnail
            txtDemo1.text = it.getDuration().toString()
            Log.d("thumbnail", it.thumbnail)

            Glide.with(this).load(imageUrl).into(imgThumbnail)
        })

        downloadVideo.setOnClickListener {
            Log.d("downloading", "yes")
            if (!txtDemo.text.isNullOrEmpty()) {
                CoroutineScope(Dispatchers.Default).launch {
                    Log.d("Inside Download", "Downloading")
                    download()
                }.invokeOnCompletion {
                    Log.d("Download", "Done")
                    this.dismiss()
                }
            }
        }
    }

    private suspend fun download() {
        withContext(IO) {
            val request = YoutubeDLRequest(txtDemo.text.toString())

            val path =
                activity!!.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath + "/youtube-dl" + "/%(title)s.%(ext)s"
            request.addOption(
                "-o", path
            )
            Log.d("PATH", path)
            val out = YoutubeDL.getInstance().execute(request) { progress, _ ->
                Log.d("Progress", progress.toString())
            }.err
            Log.d("OUT", out)
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
                Log.d("BITMAP", e!!.message)
            }
        }
        return null
    }

}