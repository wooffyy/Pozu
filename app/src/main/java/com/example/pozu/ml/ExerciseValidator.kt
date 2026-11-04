package com.example.pozu.ml

import android.util.Log
import com.example.pozu.data.model.Exercise
import com.example.pozu.data.model.ExerciseResult
import com.example.pozu.data.model.PoseLandmark
import com.example.pozu.utils.Constants

class ExerciseValidator {
    private var currentExercise: Exercise? = null
    private var currentState: Exercise.ExerciseState = Exercise.ExerciseState.IDLE
    private var repCount = 0
    private var validRepCount = 0
    private var lastRepTime = 0L
    private var formFaultInRep = false

    companion object {
        private const val TAG = "ExerciseValidator"
    }

    fun setExercise(exercise: Exercise) {
        currentExercise = exercise
        reset()
        Log.d(TAG, "Exercise set to ${exercise.name}")
    }

    fun validatePose(landmark: Map<String, PoseLandmark>): ExerciseResult {
        val exercise = currentExercise ?: return ExerciseResult(false, "No exercise selected")
        val result = exercise.validate(landmark)
        if (currentState == Exercise.ExerciseState.DOWN_POSITION &&
            !result.isCorrect && result.confidence >= 0.5f) formFaultInRep = true
        return result
    }

    fun detectRepetition(landmarks: Map<String, PoseLandmark>): RepetitionDetectionResult {
        val exercise = currentExercise ?: return RepetitionDetectionResult(false, repCount, validRepCount)

        val currentTime = System.currentTimeMillis()

        // Debounce to prevent counting reps too quickly
        if (currentTime - lastRepTime < Constants.MIN_TIME_BETWEEN_REPS_MS) {
            return RepetitionDetectionResult(false, repCount, validRepCount)
        }

        // Get new state from the specific exercise logic
        val repResult = exercise.detectRepetition(landmarks, currentState)

        // Update the current state
        currentState = repResult.newState

        // Check if the exercise logic flagged this as a completed rep
        if (repResult.repCompleted) {
            val valid = repResult.isValid && !formFaultInRep
            formFaultInRep = false
            repCount++
            if (valid) validRepCount++

            lastRepTime = currentTime

            Log.d(TAG, "REP COMPLETED! Total: $repCount, Valid: $validRepCount, New State: $currentState")

            return RepetitionDetectionResult(
                repCompleted = true,
                totalReps = repCount,
                validReps = validRepCount,
                wasValidRep = valid
            )
        }
        // If no rep was completed, just return the current counts
        return RepetitionDetectionResult(false, repCount, validRepCount)
    }

    fun getRepCount(): Int = repCount
    fun getValidRepCount(): Int = validRepCount
    fun getCurrentState(): Exercise.ExerciseState = currentState

    fun reset() {
        currentState = Exercise.ExerciseState.IDLE
        repCount = 0
        validRepCount = 0
        lastRepTime = 0L
        formFaultInRep = false
        Log.d(TAG, "Validator reset")
    }

    data class RepetitionDetectionResult(
        val repCompleted: Boolean,
        val totalReps: Int,
        val validReps: Int,
        val wasValidRep: Boolean = false
    )
}
