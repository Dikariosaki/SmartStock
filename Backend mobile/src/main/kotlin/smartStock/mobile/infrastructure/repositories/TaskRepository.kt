package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.ITaskRepository
import smartStock.mobile.domain.entities.Task
import java.util.Optional

@Repository
class TaskRepository(private val jpaRepository: TaskJpaRepository) : ITaskRepository {
    override fun findAll(): List<Task> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Task> = jpaRepository.findById(id)

    override fun findByAssignedTo(userId: Int): List<Task> = jpaRepository.findByAssignedTo(userId)

    override fun save(task: Task): Task = jpaRepository.save(task)

    override fun deleteById(id: Int) = jpaRepository.deleteById(id)
}
