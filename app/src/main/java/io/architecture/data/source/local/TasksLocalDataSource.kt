package io.architecture.data.source.local

import io.architecture.data.Task
import io.architecture.data.source.TasksDataSource
import io.architecture.util.AppExecutors

class TasksLocalDataSource private constructor(
    private val appExecutors: AppExecutors,
    private val tasksDao: TasksDao
): TasksDataSource {

    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        appExecutors.diskIO.execute {
            val tasks = tasksDao.getTasks()
            appExecutors.mainThread.execute {
                if (tasks.isEmpty()) callback.onDataNotAvailable()
                else callback.onTaskLoaded(tasks)
            }
        }
    }

    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        appExecutors.diskIO.execute {
            val task = tasksDao.getTaskById(taskId)
            appExecutors.mainThread.execute {
                if (task == null) callback.onDataNotAvailable()
                else callback.onTaskLoaded(task)
            }
        }
    }

    override fun saveTask(task: Task) {
        appExecutors.diskIO.execute {
            tasksDao.insertTask(task)
        }
    }

    override fun completeTask(task: Task) {
        appExecutors.diskIO.execute {
            tasksDao.updateCompleted(task.id, true)
        }
    }

    override fun completeTask(taskId: String) {
        // Not required
    }

    override fun activateTask(task: Task) {
        appExecutors.diskIO.execute {
            tasksDao.updateCompleted(task.id, false)
        }
    }

    override fun activateTask(taskId: String) {
        // Not required
    }

    override fun clearCompletedTasks() {
        appExecutors.diskIO.execute {
            tasksDao.deleteCompletedTasks()
        }
    }

    override fun refreshTask() {
        // Not required
    }

    override fun deleteAllTasks() {
        appExecutors.diskIO.execute {
            tasksDao.deleteTasks()
        }
    }

    override fun deleteTask(taskId: String) {
        appExecutors.diskIO.execute {
            tasksDao.deleteTaskById(taskId)
        }
    }

    companion object {

        @Volatile
        private var INSTANCE: TasksLocalDataSource? = null

        fun newInstance(appExecutors: AppExecutors, tasksDao: TasksDao): TasksLocalDataSource {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = TasksLocalDataSource(appExecutors, tasksDao)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}