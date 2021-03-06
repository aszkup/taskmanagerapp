package com.example.taskmanagerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskmanagerapp.repository.task.TaskRepository
import com.example.taskmanagerapp.utils.WorkManagerUtils
import com.example.taskmanagerapp.viewmodel.main.TasksViewModel
import com.example.taskmanagerapp.viewmodel.newtask.CreateTaskViewModel
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

/**
 * View model factory
 */
class ViewModelFactory(override val kodein: Kodein)
    : ViewModelProvider.NewInstanceFactory(), KodeinAware {

    private val taskRepository: TaskRepository by instance()
    private val workManagerUtils: WorkManagerUtils by instance()

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
            with(modelClass) {
                when {
                    isAssignableFrom(TasksViewModel::class.java) ->
                        TasksViewModel(taskRepository, workManagerUtils)
                    isAssignableFrom(CreateTaskViewModel::class.java) ->
                        CreateTaskViewModel(taskRepository)
                    else ->
                        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            } as T
}
