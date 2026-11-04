package com.example.pozu.data.repository

import com.example.pozu.data.model.*

class WorkoutRepository private constructor(){
    private val _sessions = mutableListOf<WorkoutSession>()
    val session: List<WorkoutSession>
        get() = _sessions.toList()

    private val _exerciseCatalog = mutableMapOf<String, Exercise>()
    val exerciseCatalog: Map<String, Exercise>
        get() = _exerciseCatalog.toMap()

    companion object {
        @Volatile
        private var INSTANCE: WorkoutRepository? = null

        fun getInstance(): WorkoutRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WorkoutRepository().also {
                    INSTANCE = it
                    it.initializeExercise()
                }
            }
        }
    }

    private fun initializeExercise() {
        val exercise = listOf(
            PushUp(),
            Squat(),
            JumpingJack()
        )

        exercise.forEach { exercise ->
            _exerciseCatalog[exercise.id] = exercise
        }
    }

    fun addSession(session: WorkoutSession) {
        _sessions.add(0, session)
    }

    fun getAllExercise(): List<Exercise> {
        return _exerciseCatalog.values.toList()
    }

    fun getExerciseById(id: String): Exercise? {
        return _exerciseCatalog[id]
    }

    fun getSessionByExercise(): Map<String, List<WorkoutSession>> {
        return _sessions.groupBy { it.exercise.name }
    }

    fun getSessionDate(): List<WorkoutSession> {
        return _sessions.sortedByDescending { it.timestamp }
    }

    fun getSessionSortByAccuracy(): List<WorkoutSession> {
        return _sessions.sortedByDescending { it.accuracy }
    }

    fun getTotalRepsByExercise(): Map<String, Int> {
        return _sessions.groupBy { it.exercise.name }.mapValues { (_, sessions) ->
            sessions.sumOf { it.totalReps }
        }
    }

    fun getBestSession(): WorkoutSession? {
        return _sessions.maxByOrNull { it.accuracy }
    }

    fun hasCompletedExercise(exerciseName: String): Boolean {
        return _sessions.any { it.exercise.name == exerciseName }
    }

    fun getRecentSession(limit: Int = 10): List<WorkoutSession> {
        return _sessions.take(limit)
    }

    fun clearSessions() {
        _sessions.clear()
    }

    fun getStatSummary(): WorkoutStats {
        return WorkoutStats(
            totalSession = _sessions.size,
            totalReps = _sessions.sumOf { it.totalReps },
            totalDuration = _sessions.sumOf { it.duration },
            averageAccuracy = if (_sessions.isNotEmpty()) {
                _sessions.map { it.accuracy }.average().toFloat()
            } else 0f,
            bestAccuracy = _sessions.maxOfOrNull { it.accuracy } ?: 0f,
            favoriteExercise = getTotalRepsByExercise()
                .maxByOrNull { it.value }?.key ?: "None"
        )
    }

    data class WorkoutStats(
        val totalSession: Int,
        val totalReps: Int,
        val totalDuration: Long,
        val averageAccuracy: Float,
        val bestAccuracy: Float,
        val favoriteExercise: String
    )
}