package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Task
import java.util.Optional

interface ITaskRepository {
    fun findAll(): List<Task>

    fun findById(id: Int): Optional<Task>

    fun findByAssignedTo(userId: Int): List<Task>

    fun save(task: Task): Task

    fun deleteById(id: Int)
}
