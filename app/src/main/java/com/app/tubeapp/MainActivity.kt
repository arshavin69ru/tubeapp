package com.app.tubeapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity(), DownloadProgressCallback {

    // folder where downloaded files will be saved

    private val youtubeDLDir =
        File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), folderName)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val extras = intent.extras
        val contentUrl = extras?.getString(Intent.EXTRA_TEXT)

        if (!contentUrl.isNullOrEmpty()) {
            textUrl.setText(contentUrl)
        }

        // initialize youtube-dl and ffmpeg
        YoutubeDL.getInstance().init(application)
        FFmpeg.getInstance().init(application);

        // download start button
        btnDownload.setOnClickListener {

            Thread(Runnable {
                startDownload()
            }).start()
        }
        btnUpdate.setOnClickListener {
            CoroutineScope(IO).launch {
                update()
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

        dlRequest.addOption("-o", youtubeDLDir.absolutePath + "/%(title)s.%(ext)s")

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


