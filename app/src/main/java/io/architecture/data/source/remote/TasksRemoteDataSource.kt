package io.architecture.data.source.remote

import android.os.Handler
import io.architecture.data.Task
import io.architecture.data.source.TasksDataSource

class TasksRemoteDataSource private constructor() : TasksDataSource {

    override fun getTasks(callback: TasksDataSource.LoadTasksCallback) {
        execute {
            callback.onTaskLoaded(TASKS_SERVICE_DATA.values.toList())
        }
    }

    private fun execute(block: () -> Unit) {
        Handler().postDelayed({
            block()
        }, SERVICE_LATENCY_IN_MILLIS)
    }

    override fun getTask(taskId: String, callback: TasksDataSource.GetTaskCallback) {
        execute {
            val task = TASKS_SERVICE_DATA[taskId]!!
            callback.onTaskLoaded(task)
        }
    }

    override fun saveTask(task: Task) {
       TASKS_SERVICE_DATA[task.id] = task
    }

    override fun completeTask(task: Task) {
        val completedTask = task.copy(isCompleted = true)
        TASKS_SERVICE_DATA[completedTask.id] = completedTask
    }

    override fun completeTask(taskId: String) {
        // Not required
    }

    override fun activateTask(task: Task) {
        val activateTask = task.copy(isCompleted = false)
        TASKS_SERVICE_DATA[activateTask.id] = activateTask
    }

    override fun activateTask(taskId: String) {
        // Not required
    }

    override fun clearCompletedTasks() {
        val it = TASKS_SERVICE_DATA.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry.value.isCompleted) it.remove()
        }
    }

    override fun refreshTask() {
        // Not required
    }

    override fun deleteAllTasks() {
        TASKS_SERVICE_DATA.clear()
    }

    override fun deleteTask(taskId: String) {
        TASKS_SERVICE_DATA.remove(taskId)
    }

    companion object {

        private const val SERVICE_LATENCY_IN_MILLIS = 5000L

        @Volatile
        private var INSTANCE: TasksRemoteDataSource? = null

        private val TASKS_SERVICE_DATA = LinkedHashMap<String, Task>()

        init {
            addTask("Build tower in Pisa", "Ground looks good, no foundation work required.")
            addTask("Finish bridge in Tacoma", "Found awesome girders at half the cost!")
        }

        private fun addTask(title: String, description: String) {
            val task = Task.create(title, description)
            TASKS_SERVICE_DATA[task.id] = task
        }

        fun newInstance(): TasksRemoteDataSource {
            if (INSTANCE == null) {
                synchronized(this) {
                    if (INSTANCE == null) {
                        INSTANCE = TasksRemoteDataSource()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}