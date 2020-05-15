package com.app.tubeapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.DownloadProgressCallback
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import java.io.File

private const val folderName = "youtube-dl"
private const val TAG = "MainActivity"
private const val STORAGE_REQUEST_CODE = 39
private const val PICK_MEDIA_DIRECTORY = 55

class MainActivity : AppCompatActivity(), DownloadProgressCallback {

    // folder where downloaded files will be saved

    private lateinit var youtubeDLDir: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val extras = intent.extras
        val contentUrl = extras?.getString(Intent.EXTRA_TEXT)

        if (!contentUrl.isNullOrEmpty()) {
            textUrl.setText(contentUrl)
        }

        intentPickDirectory()
        // initialize youtube-dl and ffmpeg
        YoutubeDL.getInstance().init(application)
        FFmpeg.getInstance().init(application);


        // download start button
        btnDownload.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {

                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Allow Permission to read storage").setMessage(
                        "We need access to storage in order to save" +
                                "downloaded files."
                    ).setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(
                                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                STORAGE_REQUEST_CODE
                            )
                        }
                    })
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            STORAGE_REQUEST_CODE
                        )
                    }
                }
            } else {
                youtubeDLDir =
                    File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!, folderName)
                Thread(Runnable {
                    startDownload()
                }).start()
            }


        }
        btnUpdate.setOnClickListener {
            CoroutineScope(IO).launch {
                update()
            }
        }


    }

    private fun intentPickDirectory() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
       // intent.addCategory(Intent.CATEGORY_OPENABLE)
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

    private fun readUri(uri : Uri?){
        Log.d(TAG, uri?.toString())

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                youtubeDLDir =
                    File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!, folderName)
                Toast.makeText(
                    this,
                    "Permission was granted starting the download",
                    Toast.LENGTH_SHORT

                ).show()
                // permission was granted, do something with it
                Thread(Runnable {
                    startDownload()
                }).start()
            } else {
                // permission was not granted, do nothing
                Toast.makeText(
                    this,
                    "No permission granted, the app will not work normally",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun update() {
        Log.d(TAG, "updating youtube-dl")
        YoutubeDL.getInstance().updateYoutubeDL(application)
    }

    private fun startDownload() {
        val dlRequest = YoutubeDLRequest(textUrl.text.toString())


        if (textUrl.text.toString().contains("vk.com")) {
            dlRequest.addOption("-u", "arshavin69ru@gmail.com")
            dlRequest.addOption("-p", "annanekdovA28")
        }

        if (textUrl.text.toString().contains("youtu")) {
            dlRequest.addOption("-f", "bestvideo+bestaudio")
        } else {
            dlRequest.addOption("-f", "best")
        }

        dlRequest.addOption("-o", youtubeDLDir?.absolutePath + "/%(title)s.%(ext)s")

        try {
            /*
            runOnUiThread {

                textInfo.text =
                    videoInfo.title + "\n" + videoInfo.description + "\n" + videoInfo.duration

             */
            Log.d(TAG, "starting download")
            YoutubeDL.getInstance().execute(dlRequest, this)
        } catch (e: YoutubeDLException) {
            Log.d(TAG, e.message.toString())
        }
    }

    override fun onProgressUpdate(progress: Float, etaInSeconds: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runOnUiThread {

                completed.text = "$progress% Done"
                progressBar.setProgress(progress.toInt(), true)

                val hour = etaInSeconds / 3600
                val min = (etaInSeconds % 3600) / 60
                val sec = etaInSeconds % 60

                val timeString =
                    String.format("%02d hr %02d min %02d sec remaining", hour, min, sec)

                etaText.text = timeString


            }
        }
    }
}


