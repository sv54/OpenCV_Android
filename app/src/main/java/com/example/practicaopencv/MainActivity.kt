package com.example.practicaopencv

import android.content.pm.PackageManager
import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), CameraBridgeViewBase.CvCameraViewListener2 {

    private lateinit var mOpenCvCameraView: CameraBridgeViewBase
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private var isCameraOn = true
    private lateinit var blurSeekBar: SeekBar
    private lateinit var edgeGradientSeekBar: SeekBar
    private lateinit var angleSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        else{
            mOpenCvCameraView = findViewById(R.id.camera_view)
            mOpenCvCameraView.setCameraPermissionGranted()
            mOpenCvCameraView.visibility = CameraBridgeViewBase.VISIBLE
            mOpenCvCameraView.setCvCameraViewListener(this)

            blurSeekBar = findViewById(R.id.blur_seekbar)
            edgeGradientSeekBar = findViewById(R.id.edge_gradient_seekbar)
            angleSeekBar = findViewById(R.id.angle_seekbar)
        }

        val toggleButton = findViewById<Button>(R.id.start_stop_button)
        toggleButton.setOnClickListener {
            if (isCameraOn) {
                stopCamera()
                isCameraOn = false
                toggleButton.text = "Start"
            } else {
                startCamera()
                isCameraOn = true
                toggleButton.text = "Stop"
            }
        }



    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV initialization failed")
        } else {
            Log.d(TAG, "OpenCV initialization succeeded")
            mOpenCvCameraView.enableView()
        }
    }

    override fun onPause() {
        super.onPause()
        mOpenCvCameraView.disableView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mOpenCvCameraView.disableView()
    }

    private fun startCamera() {
        mOpenCvCameraView.enableView()
    }

    private fun stopCamera() {
        mOpenCvCameraView.disableView()
    }



    override fun onCameraFrame(inputFrame: CameraBridgeViewBase.CvCameraViewFrame?): Mat {
        var src = inputFrame?.rgba() ?: Mat()
        Core.rotate(src, src, Core.ROTATE_90_CLOCKWISE)
        val blurSize = if (blurSeekBar.progress == 0) 1 else blurSeekBar.progress * 2 + 1
        // Aplicar el filtro de desenfoque gaussiano

        Imgproc.GaussianBlur(src, src, Size(blurSize.toDouble(), blurSize.toDouble()), 0.0)

        // Aplicar el filtro de detección de bordes Canny
        val srcCopy = src.clone() // Crear una copia de imagen de entrada
        val threshold1 = edgeGradientSeekBar.progress.toDouble()
        val threshold2 = 2 * threshold1
        Imgproc.Canny(srcCopy, src, threshold1, threshold2)

        // Ajustar el ángulo del filtro de deteccion de bordes
        val angle = angleSeekBar.progress.toDouble()
        val cx = src.width() / 2.0
        val cy = src.height() / 2.0
        val rotationMatrix = Imgproc.getRotationMatrix2D(Point(cx, cy), angle, 1.0)
        val rotatedSrc = Mat()
        Imgproc.warpAffine(src, rotatedSrc, rotationMatrix, src.size())

        src.release()
        rotationMatrix.release()
        return rotatedSrc
    }

    override fun onCameraViewStarted(width: Int, height: Int) {}

    override fun onCameraViewStopped() {}

    companion object {
        private const val TAG = "MainActivity"
    }
}