package com.example.pozu.data.model

import android.os.Parcelable
import android.util.Log
import com.example.pozu.R
import com.example.pozu.ml.ExerciseValidator
import kotlinx.parcelize.Parcelize
import kotlin.math.abs

// Inheritance Exercise
@Parcelize
class PushUp(
    override val id: String = "pushup",
    override val name: String = "Push Up",
    override val description: String = "Upper body strength exercise targeting chest, triceps, and shoulders",
    override val targetMuscles: String = "Chest, Triceps, Shoulders",
    override val iconRes: Int = R.drawable.pushup_logo, 
    override val instructions: List<String> = listOf(
        "Start in plank position with hands shoulder-width apart",
        "Keep your body straight from head to heels",
        "Lower your body until chest nearly touches the ground",
        "Push back up to starting position",
        "Keep core engaged throughout the movement"
    )
) : Exercise(id, name, description, targetMuscles, iconRes, instructions), Parcelable {
    companion object {
        private const val DOWN_ANGLE_THRESHOLD = 110f  // Was 100f - easier to reach
        private const val UP_ANGLE_THRESHOLD = 150f    // Was 165f - easier to reach
        private const val BODY_ALIGNMENT_TOLERANCE = 20f // Was 15f - more forgiving
    }

    override fun validate(landmarks: Map<String, PoseLandmark>): ExerciseResult {
        val required = listOf(
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.LEFT_ANKLE
        )

        if(!hasRequiredLandmarks(landmarks, required)) {
            return ExerciseResult(
                isCorrect = false,
                feedback = "Position yourself so your full body is visible",
                confidence = 0.3f
            )
        }

        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]!!
        val leftElbow = landmarks[PoseLandmark.LEFT_ELBOW]!!
        val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]!!
        val leftHip = landmarks[PoseLandmark.LEFT_HIP]!!
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]!!

        val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)
        val bodyAlignment = calculateAngle(leftShoulder, leftHip, leftAnkle)
        val isBodyStraight = abs(180f - bodyAlignment) < BODY_ALIGNMENT_TOLERANCE
        Log.d("CAL", "PUSHUP elbow=$elbowAngle body=$bodyAlignment")

        return when {
            !isBodyStraight -> ExerciseResult(
                isCorrect = false,
                feedback = "Keep your body straight! Don\'t sag your hips",
                angle = elbowAngle,
                secondaryAngle = bodyAlignment,
                confidence = 0.8f
            )
            elbowAngle > UP_ANGLE_THRESHOLD -> ExerciseResult(
                isCorrect = true,
                feedback = "Good form! Ready to go down",
                angle = elbowAngle,
                confidence = 0.9f
            )
            elbowAngle < DOWN_ANGLE_THRESHOLD -> ExerciseResult(
                isCorrect = true,
                feedback = "Perfect depth! Push back up",
                angle = elbowAngle,
                confidence = 0.9f
            )
            else -> ExerciseResult(
                isCorrect = true,
                feedback = "Keep going!",
                angle = elbowAngle,
                confidence = 0.8f
            )
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

    override fun detectRepetition(
        landmarks: Map<String, PoseLandmark>,
        previousState: ExerciseState
    ): RepetitionResult {
        val required = listOf(
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.LEFT_WRIST
        )
        if (!hasRequiredLandmarks(landmarks, required)) {
            return RepetitionResult(previousState, false, false)
        }
        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]!!
        val leftElbow = landmarks[PoseLandmark.LEFT_ELBOW]!!
        val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]!!

        val elbowAngle = calculateAngle(leftShoulder, leftElbow, leftWrist)

        return when (previousState) {
            ExerciseState.IDLE, ExerciseState.UP_POSITION -> {
                if (elbowAngle < DOWN_ANGLE_THRESHOLD) {
                    RepetitionResult(ExerciseState.DOWN_POSITION, false, true)
                } else {
                    RepetitionResult(ExerciseState.UP_POSITION, false, true)
                }
            }

            ExerciseState.DOWN_POSITION -> {
                if (elbowAngle > UP_ANGLE_THRESHOLD) {
                    RepetitionResult(ExerciseState.UP_POSITION, true, true)
                } else {
                    RepetitionResult(ExerciseState.DOWN_POSITION, false, true)
                }
            }
            else -> RepetitionResult(previousState, false, false)
        }
    }

    }
