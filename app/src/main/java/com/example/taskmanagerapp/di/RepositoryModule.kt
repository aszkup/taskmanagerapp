package com.example.taskmanagerapp.di

import androidx.work.WorkManager
import com.example.taskmanagerapp.repository.AppDatabase
import com.example.taskmanagerapp.repository.notification.NotificationHelper
import com.example.taskmanagerapp.repository.task.TaskDao
import com.example.taskmanagerapp.repository.task.TaskRepository
import com.example.taskmanagerapp.utils.FileUtils
import com.example.taskmanagerapp.utils.WorkManagerUtils
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

val repositoryModule = Kodein.Module {
    bind<AppDatabase>() with singleton { AppDatabase.getInstance(instance()) }
    bind<TaskDao>() with singleton { (instance() as AppDatabase).taskDao() }
    bind<TaskRepository>() with singleton { TaskRepository(instance()) }

    bind<WorkManager>() with singleton { WorkManager.getInstance() }
    bind<WorkManagerUtils>() with singleton { WorkManagerUtils(instance()) }

    bind<FileUtils>() with singleton { FileUtils(instance()) }
    bind<NotificationHelper>() with provider { NotificationHelper(instance()) }
}
