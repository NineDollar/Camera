package com.example.example

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            100
        )
    }

    fun onClick(view: android.view.View) {
        when (view.id) {
            R.id.btn_record -> startActivity(Intent(this, RecordActivity::class.java))
            R.id.btn_playVideo -> startActivity(Intent(this, PlayVideoActivity::class.java))
        }
    }
}