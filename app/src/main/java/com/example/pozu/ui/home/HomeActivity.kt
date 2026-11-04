package com.example.pozu.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pozu.data.model.Exercise
import com.example.pozu.data.repository.WorkoutRepository
import com.example.pozu.ui.detail.ExerciseDetailActivity
import com.example.pozu.ui.history.HistoryActivity
import com.example.pozu.utils.Constants
import com.example.pozu.utils.showToast
import com.example.pozu.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var repository: WorkoutRepository
    private lateinit var exerciseAdapter: ExerciseAdapter

    companion object {
        private const val TAG = Constants.TAG_HOME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate: HomeActivity created")
        repository = WorkoutRepository.getInstance()

        setupUI()
        setupRecyclerView()
        setupClickListeners()
    }
    private fun setupUI(){
        binding.toolbar.apply {
            setSupportActionBar(this)
        }
    }

    private fun setupRecyclerView(){
        val exercises = repository.getAllExercise()
        Log.d(TAG, "Loaded ${exercises.size} exercises")

        exerciseAdapter = ExerciseAdapter(exercises) {
            exercise -> onExerciseClicked(exercise)
        }

        with(binding.rvExercises) {
            adapter = exerciseAdapter
            layoutManager = LinearLayoutManager(this@HomeActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners(){
        binding.btnHistory.setOnClickListener {
            Log.d(TAG, "History button clicked")
            navigateToHistory()
        }
    }

    private fun onExerciseClicked(exercise: Exercise){
        Log.d(TAG, "Exercise clicked: ${exercise.name}")
        exercise.also { showToast("Selected: ${it.name}") }

        val intent = Intent(this, ExerciseDetailActivity::class.java).apply {
            putExtra(Constants.EXTRA_EXERCISE, exercise)
        }
        startActivity(intent)
    }
    private fun navigateToHistory(){
        val intent = Intent(this, HistoryActivity::class.java)
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

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }
}