package com.example.taskmanagerapp.view.main

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmanagerapp.databinding.ItemTaskBinding
import com.example.taskmanagerapp.repository.task.Task
import com.example.taskmanagerapp.utils.FileUtils
import com.example.taskmanagerapp.utils.WorkManagerUtils
import org.threeten.bp.Duration

/**
 * Task [PagedListAdapter]
 */
class TaskListAdapter(
        private val fileUtils: FileUtils,
        private val workManagerUtils: WorkManagerUtils,
        private val clickListener: (Task) -> Unit)
    : PagedListAdapter<Task, TaskListAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TaskViewHolder(binding, fileUtils, workManagerUtils, clickListener)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    /**
     * Get item at position
     * @param position [Int]
     * @return task [Task]
     */
    fun getItemAt(position: Int): Task? = getItem(position)

    inner class TaskViewHolder(
            private val binding: ItemTaskBinding,
            private val fileUtils: FileUtils,
            private val workManagerUtils: WorkManagerUtils,
            private val clickListener: (Task) -> Unit)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.task = task
            binding.fileName = fileUtils.getFileName(Uri.parse(task.fileUri))
            task.finishedAt?.let {
                binding.isFinished = true
                binding.duration = printDuration(Duration.between(task.startedAt, task.finishedAt))
            }
            binding.isInProgress = workManagerUtils.isWorkScheduled(task.id) or
                    workManagerUtils.isWorkRunning(task.id)
            binding.executePendingBindings()
            itemView.setOnClickListener { clickListener(task) }
        }

        private fun printDuration(duration: Duration): String {
            val stringBuilder = StringBuilder()
            val hours = duration.toHours()
            hours.takeIf { it > 0 }?.apply { stringBuilder.append(hours).append("h ") }
            val minutes = duration.toMinutes() % SEC_IN_MINUTES
            minutes.takeIf { it > 0 }?.apply { stringBuilder.append(minutes).append("m ") }
            stringBuilder.append(duration.seconds % SEC_IN_MINUTES).append("s")
            return stringBuilder.toString()
        }
    }

    private class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {

        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        const val SEC_IN_MINUTES = 60
    }
}
