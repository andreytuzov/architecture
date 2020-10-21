package io.architecture.data.source

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.architecture.data.Task
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test

class TasksRepositoryTest {

    private lateinit var tasksRepository: TasksRepository

    private val tasksRemoteDataSource: TasksDataSource = mock()
    private val tasksLocalDataSource: TasksDataSource = mock()
    private val loadTasksCallback: TasksDataSource.LoadTasksCallback = mock()

    private val tasksCallbackCaptor =
        argumentCaptor<TasksDataSource.LoadTasksCallback>()

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
        val task = Task.create(TASK_TITLE1, "Some task description")
        tasksRepository.saveTask(task)
        verify(tasksRemoteDataSource).saveTask(any())
        verify(tasksLocalDataSource).saveTask(any())
        assertEquals(tasksRepository.cachedTasks.size, 1)
    }


    private fun twoTasksLoadCallsToRepository(callback: TasksDataSource.LoadTasksCallback) {
        tasksRepository.getTasks(callback)

        verify(tasksLocalDataSource).getTasks(tasksCallbackCaptor.capture())

        tasksCallbackCaptor.firstValue.onDataNotAvailable()

        verify(tasksRemoteDataSource).getTasks(tasksCallbackCaptor.capture())

        tasksCallbackCaptor.firstValue.onTaskLoaded(TASKS)

        tasksRepository.getTasks(callback)
    }

    companion object {

        private const val TASK_TITLE1 = "title1"
        private const val TASK_TITLE2 = "title2"
        private const val TASK_TITLE3 = "title3"

        private val TASKS = mutableListOf(
            Task.create("Title1", "Description1"),
            Task.create("Title2", "Description2")
        )
    }
}