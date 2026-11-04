package com.example.pozu.data.model


data class PoseLandmark (val x: Float, val y: Float, val z: Float, val visibility: Float = 1.0f) {
    fun isVisible(): Boolean = visibility > 0.5f
    companion object {
        const val NOSE = "NOSE"
        const val LEFT_EYE = "LEFT_EYE"
        const val RIGHT_EYE = "RIGHT_EYE"
        const val LEFT_EAR = "LEFT_EAR"
        const val RIGHT_EAR = "RIGHT_EAR"
        const val LEFT_SHOULDER = "LEFT_SHOULDER"
        const val RIGHT_SHOULDER = "RIGHT_SHOULDER"
        const val LEFT_ELBOW = "LEFT_ELBOW"
        const val RIGHT_ELBOW = "RIGHT_ELBOW"
        const val LEFT_WRIST = "LEFT_WRIST"
        const val RIGHT_WRIST = "RIGHT_WRIST"
        const val LEFT_HIP = "LEFT_HIP"
        const val RIGHT_HIP = "RIGHT_HIP"
        const val LEFT_KNEE = "LEFT_KNEE"
        const val RIGHT_KNEE = "RIGHT_KNEE"
        const val LEFT_ANKLE = "LEFT_ANKLE"
        const val RIGHT_ANKLE = "RIGHT_ANKLE"
    }
}


