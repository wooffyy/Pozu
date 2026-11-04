package com.example.pozu.data.model

import android.os.Parcelable
import androidx.annotation.DrawableRes

abstract class Exercise(
    open val id: String,
    open val name: String,
    open val description: String,
    open val targetMuscles: String,
    @DrawableRes open val iconRes: Int,
    open val instructions: List<String>) : Parcelable {

    abstract fun validate(landmarks: Map<String, PoseLandmark>): ExerciseResult
    abstract fun detectRepetition(landmarks: Map<String, PoseLandmark>, previousState: ExerciseState): RepetitionResult
    protected fun hasRequiredLandmarks(landmarks: Map<String, PoseLandmark>, required: List<String>): Boolean {
        return required.all { key ->
            landmarks[key]?.isVisible() == true
        }
    }

    enum class ExerciseState {
        IDLE,
        DOWN_POSITION,
        UP_POSITION,
        OPEN_POSITION,
        CLOSED_POSITION
    }

    data class RepetitionResult(
        val newState: ExerciseState,
        val repCompleted: Boolean,
        val isValid: Boolean
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Exercise) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}