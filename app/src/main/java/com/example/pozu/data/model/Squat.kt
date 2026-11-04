package com.example.pozu.data.model

import android.os.Parcelable
import com.example.pozu.R
import kotlinx.parcelize.Parcelize
import kotlin.math.abs

@Parcelize
class Squat(
    override val id: String = "squat",
    override val name: String = "Squat",
    override val description: String = "Lower body strength exercise targeting quads, glutes, and hamstrings",
    override val targetMuscles: String = "Quads, Glutes, Hamstrings",
    override val iconRes: Int = R.drawable.squat_logo, 
    override val instructions: List<String> = listOf(
        "Stand with feet shoulder-width apart",
        "Keep your back straight and chest up",
        "Lower your body by bending knees and hips",
        "Go down until thighs are parallel to ground",
        "Push through heels to return to start"
    )
) : Exercise(id, name, description, targetMuscles, iconRes, instructions), Parcelable {
    companion object {
        private const val DOWN_ANGLE_THRESHOLD = 120f  // Was 110f - less deep squat OK
        private const val UP_ANGLE_THRESHOLD = 150f    // Was 165f - easier to reach
        private const val KNEE_SAFETY_RATIO = 1.3f
    }

    override fun validate(landmarks: Map<String, PoseLandmark>): ExerciseResult {
        val required = listOf(
            PoseLandmark.LEFT_HIP,
            PoseLandmark.LEFT_KNEE,
            PoseLandmark.LEFT_ANKLE,
            PoseLandmark.LEFT_SHOULDER
        )

        if(!hasRequiredLandmarks(landmarks, required)) {
            return ExerciseResult(
                isCorrect = false,
                feedback = "Stand facing sideways to the camera",
                confidence = 0.3f
            )
        }

        val leftHip = landmarks[PoseLandmark.LEFT_HIP]!!
        val leftKnee = landmarks[PoseLandmark.LEFT_KNEE]!!
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]!!
        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]!!

        val kneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)
        val kneePastToes = leftKnee.x > (leftKnee.x * KNEE_SAFETY_RATIO)
        val backAngle = calculateAngle(leftShoulder, leftHip, leftKnee)
        val isBackStraight = backAngle > 150f

        return when {
            kneePastToes -> ExerciseResult(
                isCorrect = false,
                feedback = "Don\'t let knees go past your toes!",
                angle = kneeAngle,
                confidence = 0.8f
            )
            !isBackStraight -> ExerciseResult(
                isCorrect = false,
                feedback = "Keep your back straight and chest up",
                angle = kneeAngle,
                secondaryAngle = backAngle,
                confidence = 0.8f
            )
            kneeAngle < DOWN_ANGLE_THRESHOLD -> ExerciseResult(
                isCorrect = true,
                feedback = "Great depth! Push back up",
                angle = kneeAngle,
                confidence = 0.9f
            )
            kneeAngle > UP_ANGLE_THRESHOLD -> ExerciseResult(
                isCorrect = true,
                feedback = "Good form! Ready to go down",
                angle = kneeAngle,
                confidence = 0.9f
            )
            else -> ExerciseResult(
                isCorrect = true,
                feedback = "Go lower!",
                angle = kneeAngle,
                confidence = 0.8f
            )
        }
    }

    override fun detectRepetition(
        landmarks: Map<String, PoseLandmark>,
        previousState: ExerciseState
    ): RepetitionResult {
        val leftHip = landmarks[PoseLandmark.LEFT_HIP]
        val leftKnee = landmarks[PoseLandmark.LEFT_KNEE]
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]

        if (leftHip == null || leftKnee == null || leftAnkle == null) {
            return RepetitionResult(previousState, false, false)
        }

        val kneeAngle = calculateAngle(leftHip, leftKnee, leftAnkle)
        return when (previousState) {
            ExerciseState.IDLE, ExerciseState.UP_POSITION -> {
                if (kneeAngle < DOWN_ANGLE_THRESHOLD) {
                    RepetitionResult(ExerciseState.DOWN_POSITION, false, true)
                } else {
                    RepetitionResult(ExerciseState.UP_POSITION, false, true)
                }
            }
            ExerciseState.DOWN_POSITION -> {
                if (kneeAngle > UP_ANGLE_THRESHOLD) {
                    RepetitionResult(ExerciseState.UP_POSITION, true, true)
                } else {
                    RepetitionResult(ExerciseState.DOWN_POSITION, false, true)
                }
            }
            else -> RepetitionResult(previousState, false, false)
        }
    }

    private fun calculateAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Float {
        val radians = kotlin.math.atan2(lastPoint.y - midPoint.y, lastPoint.x - midPoint.x) -
                kotlin.math.atan2(firstPoint.y - midPoint.y, firstPoint.x - midPoint.x)

        var angle = Math.toDegrees(radians.toDouble()).toFloat()

        if (angle < 0){
            angle += 360f
        }
        if (angle > 180f){
            angle = 360f - angle
        }
        return abs(angle)
    }

}