package com.example.pozu.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.time.Duration

@Parcelize
data class WorkoutSession(
    val id: String = System.currentTimeMillis().toString(),
    val exercise: Exercise,
    val totalReps: Int,
    val validReps: Int,
    val duration: Long,
    val targetReps: Int? = null,
    val targetDuration: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val accuracy: Float = if (totalReps > 0) (validReps.toFloat() / totalReps) * 100 else 0f
) : Parcelable {

    fun isTargetAchieved(): Boolean {
        return when {
            targetReps != null -> totalReps >= targetReps
            targetDuration != null -> duration >= targetDuration
            else -> true
        }
    }

    fun getFormatedDuration(): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun getFormattedAccuracy(): String {
        return String.format("%.1f%%", accuracy)
    }

    fun getFeedbackMessage(): String {
        return when {
            accuracy >= 90f -> "Excellent form! Keep it up! 🔥"
            accuracy >= 75f -> "Great job! Minor improvements possible 💪"
            accuracy >= 60f -> "Good effort! Focus on form quality 👍"
            else -> "Keep practicing! Quality over quantity 💯"
        }
    }

}
