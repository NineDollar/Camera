package com.example.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ImmersionBar
import io.fotoapparat.Fotoapparat
import io.fotoapparat.configuration.CameraConfiguration
import io.fotoapparat.configuration.UpdateConfiguration
import io.fotoapparat.parameter.Zoom
import io.fotoapparat.result.transformer.scaled
import io.fotoapparat.selector.*
import io.fotoapparat.view.CameraView
import java.io.File
import kotlin.math.roundToInt

class CameraActivity : AppCompatActivity() {
    private val TAG = "CameraActivity"
    private var activeCamera: Camera = Camera.Back
    private lateinit var fotoapparat: Fotoapparat
    private lateinit var capture: ImageView
    private lateinit var swtich: ImageView
    private lateinit var focusView: FocusView
    private lateinit var cameraZoom: Zoom.VariableZoom
    private lateinit var flashlight: ImageButton
    private var isChecked = true
    private var curZoom: Float = 0f
    private lateinit var zoomLvl: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()

        val cameraView = findViewById<CameraView>(R.id.camera_view)
        capture = findViewById(R.id.image_capture)
        swtich = findViewById(R.id.image_swtich)
        focusView = findViewById(R.id.focus_View)
        flashlight = findViewById(R.id.imageButton_flashlight)
        zoomLvl = findViewById(R.id.textView_zoomLvl)

        ImmersionBar.with(this).apply {
            statusBarAlpha(0.3f)
        }.init()

        fotoapparat = Fotoapparat(
            context = this,
            view = cameraView,                   // view which will draw the camera preview
            focusView = focusView,
            lensPosition = activeCamera.lensPosition,            // (optional) we want back camera
            cameraConfiguration = activeCamera.configuration, // (optional) define an advanced configuration
            cameraErrorCallback = { Log.e(TAG, "onCreate: ", it) }   // (optional) log fatal errors
        )

        capture onClick takePicture()
        swtich onClick changeCamera()
        flashlight onClick toggleFlash()
    }

    private fun toggleFlash(): () -> Unit = {
        fotoapparat.updateConfiguration(
            UpdateConfiguration(
                flashMode = if (isChecked) {
                    isChecked = false
                    firstAvailable(
                        torch(),
                        off()
                    )
                } else {
                    isChecked = true
                    off()
                }
            )
        )
    }

    private fun takePicture(): () -> Unit = {
        val photoResult = fotoapparat
            .autoFocus()
            .takePicture()

// Asynchronously saves photo to file
        photoResult.saveToFile(
            File(
                getExternalFilesDir("photos"),
                "photo.jpg"
            )
        )

// Asynchronously converts photo to bitmap and returns the result on the main thread
        photoResult
            .toBitmap(scaled(scaleFactor = 0.25f))
            .whenAvailable { photo ->
                photo?.let {
                    val imageView = findViewById<ImageView>(R.id.result)
                    imageView.setImageBitmap(it.bitmap)
                    imageView.rotation = (-it.rotationDegrees).toFloat()
                } ?: Log.e(TAG, "Couldn't capture photo.")
            }
    }

    private fun changeCamera(): () -> Unit = {
        activeCamera = when (activeCamera) {
            Camera.Front -> Camera.Back
            Camera.Back -> Camera.Front
        }
        fotoapparat.switchTo(
            lensPosition = activeCamera.lensPosition,
            cameraConfiguration = activeCamera.configuration
        )

        adjustViewsVisibility()
    }

    private fun adjustViewsVisibility() {
        fotoapparat.getCapabilities()
            .whenAvailable { capabilities ->
                capabilities
                    ?.let {
                        (it.zoom as? Zoom.VariableZoom)
                            ?.let {
                                cameraZoom = it
                                focusView.scaleListener = this::scaleZoom
                                focusView.ptrListener = this::pointerChanged
                            }
                            ?: run {
                                zoomLvl.visibility = View.GONE
                                focusView.scaleListener = null
                                focusView.ptrListener = null
                            }


                    }
                    ?: Log.e(TAG, "Couldn't obtain capabilities.")
            }

        swtich.visibility = if (fotoapparat.isAvailable(front())) View.VISIBLE else View.GONE
    }

    private fun scaleZoom(scaleFactor: Float) {
        //convert to -0.1 ~ 0.1
        val plusZoom = if (scaleFactor < 1) -1 * (1 - scaleFactor) else scaleFactor - 1
        val newZoom = curZoom + plusZoom
        if (newZoom < 0 || newZoom > 1) return

        curZoom = newZoom
        fotoapparat.setZoom(curZoom)
        val progress = (cameraZoom.maxZoom * curZoom).roundToInt()
        val value = cameraZoom.zoomRatios[progress]
        val roundedValue = ((value.toFloat()) / 10).roundToInt().toFloat() / 10

        zoomLvl.visibility = View.VISIBLE
        zoomLvl.text = String.format("%.1f×", roundedValue)
    }

    private fun pointerChanged(fingerCount: Int) {
        if (fingerCount == 0) {
            zoomLvl.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        fotoapparat.start()
        adjustViewsVisibility()
    }

    override fun onStop() {
        super.onStop()
        fotoapparat.stop()
    }
}

private sealed class Camera(
    val lensPosition: LensPositionSelector, val configuration: CameraConfiguration
) {

    object Back : Camera(
        lensPosition = back(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                continuousFocusPicture(),
                autoFocus()
            ),
            frameProcessor = {
                // Do something with the preview frame
            }
        )
    )

    object Front : Camera(
        lensPosition = front(),
        configuration = CameraConfiguration(
            previewResolution = firstAvailable(
                wideRatio(highestResolution()),
                standardRatio(highestResolution())
            ),
            previewFpsRange = highestFps(),
            flashMode = off(),
            focusMode = firstAvailable(
                fixed(),
                autoFocus()
            )
        )
    )
}