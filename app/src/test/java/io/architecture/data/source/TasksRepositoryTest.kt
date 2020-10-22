package io.architecture.data.source

import com.nhaarman.mockitokotlin2.*
import io.architecture.data.Task
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TasksRepositoryTest {

    private lateinit var tasksRepository: TasksRepository

    private val tasksRemoteDataSource: TasksDataSource = mock()
    private val tasksLocalDataSource: TasksDataSource = mock()
    private val loadTasksCallback: TasksDataSource.LoadTasksCallback = mock()
    private val getTasksCallback: TasksDataSource.GetTaskCallback = mock()

    private val tasksCallbackCaptor = argumentCaptor<TasksDataSource.LoadTasksCallback>()

    private val taskCallbackCaptor = argumentCaptor<TasksDataSource.GetTaskCallback>()

    @Before
    fun setupTasksRepository() {
        tasksRepository = TasksRepository.newInstance(tasksRemoteDataSource, tasksLocalDataSource)
    }

    @After
    fun destroyRepositoryInstance() {
        TasksRepository.destroyInstance()
    }

    @Test
    fun getTasks_repositoryCachesAfterFirstApiCall() {

        tasksRepository.getTasks(loadTasksCallback)

        verify(tasksLocalDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onDataNotAvailable()

        verify(tasksRemoteDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onTaskLoaded(TASKS)

        tasksRepository.getTasks(loadTasksCallback)

        verify(tasksLocalDataSource).getTasks(any())
    }

    @Test
    fun getTasks_requestAllTasksFromLocalDataSource() {
        tasksRepository.getTasks(loadTasksCallback)
        verify(tasksLocalDataSource).getTasks(any())
    }

    @Test
    fun saveTask_savesTaskToServiceApi() {
        val task = Task.create(TASK_TITLE, "Some task description")
        tasksRepository.saveTask(task)
        verify(tasksRemoteDataSource).saveTask(any())
        verify(tasksLocalDataSource).saveTask(any())
        assertEquals(tasksRepository.cachedTasks.size, 1)
    }

    @Test
    fun completeTask_completesTaskToServiceApiUpdatesCache() {
        // Given
        val task = Task.create(TASK_TITLE, "Some task description")
        tasksRepository.saveTask(task)

        // When
        tasksRepository.completeTask(task)

        // Then
        verify(tasksLocalDataSource).completeTask(task)
        verify(tasksRemoteDataSource).completeTask(task)
        assertEquals(tasksRepository.cachedTasks.size, 1)
        assertEquals(tasksRepository.cachedTasks[task.id]?.isActive, false)
    }

    @Test
    fun completeTaskId_completesTaskToServiceApiUpdatesCache() {
        // Given
        val task = Task.create(TASK_TITLE, "Some task description")
        tasksRepository.saveTask(task)

        // When
        tasksRepository.completeTask(task.id)

        // Then
        verify(tasksLocalDataSource).completeTask(task)
        verify(tasksRemoteDataSource).completeTask(task)
        assertEquals(tasksRepository.cachedTasks.size, 1)
        assertEquals(tasksRepository.cachedTasks[task.id]?.isActive, false)
    }

    @Test
    fun activeTask_activesTaskToServiceApiUpdatesCache() {
        // Given
        val task = Task.create(TASK_TITLE, "Some task description", true)
        tasksRepository.saveTask(task)

        // When
        tasksRepository.activateTask(task)

        // Then
        verify(tasksLocalDataSource).activateTask(task)
        verify(tasksRemoteDataSource).activateTask(task)
        assertEquals(tasksRepository.cachedTasks.size, 1)
        assertEquals(tasksRepository.cachedTasks[task.id]?.isActive, true)
    }

    @Test
    fun activeTaskId_activesTaskToServiceApiUpdatesCache() {
        // Given
        val task = Task.create(TASK_TITLE, "Some task description", true)
        tasksRepository.saveTask(task)

        // When
        tasksRepository.activateTask(task.id)

        // Then
        verify(tasksLocalDataSource).activateTask(task)
        verify(tasksRemoteDataSource).activateTask(task)
        assertEquals(tasksRepository.cachedTasks.size, 1)
        assertEquals(tasksRepository.cachedTasks[task.id]?.isActive, true)
    }

    @Test
    fun getTask_requestSingleTaskForLocalDataSource() {
        // When
        tasksRepository.getTask(TASK_TITLE, getTasksCallback)

        // Then
        verify(tasksLocalDataSource).getTask(eq(TASK_TITLE), any())
    }

    @Test
    fun deleteCompletedTasks_deleteCompletedTasksToServiceAPIUpdatesCache() {
        // Given
        val task1 = Task.create(TASK_TITLE, "Some description")
        val task2 = Task.create(TASK_TITLE2, "Some description2", true)
        val task3 = Task.create(TASK_TITLE3, "Some description3", true)
        tasksRepository.saveTask(task1)
        tasksRepository.saveTask(task2)
        tasksRepository.saveTask(task3)

        // When
        tasksRepository.clearCompletedTasks()

        // Then
        verify(tasksLocalDataSource).clearCompletedTasks()
        verify(tasksRemoteDataSource).clearCompletedTasks()
        assertEquals(1, tasksRepository.cachedTasks.size)
        assertEquals(true, tasksRepository.cachedTasks[task1.id]?.isActive)
        assertEquals(TASK_TITLE, tasksRepository.cachedTasks[task1.id]?.title)
    }

    @Test
    fun deleteAllTasks_deleteTasksToServiceAPIUpdatesCache() {
        // Given
        val task1 = Task.create(TASK_TITLE, "Some description")
        val task2 = Task.create(TASK_TITLE2, "Some description2", true)
        val task3 = Task.create(TASK_TITLE3, "Some description3", true)
        tasksRepository.saveTask(task1)
        tasksRepository.saveTask(task2)
        tasksRepository.saveTask(task3)

        // When
        tasksRepository.deleteAllTasks()

        // Then
        verify(tasksLocalDataSource).deleteAllTasks()
        verify(tasksRemoteDataSource).deleteAllTasks()
        assertEquals(0, tasksRepository.cachedTasks.size)
    }

    @Test
    fun deleteTask_deleteTaskToServiceAPIRemovedFromCache() {
        // Given
        val task1 = Task.create(TASK_TITLE, "Some description")
        val task2 = Task.create(TASK_TITLE2, "Some description2", true)
        val task3 = Task.create(TASK_TITLE3, "Some description3", true)
        tasksRepository.saveTask(task1)
        tasksRepository.saveTask(task2)
        tasksRepository.saveTask(task3)

        // When
        tasksRepository.deleteTask(task2.id)

        // Then
        verify(tasksLocalDataSource).deleteTask(task2.id)
        verify(tasksRemoteDataSource).deleteTask(task2.id)
        assertEquals(false, tasksRepository.cachedTasks.containsKey(task2.id))
    }

    @Test
    fun getTasksWithDirtyCache_tasksAreRetrievedFromRemote() {
        // When
        tasksRepository.refreshTask()
        tasksRepository.getTasks(loadTasksCallback)

        verify(tasksRemoteDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onTaskLoaded(TASKS)

        // Then
        verify(tasksLocalDataSource, never()).getTasks(any())
        verify(loadTasksCallback).onTaskLoaded(TASKS)
    }

    @Test
    fun getTasksWithLocalDataSourceUnavailable_tasksAreRetrievedFromRemote() {
        // When
        tasksRepository.getTasks(loadTasksCallback)

        verify(tasksLocalDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onDataNotAvailable()

        verify(tasksRemoteDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onTaskLoaded(TASKS)

        // Then
        verify(loadTasksCallback).onTaskLoaded(TASKS)
    }

    @Test
    fun getTasksWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When
        tasksRepository.getTasks(loadTasksCallback)

        verify(tasksLocalDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onDataNotAvailable()

        verify(tasksRemoteDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onDataNotAvailable()

        // Then
        verify(loadTasksCallback).onDataNotAvailable()
    }

    @Test
    fun getTaskWithBothDataSourcesUnavailable_firesOnDataUnavailable() {
        // When
        tasksRepository.getTask(TASK_TITLE, getTasksCallback)

        verify(tasksLocalDataSource).getTask(eq(TASK_TITLE), taskCallbackCaptor.capture())
        taskCallbackCaptor.lastValue.onDataNotAvailable()

        verify(tasksRemoteDataSource).getTask(eq(TASK_TITLE), taskCallbackCaptor.capture())
        taskCallbackCaptor.lastValue.onDataNotAvailable()

        // Then
        verify(getTasksCallback).onDataNotAvailable()
    }

    @Test
    fun getTasks_refreshesLocalDataSource() {
        // Given
        tasksRepository.refreshTask()

        // When
        tasksRepository.getTasks(loadTasksCallback)

        verify(tasksRemoteDataSource).getTasks(tasksCallbackCaptor.capture())
        tasksCallbackCaptor.lastValue.onTaskLoaded(TASKS)

        // Then
        verify(tasksLocalDataSource, times(TASKS.size)).saveTask(any())
    }

    companion object {

        private const val TASK_TITLE = "title1"
        private const val TASK_TITLE2 = "title2"
        private const val TASK_TITLE3 = "title3"

        private val TASKS = mutableListOf(
            Task.create("Title1", "Description1"),
            Task.create("Title2", "Description2")
        )
    }
}