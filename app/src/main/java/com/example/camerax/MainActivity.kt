package com.example.camerax

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.ImageAnalysis
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Main entry point of the Face Recognition App.
 *
 * This activity manages the full lifecycle of real-time face detection:
 * 1. Requests the [Manifest.permission.CAMERA] permission at runtime.
 * 2. Binds a CameraX [Preview] use-case so the user sees a live camera feed.
 * 3. Binds a CameraX [ImageAnalysis] use-case that pipes every frame through
 *    the Google ML Kit [com.google.mlkit.vision.face.FaceDetector].
 * 4. Forwards detected-face bounding boxes to [FaceBoxOverlay], which draws
 *    green rectangles around each face on top of the live preview.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Activity-result launcher used to request the [Manifest.permission.CAMERA] permission.
     *
     * On a granted result [startCamera] is invoked immediately; on denial a toast is shown
     * and the camera is not started.
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Checks whether the [Manifest.permission.CAMERA] permission has already been granted.
     *
     * If the permission is already available [startCamera] is called directly; otherwise
     * [requestPermissionLauncher] initiates the system permission dialog.
     */
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Configures and starts the CameraX pipeline with face-detection analysis.
     *
     * Two CameraX use-cases are bound to the activity lifecycle:
     * - **Preview** — renders the live camera feed inside the [androidx.camera.view.PreviewView]
     *   with id `R.id.viewFinder`.
     * - **ImageAnalysis** — receives every camera frame (keeping only the latest when the
     *   processor is busy) and runs it through the ML Kit
     *   [com.google.mlkit.vision.face.FaceDetector] configured in
     *   [com.google.mlkit.vision.face.FaceDetectorOptions.PERFORMANCE_MODE_FAST].
     *
     * Detected face bounding boxes are forwarded to [FaceBoxOverlay.setFaces] so that green
     * rectangles are drawn over the live preview.  The default back camera is used.
     */
    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    val viewFinder = findViewById<androidx.camera.view.PreviewView>(R.id.viewFinder)
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val faceOverlay = findViewById<FaceBoxOverlay>(R.id.faceOverlay)
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .build()
            val detector = FaceDetection.getClient(options)

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    detector.process(image)
                        .addOnSuccessListener { faces ->
                            faceOverlay.setFaces(faces, image.width, image.height)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )
            } catch (exc: Exception) {
                exc.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Called when the activity is first created.
     *
     * Sets up edge-to-edge display, inflates the main layout, applies system-bar insets so
     * content is not obscured by navigation or status bars, and then calls [checkPermissions]
     * to begin the camera/permission flow.
     *
     * @param savedInstanceState If the activity is being re-created from a previous saved state,
     *   this bundle contains the data it most recently supplied in
     *   [onSaveInstanceState]; otherwise it is `null`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkPermissions()
    }
}