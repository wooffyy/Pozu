package com.example.pozu.ml

import android.content.Context
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark as MlKitPoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.example.pozu.data.model.PoseLandmark as AppPoseLandmark
import com.example.pozu.utils.Constants

/**
 * Processor for ML Kit Pose Detection
 */
class PoseDetectorProcessor(context: Context) {

    private var poseDetector: PoseDetector

    // FIXED: Correct callback type
    private var onPoseDetected: ((Map<String, AppPoseLandmark>) -> Unit)? = null
    private var lastProcessTime = 0L

    companion object {
        private const val TAG = Constants.TAG_POSE_PROCESSOR
        private const val PROCESS_INTERVAL_MS = 100L  // Process every 100ms max
    }

    init {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build()

        poseDetector = PoseDetection.getClient(options)
        Log.d(TAG, "PoseDetectorProcessor initialized")
    }

    /**
     * Set callback for pose detection results
     */
    fun setOnPoseDetectedListener(listener: (Map<String, AppPoseLandmark>) -> Unit) {
        onPoseDetected = listener
    }

    /**
     * Process camera frame from CameraX
     */
    @androidx.camera.core.ExperimentalGetImage
    fun processImageProxy(imageProxy: ImageProxy) {
        // Throttle processing to avoid overload
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessTime < PROCESS_INTERVAL_MS) {
            imageProxy.close()
            return
        }
        lastProcessTime = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    processPose(pose)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Pose detection failed: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    /**
     * Convert ML Kit Pose to app landmark map
     */
    private fun processPose(pose: Pose) {
        val landmarks = mutableMapOf<String, AppPoseLandmark>()

        // Map all landmarks
        mapLandmark(pose, MlKitPoseLandmark.NOSE, AppPoseLandmark.NOSE, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_EYE, AppPoseLandmark.LEFT_EYE, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_EYE, AppPoseLandmark.RIGHT_EYE, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_EAR, AppPoseLandmark.LEFT_EAR, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_EAR, AppPoseLandmark.RIGHT_EAR, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_SHOULDER, AppPoseLandmark.LEFT_SHOULDER, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_SHOULDER, AppPoseLandmark.RIGHT_SHOULDER, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_ELBOW, AppPoseLandmark.LEFT_ELBOW, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_ELBOW, AppPoseLandmark.RIGHT_ELBOW, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_WRIST, AppPoseLandmark.LEFT_WRIST, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_WRIST, AppPoseLandmark.RIGHT_WRIST, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_HIP, AppPoseLandmark.LEFT_HIP, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_HIP, AppPoseLandmark.RIGHT_HIP, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_KNEE, AppPoseLandmark.LEFT_KNEE, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_KNEE, AppPoseLandmark.RIGHT_KNEE, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.LEFT_ANKLE, AppPoseLandmark.LEFT_ANKLE, landmarks)
        mapLandmark(pose, MlKitPoseLandmark.RIGHT_ANKLE, AppPoseLandmark.RIGHT_ANKLE, landmarks)

        // Callback with detected landmarks
        if (landmarks.isNotEmpty()) {
            onPoseDetected?.invoke(landmarks)
            Log.d(TAG, "Pose detected: ${landmarks.size} landmarks, callback invoked")
        } else {
            Log.d(TAG, "No landmarks detected in frame")
        }
    }

    /**
     * Map single landmark
     */
    private fun mapLandmark(
        pose: Pose,
        mlKitType: Int,
        appKey: String,
        landmarksMap: MutableMap<String, AppPoseLandmark>
    ) {
        val landmark = pose.getPoseLandmark(mlKitType)
        if (landmark != null) {
            landmarksMap[appKey] = AppPoseLandmark(
                x = landmark.position.x,
                y = landmark.position.y,
                z = landmark.position3D.z,
                visibility = landmark.inFrameLikelihood
            )
        }
    }

    fun close() {
        poseDetector.close()
        Log.d(TAG, "PoseDetectorProcessor closed")
    }
}