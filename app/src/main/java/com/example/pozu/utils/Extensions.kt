package com.example.pozu.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toTimeString(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / 1000) / 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun Float.toDegrees(): Float {
    return Math.toDegrees(this.toDouble()).toFloat()
}

fun Float.toRadians(): Float {
    return Math.toRadians(this.toDouble()).toFloat()
}

fun Float.clamp(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun String?.isValid(): Boolean {
    return !this.isNullOrEmpty()
}

fun String.safeSubstring(startIndex: Int, endIndex: Int): String {
    if (startIndex >= this.length || endIndex > this.length || startIndex >= endIndex) {
        return ""
    }
    return this.substring(startIndex, endIndex)
}