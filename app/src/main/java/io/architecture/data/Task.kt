package io.architecture.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "tasks")
data class Task(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean
) {

    val titleForList: String
        get() = if (title.isEmpty()) description
        else title

    val isActive: Boolean
        get() = !isCompleted

    companion object {

        fun create(
            title: String,
            description: String,
            isCompleted: Boolean = false
        ) = Task(UUID.randomUUID().toString(), title, description, isCompleted)
    }
}