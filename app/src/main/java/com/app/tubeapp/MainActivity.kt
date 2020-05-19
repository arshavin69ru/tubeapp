package com.app.tubeapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

private const val folderName = "youtube-dl"
private const val TAG = "MainActivity"
private const val STORAGE_REQUEST_CODE = 39
private const val PICK_MEDIA_DIRECTORY = 55
private const val SAVE_MEDIA = 58
private const val PERMISSION_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE

class MainActivity : AppCompatActivity(), LifecycleOwner {
    // monitor permission for storage access
    private var permissionStatus = false

    // folder where downloaded files will be saved
    private var downloadUrl: String? = null
    private lateinit var youtubeDLDir: File
    private lateinit var selectedDir: Uri
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var activity: MainActivity
    private var videoInfo: LiveData<VideoInfo>? = null
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(mainToolbar)

        activityViewModel = MainActivityViewModel()
        lifecycle.addObserver(activityViewModel)

        // initialize youtube-dl and ffmpeg
        YoutubeDL.getInstance().init(application)
        FFmpeg.getInstance().init(application)


        // activityViewModel instance will observe lifecycle events of this lifecycle owner(MainActivity)


        // check if we have any shared video link

        downloadUrl = if (catchVideoLink() != null) catchVideoLink() else ""
        Log.d("MainActivity", downloadUrl!!)

        @RequiresApi(Build.VERSION_CODES.M)
        if (!checkPermission()) { // we have no permission enter here
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSION_WRITE)) { // if user denied perm
                showCustomDialog(
                    getString(R.string.perm_storage_title),
                    getString(R.string.perm_storage_message)
                ).setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    requestPermissions(kotlin.arrayOf(PERMISSION_WRITE), STORAGE_REQUEST_CODE)
                }.show()

            } else { // user didn't deny , just ask for permission
                requestPermissions(arrayOf(PERMISSION_WRITE), STORAGE_REQUEST_CODE)
            }
        } else { // we already have permission

            permissionStatus = true

            if (downloadUrl.isNullOrEmpty())
                return




            Log.d("Main", "has observers")
            activityViewModel.getVideoInfo(downloadUrl!!)?.observe(this, Observer {
                Log.d("setting text", it.title)
                etaText.text = it.title
            })
        }


        // download start button
        btnDownload.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                youtubeDLDir = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), folderName)
            } else {
                if (permissionStatus) {
                    youtubeDLDir = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), folderName)
                }
            }

//            val fragment = VkLoginFragment()
//            fragment.show(supportFragmentManager.beginTransaction(), "dialog")
//            var result  : String? = null

        }

        btnUpdate.setOnClickListener {
            CoroutineScope(IO).launch {
                update()
            }
        }
    }

    /**
     * check permission
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, PERMISSION_WRITE) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Grab media url from other applications, using share button
     * @return: media url
     */
    private fun catchVideoLink(): String? {
        val extras = intent.extras
        return extras?.getString(Intent.EXTRA_TEXT)
    }

    /**
     * show alert dialog for permission
     * and ask for permission if android version
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun showCustomDialog(title: String, message: String): AlertDialog.Builder {
        val alertDialog = AlertDialog.Builder(this)
        return alertDialog
            .setTitle(title)
            .setMessage(message)
    }

    /**
     * Pick a directory with system picker where we want to store the downloaded
     * media content.
     */
    private fun intentPickDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        // intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivityForResult(intent, PICK_MEDIA_DIRECTORY)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_DIRECTORY) {
            if (resultCode == Activity.RESULT_OK) {
                readUri(data?.data)
            }
        }
    }

    /**
     * Create a file in destination folder using storage access framework
     */
    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        startActivityForResult(intent, SAVE_MEDIA)
    }

    private fun readUri(uri: Uri?) {
        Log.d(TAG, uri.toString())
        selectedDir = uri!!

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE) {
            permissionStatus = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun update() {
        Log.d(TAG, "updating youtube-dl")
        YoutubeDL.getInstance().updateYoutubeDL(application)
    }

//    override fun onProgressUpdate(progress: Float, etaInSeconds: Long) {
//        val hour = etaInSeconds / 3600
//        val min = (etaInSeconds % 3600) / 60
//        val sec = etaInSeconds % 60
//
//        val timeString =
//            String.format("%02d hr %02d min %02d sec remaining", hour, min, sec)
//
//        runOnUiThread {
//            completed.text = "$progress% Done"
//            progressBar.progress = progress.toInt()
//            etaText.text = timeString
//        }
//
//    }
}


