package com.example.pozu.ml

import com.example.pozu.data.model.PoseLandmark
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

object AngleCalculator {
    fun calculateAngle(
        firstPoint: PoseLandmark,
        midPoint: PoseLandmark,
        lastPoint: PoseLandmark
    ): Float {
        val radians = atan2(
            lastPoint.y - midPoint.y,
            lastPoint.x - midPoint.x
        ) - atan2(
            firstPoint.y - midPoint.y,
            firstPoint.x - midPoint.x
        )

        var angle = Math.toDegrees(radians.toDouble()).toFloat()

        if (angle < 0){
            angle += 360f
        }
        if (angle > 180f) {
            angle = 360f - angle
        }

        return abs(angle)
    }

    fun calculateDistance(
        firstPoint: PoseLandmark,
        secondPoint: PoseLandmark
    ): Float {
        val dx = secondPoint.x - firstPoint.x
        val dy = secondPoint.y - firstPoint.y
        val dz = secondPoint.z - firstPoint.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun isCollinear(
        firstPoint: PoseLandmark,
        secondPoint: PoseLandmark,
        thirdPoint: PoseLandmark,
        tolerance: Float = 15f
    ): Boolean {
        val angle = calculateAngle(firstPoint, secondPoint, thirdPoint)
        return abs(180f - angle) < tolerance
    }

    fun calculateSlope(
        firstPoint: PoseLandmark,
        secondPoint: PoseLandmark
    ): Float {
        val dx = secondPoint.x - firstPoint.x
        val dy = secondPoint.y - firstPoint.y
        return if (dx!=0f) dy / dx else Float.MAX_VALUE
    }
}