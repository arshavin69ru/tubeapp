package com.app.tubeapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.app.tubeapp.R
import com.app.tubeapp.models.CustomVideoInfo
import com.app.tubeapp.viewmodels.SharedViewModel
import com.yausername.youtubedl_android.DownloadProgressCallback
import kotlinx.android.synthetic.main.video_download_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.io.File

class VideoDownloadFragment : Fragment() {

    private val tagName: String = javaClass.simpleName
    private lateinit var videoView: VideoView
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var urlLiveData: LiveData<String>
    private var customVideoInfo: CustomVideoInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tagName, "fragment $tagName onCreate() start")
        super.onCreate(savedInstanceState)
        // set options menu for fragment
        setHasOptionsMenu(true)
        lifecycle.addObserver(sharedViewModel)
        urlLiveData = sharedViewModel.getVideoGrabLink()!!

        // set live data
        sharedViewModel.setVideoGrabLink(activity?.intent?.extras?.getString(Intent.EXTRA_TEXT))
        Log.d(tagName, "fragment $tagName onCreate() end")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tagName, "fragment $tagName onCreateView() start")
        return inflater.inflate(R.layout.video_download_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(tagName, "fragment $tagName onViewCreated() start")
        super.onViewCreated(view, savedInstanceState)
        videoView = view.findViewById(R.id.videoPreview)
        sharedViewModel.initYoutubeDL()
        sharedViewModel.getVideoGrabLink()?.observe(viewLifecycleOwner, Observer {
            val job = CoroutineScope(IO).launch {
                customVideoInfo = sharedViewModel.getVideoInfo(it)
            }
            job.invokeOnCompletion {
                CoroutineScope(Dispatchers.Main).launch {
                    customVideoInfo?.videoInfo?.apply {
                        txtVidInfoTitle.text = title
                        txtVidInfoExtraOne.text = description
                        txtVidInfoExtraTwo.text = webpageUrl
                    }
                    customVideoInfo?.getPlayableUrl()?.apply {
                        videoView.setVideoURI(Uri.parse(this))
                        videoView.start()
                    }
                }
            }
            btnVidInfoDownload.setOnClickListener {
                val navController = view.findNavController()
                val list = ArrayList<String>()
                for (data in customVideoInfo?.videoInfo?.formats!!) {
                    list.add(data.format)
                }
                sharedViewModel.setVideoFormatsList(list)
                navController.navigate(R.id.downloadDialogFragment)
            }
        })


        sharedViewModel.getSelection()?.observe(viewLifecycleOwner, Observer {
            val path = File(activity?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "youtube-dl")
            Log.d("Format", sharedViewModel.getSelection()?.value!!)

            CoroutineScope(IO).launch {
                sharedViewModel.download(
                    urlLiveData.value,
                    sharedViewModel.getSelection()?.value!!,
                    path,
                    DownloadProgressCallback { progress, etaInSeconds ->
                        Log.d("progress", progress.toString())
                        Log.d("ETA", etaInSeconds.toString())
                        launch(Main) {
                            Toast.makeText(context, "Downloading progress $progress ETA: $etaInSeconds", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        })
    }

    override fun onPause() {
        super.onPause()
        Log.d(tagName, "onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tagName, "onDestroy()")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tagName, "onStop()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(tagName, "onViewDestroy()")
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(tagName, "onViewStateRestored()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(tagName, "onResume()")

    }

    override fun onDetach() {
        super.onDetach()
        Log.d(tagName, "onDetach()")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        Log.d(tagName, "fragment $tagName onCreateOptionsMenu() start")
        inflater.inflate(R.menu.activity_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(tagName, "fragment $tagName onOptionsItemSelected() start")
        if (item.itemId == R.id.setting) {
            Toast.makeText(activity, "settings", Toast.LENGTH_LONG).show()
        } else if (item.itemId == R.id.update) {
            Toast.makeText(activity, "updating", Toast.LENGTH_LONG).show()
            CoroutineScope(IO).launch {
                sharedViewModel.update(activity?.application!!)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(tagName, "fragment $tagName onActivityCreated() start")
        super.onActivityCreated(savedInstanceState)
    }
}

