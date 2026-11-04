package com.example.pozu.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pozu.data.model.WorkoutSession
import com.example.pozu.databinding.ItemHistoryBinding
import com.example.pozu.utils.toDateString
import com.example.pozu.utils.toTimeString

class HistoryAdapter(
    private val sessions: List<WorkoutSession>,
    private val onSessionClick: (WorkoutSession) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: WorkoutSession) {
            // SCOPE FUNCTION: with - multiple operations
            with(binding) {
                // Exercise info
                tvExerciseName.text = session.exercise.name
                ivExerciseIcon.setImageResource(session.exercise.iconRes)

                // Date
                tvDate.text = session.timestamp.toDateString()

                // Stats
                tvReps.text = "${session.totalReps} reps"
                tvDuration.text = session.duration.toTimeString()

                // Target achievement badge
                if (session.targetReps != null || session.targetDuration != null) {
                    tvTargetBadge.visibility = if (session.isTargetAchieved()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                } else {
                    tvTargetBadge.visibility = View.GONE
                }

                // Click listener
                root.setOnClickListener {
                    onSessionClick(session)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(sessions[position])
    }

    override fun getItemCount(): Int = sessions.size
}