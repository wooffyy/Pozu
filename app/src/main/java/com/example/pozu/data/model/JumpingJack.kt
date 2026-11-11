package com.example.pozu.data.model

import android.os.Parcelable
import android.util.Log
import com.example.pozu.R
import kotlinx.parcelize.Parcelize
import kotlin.math.abs

@Parcelize
class JumpingJack(
    override val id: String = "jumpingjack",
    override val name: String = "Jumping Jack",
    override val description: String = "Full body cardio exercise",
    override val targetMuscles: String = "Full Body - Cardio",
    override val iconRes: Int = R.drawable.jj_logo, 
    override val instructions: List<String> = listOf(
        "Stand with feet together and arms at sides",
        "Jump while spreading legs shoulder-width apart",
        "Simultaneously raise arms above head",
        "Jump back to starting position",
        "Maintain steady rhythm"
    )
) : Exercise(id, name, description, targetMuscles, iconRes, instructions), Parcelable {
    companion object {
        private const val LEG_SPREAD_THRESHOLD = 1.5f
        private const val ARMS_UP_MARGIN = 0.4f // needs calibration
    }

    override fun validate(landmarks: Map<String, PoseLandmark>): ExerciseResult {
        val required = listOf(
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.LEFT_ELBOW,
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.RIGHT_ELBOW,
            PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_HIP,
            PoseLandmark.LEFT_ANKLE,
            PoseLandmark.RIGHT_ANKLE
        )

        if(!hasRequiredLandmarks(landmarks, required)) {
            return ExerciseResult(
                isCorrect = false,
                feedback = "Stand facing the camera with full body visible",
                confidence = 0.3f
            )
        }

        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]!!
        val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]!!
        val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]!!
        val rightWrist = landmarks[PoseLandmark.RIGHT_WRIST]!!
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]!!
        val rightAnkle = landmarks[PoseLandmark.RIGHT_ANKLE]!!
        val leftHip = landmarks[PoseLandmark.LEFT_HIP]!!
        val rightHip = landmarks[PoseLandmark.RIGHT_HIP]!!

        val avgWristY = (leftWrist.y + rightWrist.y) / 2
        val isArmsUp = areArmsRaised(leftShoulder, rightShoulder, leftWrist, rightWrist, leftHip, rightHip)

        val ankleSpread = abs(rightAnkle.x - leftAnkle.x)
        val hipWidth = abs(rightHip.x - leftHip.x)
        // Avoid division by zero if hip width is not detected
        val legSpreadRatio = if (hipWidth > 0) ankleSpread / hipWidth else 0f

        val isLegsOpen = legSpreadRatio > LEG_SPREAD_THRESHOLD
        val torso = abs((leftHip.y + rightHip.y) / 2 - (leftShoulder.y + rightShoulder.y) / 2)
        val armsRatio = ((leftShoulder.y + rightShoulder.y) / 2 - avgWristY) / torso
        Log.d("CAL", "JJ armsRatio=$armsRatio legRatio=$legSpreadRatio")

        return when {
            isArmsUp && isLegsOpen -> ExerciseResult(
                isCorrect = true,
                feedback = "Perfect open position! 🙌",
                angle = avgWristY,
                secondaryAngle = legSpreadRatio,
                confidence = 0.9f
            )

            !isArmsUp && !isLegsOpen -> ExerciseResult(
                isCorrect = true,
                feedback = "Good closed position! 👏",
                angle = avgWristY,
                secondaryAngle = legSpreadRatio,
                confidence = 0.9f
            )

            !isArmsUp && isLegsOpen -> ExerciseResult(
                isCorrect = true,
                feedback = "Raise your arms higher! ⬆️",
                angle = avgWristY,
                confidence = 0.6f
            )
            isArmsUp && !isLegsOpen -> ExerciseResult(
                isCorrect = true,
                feedback = "Spread your legs wider! ↔️",
                angle = avgWristY,
                secondaryAngle = legSpreadRatio,
                confidence = 0.6f
            )
            
            else -> ExerciseResult(
                isCorrect = true,
                feedback = "Keep moving!",
                angle = avgWristY,
                confidence = 0.8f
            )
        }
    }

    override fun detectRepetition(
        landmarks: Map<String, PoseLandmark>,
        previousState: ExerciseState
    ): RepetitionResult {
        val required = listOf(
            PoseLandmark.LEFT_SHOULDER,
            PoseLandmark.RIGHT_SHOULDER,
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.RIGHT_WRIST,
            PoseLandmark.LEFT_ANKLE,
            PoseLandmark.RIGHT_ANKLE,
            PoseLandmark.LEFT_HIP,
            PoseLandmark.RIGHT_HIP
        )

        if (!hasRequiredLandmarks(landmarks, required)) {
            return RepetitionResult(previousState, false, false)
        }

        val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]!!
        val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]!!
        val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]!!
        val rightWrist = landmarks[PoseLandmark.RIGHT_WRIST]!!
        val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]!!
        val rightAnkle = landmarks[PoseLandmark.RIGHT_ANKLE]!!
        val leftHip = landmarks[PoseLandmark.LEFT_HIP]!!
        val rightHip = landmarks[PoseLandmark.RIGHT_HIP]!!
        
        val isArmsUp = areArmsRaised(leftShoulder, rightShoulder, leftWrist, rightWrist, leftHip, rightHip)

        // Check leg spread
        val ankleSpread = abs(rightAnkle.x - leftAnkle.x)
        val hipWidth = abs(rightHip.x - leftHip.x)
        val legSpreadRatio = if (hipWidth > 0) ankleSpread / hipWidth else 0f
        val isLegsOpen = legSpreadRatio > LEG_SPREAD_THRESHOLD
        // Determine if the user is in the "open" position
        val isOpen = isArmsUp && isLegsOpen

        // State machine logic for detecting a repetition
        return when (previousState) {
            ExerciseState.IDLE, ExerciseState.CLOSED_POSITION -> {
                if (isOpen) {
                    // Transition from closed to open
                    RepetitionResult(ExerciseState.OPEN_POSITION, false, true)
                } else {
                    // Stay in closed state
                    RepetitionResult(ExerciseState.CLOSED_POSITION, false, true)
                }
            }
            ExerciseState.OPEN_POSITION -> {
                // ***MODIFIED LOGIC***
                // If the user is no longer in the open position, count it as a rep
                if (!isOpen) {
                    // Completed one rep by moving from open to closed!
                    RepetitionResult(ExerciseState.CLOSED_POSITION, true, true)
                } else {
                    // Stay in the open state
                    RepetitionResult(ExerciseState.OPEN_POSITION, false, true)
                }
            }
            // Default case to handle any other states, though they aren\'t used for this exercise
            else -> RepetitionResult(previousState, false, false)
        }
    }

    private fun areArmsRaised(ls: PoseLandmark, rs: PoseLandmark, lw: PoseLandmark,
        rw: PoseLandmark, lh: PoseLandmark, rh: PoseLandmark): Boolean{

        val shoulderY = (ls.y + rs.y) / 2
        val wristY = (lw.y + rw.y) / 2
        val torso = abs((lh.y + rh.y)/2 -shoulderY)
        return wristY < shoulderY - ARMS_UP_MARGIN * torso
    }
    
}
