package com.app.tubeapp.ui

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.app.tubeapp.R
import com.app.tubeapp.repository.UpdStatus
import com.app.tubeapp.viewmodels.HomeViewModel
import com.google.android.material.textfield.TextInputLayout
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.mapper.VideoFormat
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class HomeFragment : Fragment(), View.OnClickListener, LifecycleOwner {

    private val tagName: String = javaClass.simpleName
    private val homeViewModel: HomeViewModel by activityViewModels()
    private var currentUrl: String? = null
    private lateinit var btn144P: Button
    private lateinit var btn240P: Button
    private lateinit var btn360P: Button
    private lateinit var btn480P: Button
    private lateinit var btn720P: Button
    private lateinit var btn1080P: Button
    private lateinit var btn2K: Button
    private lateinit var btn4K: Button
    private lateinit var downloadPath: String
    private lateinit var videoView: VideoView
    private lateinit var relativeLocation: String
    private lateinit var formats: ArrayList<VideoFormat>
    private var data: VideoInfo? = null
    private lateinit var textInput: TextInputLayout

    // initialize widgets
    private fun initComponents(v: View) {
        btn144P = v.findViewById(R.id.btn144p)
        btn240P = v.findViewById(R.id.btn240p)
        btn360P = v.findViewById(R.id.btn360p)
        btn480P = v.findViewById(R.id.btn480P)
        btn720P = v.findViewById(R.id.btn720P)
        btn1080P = v.findViewById(R.id.btnHD)
        btn2K = v.findViewById(R.id.btnTwoK)
        btn4K = v.findViewById(R.id.btnFourK)
        videoView = v.findViewById(R.id.videoPreview)
        textInput = v.findViewById(R.id.textUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(tagName, "fragment $tagName onCreate() start")
        super.onCreate(savedInstanceState)
        // set options menu for fragment
        setHasOptionsMenu(true)
        lifecycle.addObserver(homeViewModel)

        // set live data
        // update url
        homeViewModel.setVideoGrabLink(activity?.intent?.extras?.getString(Intent.EXTRA_TEXT))
        downloadPath = activity?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath
        relativeLocation = Environment.DIRECTORY_MOVIES + File.separator + "Youtube-dl"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents(view)

        textInput.editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
              val x =  s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        // observe changes to live data
        homeViewModel.getVideoGrabLink()?.observe(viewLifecycleOwner, Observer {
            currentUrl = it
            if (!currentUrl.isNullOrEmpty()) {
                CoroutineScope(IO).launch {
                    data = homeViewModel.getMedia(currentUrl!!)
                    if (data != null) {
                        withContext(Dispatchers.Main) {
                            txtTitle.text = data!!.title
                            videoView.setVideoPath(homeViewModel.getPlayableUrl(data!!))
                            videoView.start()
                        }
                        // get available formats for the video
                        formats = homeViewModel.getFormats(data!!)
                    }
                }
            }
        })


        setListener(arrayOf(btn4K, btn2K, btn1080P, btn720P, btn360P, btn480P, btn240P, btn144P))
        btn144P.setOnClickListener {

            CoroutineScope(IO).launch {
                if (currentUrl == null || textUrl.editText?.text.toString().isEmpty()) return@launch

                val data = homeViewModel.getMedia(currentUrl!!)
                if (data != null) {


                } else {
                    Toast.makeText(requireContext(), "video not found ", Toast.LENGTH_SHORT).show()
                }


                val filename = homeViewModel.download(
                    data!!,
                    downloadPath,
                    "133+140", DownloadProgressCallback { p, e ->
                        Toast.makeText(requireContext(), "$p $e", Toast.LENGTH_LONG).show()
                    })

                try {
                    val resolver = requireContext().contentResolver

                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename!!)
                        //put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                        put(MediaStore.MediaColumns.IS_PENDING, 1)

                    }
                    val collection: Uri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val uri = resolver.insert(collection, contentValues)

                    if (uri != null) {
                        val os = resolver.openOutputStream(uri)
                        os?.write(
                            FileInputStream(
                                File(
                                    activity?.getExternalFilesDir(
                                        Environment.DIRECTORY_DOWNLOADS
                                    )!!.absolutePath, filename!!
                                )
                            ).readBytes()
                        )
                        os?.close()
                        contentValues.clear()
                        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }
                } catch (ex: Exception) {
                    log("Exception ", ex.message!!)
                }

            }
        }
    }

    private fun log(title: String, msg: String) {
        Log.d(title, msg)
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
                when (homeViewModel.update(activity?.application!!)) {
                    UpdStatus.FAILED -> Toast.makeText(requireContext(), "failed to update", Toast.LENGTH_SHORT).show()
                    UpdStatus.UPDATED -> Toast.makeText(requireContext(), "updated successfully", Toast.LENGTH_SHORT).show()
                    UpdStatus.NONE -> Toast.makeText(requireContext(), "already up to date", Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(tagName, "fragment $tagName onActivityCreated() start")
        super.onActivityCreated(savedInstanceState)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnFourK -> startDownload("format")
            R.id.btn360p -> startDownload("134+140")
            else -> {

            }
        }
    }

    private fun startDownload(format: String) {

    }

    private fun setListener(btns: Array<Button>) {
        for (btn in btns) {
            btn.setOnClickListener(this)
        }
    }
}

