package com.example.pozu.data.model

data class ExerciseResult (
    val isCorrect: Boolean,
    val feedback: String,
    val angle: Float? = null,
    val secondaryAngle: Float? = null,
    val thirdAngle: Float? = null,
    val confidence: Float = 1.0f) {

    fun getFeedbackColor(): FeedbackColor {
        return when {
            !isCorrect -> FeedbackColor.INCORRECT
            confidence < 0.7f -> FeedbackColor.WARNING
            else -> FeedbackColor.CORRECT
        }
    }

    enum class FeedbackColor{
        CORRECT,
        WARNING,
        INCORRECT
    }
}