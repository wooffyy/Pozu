package com.example.pozu.ui.camera

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.pozu.data.model.Exercise
import com.example.pozu.data.model.WorkoutSession
import com.example.pozu.databinding.ActivityCameraExerciseBinding
import com.example.pozu.ml.ExerciseValidator
import com.example.pozu.ml.PoseDetectorProcessor
import com.example.pozu.ui.result.ResultActivity
import com.example.pozu.utils.Constants
import com.example.pozu.utils.PermissionHelper
import com.example.pozu.utils.showToast
import com.example.pozu.utils.toTimeString
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class CameraExerciseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraExerciseBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var poseDetectorProcessor: PoseDetectorProcessor
    private lateinit var exerciseValidator: ExerciseValidator

    private var exercise: Exercise? = null
    private var hasTarget = false
    private var targetReps: Int? = null
    private var targetDuration: Long? = null
    private var startTime = 0L
    private var elapsedTime = 0L
    private var isExerciseActive = false
    private var isStopping = false
    private val repCount = AtomicInteger(0)
    private val validRepCount = AtomicInteger(0)
    private val timerHandler = Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isExerciseActive) {
                elapsedTime = System.currentTimeMillis() - startTime
                updateTimerUI()
                timerHandler.postDelayed(this, Constants.TIMER_UPDATE_INTERVAL_MS)
            }
        }
    }
    private var pausedAt = 0L

    companion object {
        private const val TAG = Constants.TAG_CAMERA
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraExerciseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exercise = intent.getParcelableExtra(Constants.EXTRA_EXERCISE)
        if (exercise == null) {
            showToast("Error: Exercise not found")
            finish()
            return
        }

        hasTarget = intent.getBooleanExtra(Constants.EXTRA_HAS_TARGET, false)
        targetReps = intent.getIntExtra(Constants.EXTRA_TARGET_REPS, 0).takeIf { it > 0 }
        targetDuration = intent.getLongExtra(Constants.EXTRA_TARGET_DURATION, 0).takeIf { it > 0 }

        setupUI(exercise!!)
        initializeML(exercise!!)
        setupClickListeners()

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (PermissionHelper.isCameraPermissionGranted(this)) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupUI(exercise: Exercise) {
        binding.tvExerciseName.text = exercise.name
        binding.tvFeedback.text = "Get ready!"
        updateRepCountUI(repCount.get())
    }

    private fun initializeML(exercise: Exercise) {
        poseDetectorProcessor = PoseDetectorProcessor(this)
        exerciseValidator = ExerciseValidator().apply {
            setExercise(exercise)
        }
        poseDetectorProcessor.setOnPoseDetectedListener { landmarks ->
            processPose(landmarks)
        }
    }

    private fun setupClickListeners() {
        binding.btnStop.setOnClickListener { stopExercise() }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            poseDetectorProcessor.processImageProxy(imageProxy)
                        }
                    }
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                binding.previewView.post { startExercise() }
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                showToast("Failed to start camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startExercise() {
        if (isExerciseActive) return
        isExerciseActive = true
        startTime = System.currentTimeMillis()
        timerHandler.post(timerRunnable)
        showToast("Exercise started! Get ready!")
        runOnUiThread { binding.tvFeedback.text = "Get into position!" }
    }

    private fun processPose(landmarks: Map<String, com.example.pozu.data.model.PoseLandmark>) {
        if (!isExerciseActive) return

        val validationResult = exerciseValidator.validatePose(landmarks)
        runOnUiThread { updateFeedbackUI(validationResult) }

        val repResult = exerciseValidator.detectRepetition(landmarks)

        if (repResult.repCompleted) {
            repCount.set(repResult.totalReps)
            validRepCount.set(repResult.validReps)

            runOnUiThread {
                updateRepCountUI(repResult.totalReps)

                targetReps?.let {
                    if (repResult.totalReps >= it) {
                        stopExercise()
                    }
                }
            }
        }
    }

    private fun stopExercise() {
        if (isStopping) return
        isStopping = true

        isExerciseActive = false
        timerHandler.removeCallbacks(timerRunnable)
        navigateToResult()
    }

    private fun updateFeedbackUI(result: com.example.pozu.data.model.ExerciseResult) {
        binding.tvFeedback.text = result.feedback
    }

    private fun updateRepCountUI(newRepCount: Int) {
        binding.tvRepCounter.text = newRepCount.toString()
        if (hasTarget && targetReps != null) {
            binding.tvTargetProcess.visibility = View.VISIBLE
            binding.tvTargetProcess.text = "Target: $newRepCount/$targetReps"
        }
    }

    private fun updateTimerUI() {
        binding.tvTimer.text = elapsedTime.toTimeString()
    }

    private fun navigateToResult() {
        val session = WorkoutSession(
            exercise = exercise!!,
            totalReps = repCount.get(),
            validReps = validRepCount.get(),
            duration = elapsedTime,
            targetReps = targetReps,
            targetDuration = targetDuration
        )
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(Constants.EXTRA_WORKOUT_SESSION, session)
        }
        startActivity(intent)
        finish()
    }

    private fun requestCameraPermission() {
        PermissionHelper.requestCameraPermission(this, Constants.CAMERA_PERMISSION_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.handlePermissionResult(
            requestCode = requestCode,
            permissions = permissions,
            grantResults = grantResults,
            expectedRequestCode = Constants.CAMERA_PERMISSION_REQUEST_CODE,
            onGranted = { startCamera() },
            onDenied = {
                showToast("Camera permission is required")
                finish()
            }
        )
    }

    override fun onPause() {
        super.onPause()
        if (isExerciseActive) {
            isExerciseActive = false
            pausedAt = System.currentTimeMillis()
            timerHandler.removeCallbacks(timerRunnable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (pausedAt > 0 && !isStopping) {
            startTime += System.currentTimeMillis() - pausedAt
            pausedAt = 0
            isExerciseActive = true
            timerHandler.post(timerRunnable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::poseDetectorProcessor.isInitialized) {
            poseDetectorProcessor.close()
        }
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }
}
