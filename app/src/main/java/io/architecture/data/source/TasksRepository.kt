package io.architecture.data.source

import io.architecture.data.Task

class TasksRepository private constructor(
    private val remoteDataSource: TasksDataSource,
    private val localDataSource: TasksDataSource
) : TasksDataSource {

    internal val cachedTasks = LinkedHashMap<String, Task>()
    private var isCacheIsDirty = false

    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        if (cachedTasks.isNotEmpty() && !isCacheIsDirty) {
            callback.onTaskLoaded(cachedTasks.values.toList())
            return
        }
        if (isCacheIsDirty) {
            getTasksFromRemoteDataSource(callback)
        } else {
            localDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {

                override fun onTaskLoaded(tasks: List<Task>) {
                    refreshCache(tasks)
                    callback.onTaskLoaded(cachedTasks.values.toList())
                }

                override fun onDataNotAvailable() {
                    getTasksFromRemoteDataSource(callback)
                }
            })
        }
    }

    private fun getTasksFromRemoteDataSource(callback: TasksDataSource.LoadTasksCallback) {
        remoteDataSource.getTasks(object : TasksDataSource.LoadTasksCallback {

            override fun onTaskLoaded(tasks: List<Task>) {
                refreshCache(tasks)
                refreshLocalDataSource(tasks)
                callback.onTaskLoaded(tasks)
            }

            override fun onDataNotAvailable() {
                callback.onDataNotAvailable()
            }
        })
    }
    private fun refreshLocalDataSource(tasks: List<Task>) {
        localDataSource.deleteAllTasks()
        tasks.forEach {
            localDataSource.saveTask(it)
        }
    }

    private fun refreshCache(tasks: List<Task>) {
        cachedTasks.clear()
        tasks.forEach {
            cachedTasks[it.id] = it
        }
        isCacheIsDirty = false
    }

    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        val cachedTask = getTaskWithId(taskId)
        if (cachedTask != null) {
            callback.onTaskLoaded(cachedTask)
            return
        }
        localDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {

            override fun onTaskLoaded(task: Task) {
                cachedTasks[taskId] = task
                callback.onTaskLoaded(task)
            }

            override fun onDataNotAvailable() {
                remoteDataSource.getTask(taskId, object : TasksDataSource.GetTaskCallback {
                    override fun onTaskLoaded(task: Task) {
                        cachedTasks[taskId] = task
                        callback.onTaskLoaded(task)
                    }

                    override fun onDataNotAvailable() {
                        callback.onDataNotAvailable()
                    }
                })
            }
        })
    }

    private fun getTaskWithId(taskId: String) =
        cachedTasks[taskId]

    override fun saveTask(task: Task) {
        localDataSource.saveTask(task)
        remoteDataSource.saveTask(task)
        cachedTasks[task.id] = task
    }

    override fun completeTask(task: Task) {
        localDataSource.saveTask(task)
        remoteDataSource.saveTask(task)
        cachedTasks[task.id] = task.copy(isCompleted = true)
    }

    override fun completeTask(taskId: String) {
        completeTask(getTaskWithId(taskId)!!)
    }

    override fun activateTask(task: Task) {
        localDataSource.activateTask(task)
        remoteDataSource.activateTask(task)
        cachedTasks[task.id] = task.copy(isCompleted = false)
    }

    override fun activateTask(taskId: String) {
        activateTask(getTaskWithId(taskId)!!)
    }

    override fun clearCompletedTasks() {
        localDataSource.clearCompletedTasks()
        remoteDataSource.clearCompletedTasks()
        val it = cachedTasks.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) {
                it.remove()
            }
        }
    }

    override fun refreshTask() {
        isCacheIsDirty = true
    }

    override fun deleteAllTasks() {
        localDataSource.deleteAllTasks()
        remoteDataSource.deleteAllTasks()
        cachedTasks.clear()
    }

    override fun deleteTask(taskId: String) {
        localDataSource.deleteTask(taskId)
        remoteDataSource.deleteTask(taskId)
        cachedTasks.remove(taskId)
    }

    companion object {

        @Volatile
        private var INSTANCE: TasksRepository? = null

        fun newInstance(
            remoteDataSource: TasksDataSource,
            localDataSource: TasksDataSource
        ): TasksRepository {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = TasksRepository(remoteDataSource, localDataSource)
                    }
                }
            }
            return INSTANCE!!
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}