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
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.app.tubeapp.models.MediaDownloadUtil
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.io.File

private const val folderName = "youtube-dl"
private const val STORAGE_REQUEST_CODE = 39
private const val PICK_MEDIA_DIRECTORY = 55
private const val SAVE_MEDIA = 58
private const val PERMISSION_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE
var downloadUrl: String? = null

class MainActivity : AppCompatActivity(), LifecycleOwner, VideoDownloadFragment.DownloadCallback {
    // monitor permission for storage access
    private var isDownloading = false
    private var permissionStatus = false
    private val TAG: String = this.javaClass.name

    private lateinit var youtubeDLDir: File
    private lateinit var selectedDir: Uri
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var activity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.mainToolbar))

        CoroutineScope(Dispatchers.Default).launch {
            askPermissionIfNotAvailable()
        }

        // add observers
        activityViewModel = MainActivityViewModel()
        lifecycle.addObserver(activityViewModel)

        CoroutineScope(Dispatchers.Default).launch {
            downloadUrl = if (catchVideoLink() != null) catchVideoLink() else ""
        }


        val fragment = VideoDownloadFragment()
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commit()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private suspend fun askPermissionIfNotAvailable() {
        activity = this
        withContext(Dispatchers.Main) {
            if (!checkPermission()) { // we have no permission enter here
                when {
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION_WRITE) -> { // if user denied perm
                        showCustomDialog(
                            getString(R.string.perm_storage_title),
                            getString(R.string.perm_storage_message)
                        ).setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            requestPermissions(arrayOf(PERMISSION_WRITE), STORAGE_REQUEST_CODE)
                        }.show()
                        permissionStatus = checkPermission()

                    }
                    else -> { // user didn't deny , just ask for permission
                        requestPermissions(arrayOf(PERMISSION_WRITE), STORAGE_REQUEST_CODE)
                        permissionStatus = checkPermission()
                    }
                }
            } else { // we already have permission
                permissionStatus = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.setting) {
            Toast.makeText(this, "hello", Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * check for permission on android M+
     */
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, PERMISSION_WRITE) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Grab media url from other applications, using share button
     * @return: media url
     */
    private suspend fun catchVideoLink(): String? {
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

    override fun startDownload() {

        val job = CoroutineScope(IO).launch {
            val request = YoutubeDLRequest(downloadUrl)
            val dir = "${activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath}/youtube-dl/%(title)s.%(ext)s"
            request.addOption("-o", dir)
            MediaDownloadUtil.downloadVideoWithRequest(DownloadProgressCallback { progress, eta ->
                isDownloading = true
                if (isActive)
                    CoroutineScope(Main).launch {
                        val hour = eta / 3600
                        val min = (eta % 3600) / 60
                        val sec = eta % 60
                        val timeString =
                            String.format("%02d hr %02d min %02d sec remaining", hour, min, sec)

//                        progressBar.progress = progress.toInt()
//                        completed.text = progress.toString()
//                        etaText.text = timeString
                    }
            }, request)
        }
    }
}


//    override fun onProgressUpdate(progress: Float, etaInSeconds: Long) {

//
//        val timeString =
//            String.format("%02d hr %02d min %02d sec remaining", hour, min, sec)
//
//        runOnUiThread {
//            completed.text = "$progress% Done"
//            progressBar.progress = progress.toInt()
//            etaText.text = timeString
//        }
//    }


