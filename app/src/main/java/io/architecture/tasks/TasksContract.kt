package io.architecture.tasks

import io.architecture.BasePresenter
import io.architecture.BaseView
import io.architecture.data.Task

interface TasksContract {

    interface View : BaseView<Presenter> {
        fun setLoadingIndicator(isActive: Boolean)
        fun showTasks(tasks: List<Task>)
        fun showAddTask()
        fun showTaskDetailUi()
        fun showTaskMarkedCompleted()
        fun showTaskMarkedActive()
        fun showCompletedTasksCleared()
        fun showLoadingTaskError()
        fun showNoTasks()
        fun showActiveFilterLabel()
        fun showCompletedFilterLabel()
        fun showAllFilterLabel()
        fun showNoActiveTasks()
        fun showNoCompletedTasks()
        fun showSuccessfullySavedMessage()
        fun isActive(): Boolean
        fun showFilteringPopUpMenu()
    }

    interface Presenter : BasePresenter {
        fun result(requestCode: Int, resultCode: Int)
        fun loadTasks(isForceUpdate: Boolean)
        fun addNewTask()
        fun openTaskDetail(task: Task)
        fun completeTask(task: Task)
        fun activeTask(task: Task)
        fun clearCompletedTask()

    }

}