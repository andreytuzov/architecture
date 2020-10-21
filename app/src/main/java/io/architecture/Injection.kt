package io.architecture

import android.content.Context
import io.architecture.data.source.TasksRepository
import io.architecture.data.source.local.TasksLocalDataSource
import io.architecture.data.source.local.ToDoDatabase
import io.architecture.data.source.remote.TasksRemoteDataSource
import io.architecture.util.AppExecutors

class Injection {

    fun provideTasksRepository(context: Context): TasksRepository {
        val database = ToDoDatabase.getInstance(context)
        return TasksRepository.newInstance(
            remoteDataSource = TasksRemoteDataSource.newInstance(),
            localDataSource = TasksLocalDataSource.newInstance(AppExecutors.create(), database.taskDao())
        )
    }
}