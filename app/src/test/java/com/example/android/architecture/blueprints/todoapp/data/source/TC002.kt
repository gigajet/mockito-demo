package com.example.android.architecture.blueprints.todoapp.data.source

import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runBlockingTest
import net.bytebuddy.agent.builder.AgentBuilder
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any

class TC002 {
    private val task1 = Task("old task", "this old task should be deleted when forceUpdate")
    private val task2 = Task("up-to-date task", "both local and remote")
    private val task3 = Task("new task", "second new task")
    private val remoteTasks = listOf(task2, task3).sortedBy { it.id }
    // We need to mutate them
    private val localTasks = listOf(task1, task2).sortedBy { it.id }.toMutableList()
    private val supposedTasksToReturn = listOf(task2, task3).sortedBy { it.id }
    private lateinit var tasksRemoteDataSource: TasksDataSource
    private lateinit var tasksLocalDataSource: TasksDataSource

    // Class under test
    private lateinit var tasksRepository: DefaultTasksRepository

    @Before
    fun setUp() = runBlockingTest {
        // Define our mock without creating new class
        tasksRemoteDataSource = Mockito.mock(TasksDataSource::class.java)
        tasksLocalDataSource = Mockito.mock(TasksDataSource::class.java)
        Mockito.`when`(tasksRemoteDataSource.getTasks()).thenReturn(Result.Success(remoteTasks))
        Mockito.`when`(tasksLocalDataSource.getTasks()).thenReturn(Result.Success(localTasks))
        Mockito.`when`(tasksLocalDataSource.deleteAllTasks()).then { localTasks.clear() }
        Mockito.`when`(tasksLocalDataSource.saveTask(any())).then {
            localTasks.add(it.getArgument<Task>(0))
        }

        tasksRepository = DefaultTasksRepository(
            tasksRemoteDataSource,
            tasksLocalDataSource,
            Dispatchers.Unconfined
        )
    }

    @Test
    fun GetTaskFromNetwork() = runBlockingTest {
        /// WHEN
        val tasks = tasksRepository.getTasks(forceUpdate = true) as Result.Success

        /// THEN
        assertThat(tasks.data, IsEqual(supposedTasksToReturn))
        val `local tasks` = tasksLocalDataSource.getTasks() as Result.Success
        assertThat(`local tasks`.data, IsEqual(remoteTasks))
    }
}