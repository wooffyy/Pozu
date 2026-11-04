package com.example.pozu.ui.detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pozu.data.model.Exercise
import com.example.pozu.data.model.JumpingJack
import com.example.pozu.databinding.ActivityExerciseDetailBinding
import com.example.pozu.ui.camera.CameraExerciseActivity
import com.example.pozu.utils.Constants
import com.example.pozu.utils.showToast

class ExerciseDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExerciseDetailBinding
    private var exercise: Exercise? = null
    private var hasTarget = false
    private var targetValue = 0

    companion object {
        private const val TAG = Constants.TAG_DETAIL
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: ExerciseDetailActivity created")

        exercise = intent.getParcelableExtra(Constants.EXTRA_EXERCISE)

        exercise?.let{
            Log.d(TAG, "received exercise: ${it.name}")
            setupUI(it)
            setupClickListeners()
        } ?: run {
            showToast("Exercise not found")
            finish()
        }
    }


    private fun setupUI(exercise: Exercise){
        with(binding){
            toolbar.setNavigationOnClickListener {
                Log.d(TAG, "Back button clicked")
                finish()
            }

            tvExerciseName.text = exercise.name
            tvTargetMuscles.text = exercise.targetMuscles
            tvDescription.text = exercise.description
            ivExerciseIcon.setImageResource(exercise.iconRes)

            val instuctionText = exercise.instructions
                .mapIndexed { index, instruction ->
                    "${index + 1}. $instruction" }
                .joinToString("\n\n")

            tvInstructions.text = instuctionText

            Log.d(TAG, "UI Setup complete for ${exercise.name}")
        }

        setupTargetSection(exercise)
    }

    private fun setupTargetSection(exercise: Exercise){
        binding.apply{
            val isTimerBased = exercise is JumpingJack

            tvTargetLabel.text = if (isTimerBased) {
                "Target Duration (seconds):"
            } else {
                "Target Reps:"
            }

            val defaultValue = when (exercise) {
                is JumpingJack -> "30"
                else -> when (exercise.id) {
                    "pushup" -> Constants.DEFAULT_TARGET_REPS_PUSHUP.toString()
                    "squat" -> Constants.DEFAULT_TARGET_REPS_SQUAT.toString()
                    else -> "10"
                }
            }
            tilTargetValue.hint = defaultValue
            Log.d(TAG, "Target section setip - Timer based: $isTimerBased")

        }
    }

    private fun setupClickListeners(){
        binding.cbSetTarget.setOnCheckedChangeListener { _, isChecked ->
            hasTarget = isChecked
            binding.layoutTargetInput.apply {
                visibility = if(isChecked) View.VISIBLE else View.GONE
            }
            Log.d(TAG, "Set target checkbox: $isChecked")
        }

        binding.btnStartExercise.setOnClickListener {
            Log.d(TAG, "Start exercise button clicked")
            startExercise()
        }
    }

    private fun startExercise(){
        val currentExercise = exercise ?: return

        if (hasTarget){
            val inputText = binding.etTargetValue.text.toString()

            targetValue = inputText.let {
                if (it.isEmpty()) {
                    binding.tilTargetValue.hint.toString().toIntOrNull() ?: 10
                } else {
                    it.toIntOrNull() ?: 0
                }
            }

            if (targetValue <= 0){
                showToast("Please enter a valid target value")
                return
            }

            Log.d(TAG, "Target set to: $targetValue")
        }

        navigateToCameraActivity(currentExercise)
    }

    private fun navigateToCameraActivity(exercise: Exercise){
        val intent = Intent(this, CameraExerciseActivity::class.java).apply {
            putExtra(Constants.EXTRA_EXERCISE, exercise)
            putExtra(Constants.EXTRA_HAS_TARGET, hasTarget)

            if (hasTarget) {
                if (exercise is JumpingJack) {
                    putExtra(Constants.EXTRA_TARGET_DURATION, targetValue * 1000L)
                } else {
                    putExtra(Constants.EXTRA_TARGET_REPS, targetValue)
                }
            }
        }

        intent.also {
            Log.d(TAG, "Starting camera activity for ${exercise.name}")
            Log.d(TAG, "Has target: $hasTarget, Value: $targetValue")
        }

        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}