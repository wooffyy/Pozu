package com.example.pozu.ui.history

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pozu.data.model.WorkoutSession
import com.example.pozu.data.repository.WorkoutRepository
import com.example.pozu.databinding.ActivityHistoryBinding
import com.example.pozu.utils.Constants
import com.example.pozu.utils.toDateString
import com.example.pozu.utils.toTimeString

/**
 * History Activity
 * Displays list of completed workout sessions
 */
class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var repository: WorkoutRepository
    private lateinit var historyAdapter: HistoryAdapter

    companion object {
        private const val TAG = Constants.TAG_HISTORY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: HistoryActivity created")

        // Setup toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Initialize repository
        repository = WorkoutRepository.getInstance()

        // Load history
        loadHistory()
    }

    /**
     * Load workout history from repository
     * Demonstrates COLLECTIONS usage
     */
    private fun loadHistory() {
        // COLLECTIONS: Get sessions sorted by date (newest first)
        val sessions = repository.getSessionDate()

        Log.d(TAG, "Loaded ${sessions.size} workout sessions")

        if (sessions.isEmpty()) {
            // Show empty state
            showEmptyState()
        } else {
            // Show history list
            showHistoryList(sessions)
        }
    }

    /**
     * Show empty state when no history
     */
    private fun showEmptyState() {
        binding.rvHistory.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        Log.d(TAG, "No history found - showing empty state")
    }

    /**
     * Show history list with RecyclerView
     */
    private fun showHistoryList(sessions: List<WorkoutSession>) {
        binding.rvHistory.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE

        // Setup adapter
        historyAdapter = HistoryAdapter(sessions) { session ->
            onSessionClicked(session)
        }

        // SCOPE FUNCTION: with - configure RecyclerView
        with(binding.rvHistory) {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            setHasFixedSize(false)
        }

        Log.d(TAG, "History list displayed with ${sessions.size} items")
    }

    /**
     * Handle session item click
     * Shows detailed dialog
     */
    private fun onSessionClicked(session: WorkoutSession) {
        Log.d(TAG, "Session clicked: ${session.exercise.name}")

        // Build detail message
        // SCOPE FUNCTION: run - build string
        val message = buildString {
            append("Exercise: ${session.exercise.name}\n\n")
            append("Date: ${session.timestamp.toDateString()}\n\n")
            append("Total Reps: ${session.totalReps}\n")
            append("Valid Reps: ${session.validReps}\n")
            append("Duration: ${session.duration.toTimeString()}\n")
            append("Accuracy: ${session.getFormattedAccuracy()}\n\n")

            if (session.targetReps != null) {
                append("Target Reps: ${session.targetReps}\n")
                append("Achieved: ${if (session.isTargetAchieved()) "Yes ✓" else "No"}\n\n")
            }

            if (session.targetDuration != null) {
                append("Target Duration: ${session.targetDuration?.toTimeString()}\n")
                append("Achieved: ${if (session.isTargetAchieved()) "Yes ✓" else "No"}\n\n")
            }

            append("Feedback: ${session.getFeedbackMessage()}")
        }

        // Show dialog
        AlertDialog.Builder(this)
            .setTitle("Workout Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    // LIFECYCLE LOGGING
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")

        // Refresh history in case new workouts were added
        loadHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}