package com.example.pozu.ui.result

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pozu.databinding.ActivityResultBinding
import com.example.pozu.data.model.WorkoutSession
import com.example.pozu.data.repository.WorkoutRepository
import com.example.pozu.ui.home.HomeActivity
import com.example.pozu.utils.Constants
import com.example.pozu.utils.showToast
import com.example.pozu.utils.toTimeString

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var repository: WorkoutRepository
    private var workoutSession: WorkoutSession? = null
    private var isSaved = false

    companion object{
        private const val TAG = Constants.TAG_RESULT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: ResultActivity created")

        repository = WorkoutRepository.getInstance()
        workoutSession = intent.getParcelableExtra(Constants.EXTRA_WORKOUT_SESSION)
        workoutSession?.let { session ->
            Log.d(TAG, "\"Received session: ${session.exercise.name}, Reps: ${session.totalReps}, Duration: ${session.duration}")
            displayResults(session)
            setUpClickListeners()
        } ?: run {
            showToast("Error: Workout session not found")
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                workoutSession?.let {
                    if (!isSaved) {
                        saveToHistory()
                    }
                }
                navigateToHome()
            }
        })
    }

    private fun displayResults(session: WorkoutSession) {
        with(binding){
            tvExerciseName.text = session.exercise.name
            tvTotalReps.text = session.totalReps.toString()
            tvDuration.text = session.duration.toTimeString()
            tvFeedbackMessage.text = session.getFeedbackMessage()

            if (session.targetReps != null || session.targetDuration != null){
                cardTargetAchievement.visibility = View.VISIBLE
                tvTargetAchievement.apply {
                    text = if (session.isTargetAchieved()) {
                        "Target Achieved!"
                    } else {
                        "Target not reached. Keep Trying!"
                    }
                }

                val bgColor = if (session.isTargetAchieved()) {
                    com.example.pozu.R.color.feedback_correct
                } else {
                    com.example.pozu.R.color.feedback_warning
                }
                cardTargetAchievement.setCardBackgroundColor(
                    resources.getColor(bgColor, null)
                )
            } else {
                cardTargetAchievement.visibility = View.GONE
            }

            tvCompletionMessage.text = when {
                session.totalReps == 0 -> "Workout Ended"
                session.totalReps >= 20 -> "Amazing Workout!"
                session.totalReps >= 10 -> "Great Job!"
                else -> "Good Start! Keep going!"
            }
        }

        Log.d(TAG, "Results displayed successfully")
    }

    private fun setUpClickListeners() {
        binding.btnSaveToHistory.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            saveToHistory()
        }

        binding.btnBackToHome.setOnClickListener {
            Log.d(TAG,"Back button clicked")
            navigateToHome()
        }
    }

    private fun saveToHistory(){
        val session = workoutSession ?: return
        if (isSaved) {
            showToast("Already saved to history")
            return
        }
        session.also {
            Log.d(TAG, "Saving session to history: ${it.exercise.name}")
        }

        repository.addSession(session)
        isSaved = true

        binding.btnSaveToHistory.apply {
            text = "Saved to History"
            isEnabled = false
        }

        showToast("Workout saved to history!")
        Log.d(TAG, "Session saved successfully")
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}
