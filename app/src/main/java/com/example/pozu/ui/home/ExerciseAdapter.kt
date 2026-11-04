package com.example.pozu.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pozu.data.model.Exercise
import com.example.pozu.databinding.ItemExerciseBinding

class ExerciseAdapter (
    private val exercise: List<Exercise>,
    private val onExerciseClick: (Exercise) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(
        private val binding: ItemExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root){

        fun bind(exercise: Exercise) {
            with(binding){
                tvExerciseName.text = exercise.name
                tvTargetMuscles.text = exercise.targetMuscles
                ivExerciseIcon.setImageResource(exercise.iconRes)

                root.setOnClickListener {
                    onExerciseClick(exercise)

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(exercise[position])
    }

    override fun getItemCount(): Int = exercise.size
}