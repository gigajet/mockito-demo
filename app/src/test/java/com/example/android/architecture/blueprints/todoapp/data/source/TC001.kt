package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import javax.sql.DataSource

class TC001 {
    private val task1 = Task("old task", "Description1")
    private val task2 = Task("old task 2", "Description2")
    private val task3 = Task("new task", "Description3")
    private val remoteTasks = listOf(task1, task2, task3).sortedBy { it.id }
    private val localTasks = listOf(task1, task2).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: TasksDataSource
    private lateinit var tasksLocalDataSource: TasksDataSource

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    @Before
    fun createRepository() {
        // .toMutableList() returns a copy, not the original list.
        tasksRemoteDataSource = FakeDataSource(remoteTasks.toMutableList())
        tasksLocalDataSource = FakeDataSource(localTasks.toMutableList())
        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource,
            tasksLocalDataSource,
            Dispatchers.Unconfined
        )
    }

    @Test
    fun getAllTasksFromRemoteSource() = runBlockingTest {
        /// WHEN
        val tasks = tasksRepository.getTasks(forceUpdate = true) as Result.Success

        /// THEN
        assertThat(tasks.data, IsEqual(remoteTasks))
    }
}