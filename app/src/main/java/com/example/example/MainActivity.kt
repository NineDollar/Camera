package com.example.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.gyf.immersionbar.ImmersionBar

class MainActivity : AppCompatActivity() {
    private lateinit var camera: ImageView
    private lateinit var gallery: ImageView
    private lateinit var record: ImageView
    private lateinit var playVideo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ImmersionBar.with(this).apply {
            statusBarColor(R.color.purple_500)
        }.init()

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            100
        )

        camera = findViewById(R.id.imageView_camera)
        camera.apply {
            addClickScale()
            this onClick takeCamera()
        }

        record = findViewById(R.id.imageView_record)
        record.addClickScale()
        record onClick takeRecord()
        playVideo = findViewById(R.id.imageView_playVideo)
        playVideo.addClickScale()
        playVideo onClick takePlayVideo()
    }

    private fun takePlayVideo(): () -> Unit = {
        startActivity(Intent(this, PlayVideoActivity::class.java))
    }

    private fun takeRecord(): () -> Unit = {
        startActivity(Intent(this, RecordActivity::class.java))

    }

    private fun takeCamera(): () -> Unit = {
        startActivity(Intent(this, CameraActivity::class.java))
    }

    @SuppressLint("ClickableViewAccessibility")
    fun View.addClickScale(scale: Float = 0.7f, duration: Long = 150) {
        this.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    this.animate().scaleX(scale).scaleY(scale).setDuration(duration).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    this.animate().scaleX(1f).scaleY(1f).setDuration(duration).start()
                }
            }
            this.onTouchEvent(event)
        }
    }
}