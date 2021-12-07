package com.example.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import java.io.File

class PlayVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)

        val videoView = findViewById<VideoView>(R.id.playVideo_videoView)
        MediaController(this).apply {
            setPrevNextListeners(this, this)
            videoView.setMediaController(this)

            videoView.setVideoPath(File(getExternalFilesDir(""), "a.mp4").absolutePath)
            videoView.start()
        }
    }

    private fun setPrevNextListeners(
        mediaController: MediaController,
        mediaController1: MediaController
    ) {

    }
}