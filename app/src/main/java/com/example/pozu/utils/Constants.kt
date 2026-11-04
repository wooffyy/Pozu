package com.example.pozu.utils

object Constants {
    // Intent Extra Keys
    const val EXTRA_EXERCISE = "extra_exercise"
    const val EXTRA_WORKOUT_SESSION = "extra_workout_session"
    const val EXTRA_TARGET_REPS = "extra_target_reps"
    const val EXTRA_TARGET_DURATION = "extra_target_duration"
    const val EXTRA_HAS_TARGET = "extra_has_target"

    // Shared Preferences Keys
    const val PREFS_NAME = "pozu_preferences"
    const val KEY_FIRST_LAUNCH = "first_launch"
    const val KEY_CALIBRATION_DONE = "calibration_done"

    // Camera & ML
    const val CAMERA_PERMISSION_REQUEST_CODE = 100
    const val POSE_DETECTION_CONFIDENCE_THRESHOLD = 0.5f

    // Exercise Timing
    const val COUNTDOWN_DURATION_MS = 3000L  // 3 seconds countdown before start
    const val MIN_EXERCISE_DURATION_MS = 5000L  // Minimum 5 seconds

    // UI Update Intervals
    const val TIMER_UPDATE_INTERVAL_MS = 100L
    const val POSE_DETECTION_INTERVAL_MS = 50L

    // Default Target Values
    const val DEFAULT_TARGET_REPS_PUSHUP = 10
    const val DEFAULT_TARGET_REPS_SQUAT = 15
    const val DEFAULT_TARGET_DURATION_JUMPINGJACK = 30000L  // 30 seconds

    // Feedback Display Duration
    const val FEEDBACK_DISPLAY_DURATION_MS = 2000L

    // Rep Validation
    const val MIN_TIME_BETWEEN_REPS_MS = 500L  // Prevent double counting

    // Log Tags
    const val TAG_HOME = "HomeActivity"
    const val TAG_DETAIL = "ExerciseDetailActivity"
    const val TAG_CAMERA = "CameraExerciseActivity"
    const val TAG_RESULT = "ResultActivity"
    const val TAG_HISTORY = "HistoryActivity"
    const val TAG_POSE_PROCESSOR = "PoseDetectorProcessor"

    // Result Codes
    const val RESULT_CODE_EXERCISE_COMPLETED = 200
    const val RESULT_CODE_EXERCISE_CANCELLED = 201
}