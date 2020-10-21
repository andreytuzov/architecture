package io.architecture.data.source.local

import androidx.room.*
import io.architecture.data.Task

@Dao
interface TasksDao {

    @Query("SELECT * FROM tasks")
    fun getTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: Task)

    @Update
    fun updateTask(task: Task)

    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE id = :taskId")
    fun updateCompleted(taskId: String, isCompleted: Boolean)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    fun deleteTaskById(taskId: String)

    @Query("DELETE FROM tasks")
    fun deleteTasks()

    @Query("DELETE FROM TASKS WHERE is_completed = 1")
    fun deleteCompletedTasks()
}