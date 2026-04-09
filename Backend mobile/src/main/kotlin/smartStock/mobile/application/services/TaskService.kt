package smartStock.mobile.application.services

import org.springframework.stereotype.Service
import smartStock.mobile.application.dtos.TaskCreateRequest
import smartStock.mobile.application.dtos.TaskResponse
import smartStock.mobile.application.dtos.TaskUpdateRequest
import smartStock.mobile.application.interfaces.ITaskRepository
import smartStock.mobile.application.interfaces.IUserRepository
import smartStock.mobile.domain.entities.Task
import java.time.LocalDateTime

@Service
class TaskService(
    private val taskRepository: ITaskRepository,
    private val userRepository: IUserRepository,
) {
    fun getAll(): List<TaskResponse> {
        val usersById =
            userRepository.findAll()
                .associateBy { it.id }

        return taskRepository.findAll().map { task ->
            val assignedName = task.assignedTo?.let { usersById[it]?.name }
            task.toResponse(assignedName)
        }
    }

    fun getById(id: Int): TaskResponse? {
        return taskRepository.findById(id).map { task ->
            val assignedName = task.assignedTo?.let { assignedTo ->
                userRepository.findById(assignedTo).map { it.name }.orElse(null)
            }
            task.toResponse(assignedName)
        }.orElse(null)
    }

    fun getByUserId(userId: Int): List<TaskResponse> {
        val usersById =
            userRepository.findAll()
                .associateBy { it.id }
        return taskRepository.findByAssignedTo(userId).map { task ->
            val assignedName = task.assignedTo?.let { usersById[it]?.name }
            task.toResponse(assignedName)
        }
    }

    fun create(request: TaskCreateRequest): TaskResponse {
        val task =
            Task(
                title = request.title,
                description = request.description,
                assignedTo = request.assignedTo,
                status = true,
            )
        val saved = taskRepository.save(task)
        val assignedName = saved.assignedTo?.let { userRepository.findById(it).map { user -> user.name }.orElse(null) }
        return saved.toResponse(assignedName)
    }

    fun update(
        id: Int,
        request: TaskUpdateRequest,
    ): TaskResponse {
        val existingTask = taskRepository.findById(id).orElseThrow { RuntimeException("Tarea no encontrada") }

        val updatedTask =
            existingTask.copy(
                title = request.title,
                description = request.description,
                assignedTo = request.assignedTo,
                status = request.status,
                finishedAt = if (!request.status && existingTask.status) LocalDateTime.now() else existingTask.finishedAt,
            )
        val savedTask = taskRepository.save(updatedTask)
        val assignedName = savedTask.assignedTo?.let { userRepository.findById(it).map { user -> user.name }.orElse(null) }
        return savedTask.toResponse(assignedName)
    }

    fun activate(id: Int) {
        val existingTask = taskRepository.findById(id).orElseThrow { RuntimeException("Tarea no encontrada") }
        val updatedTask = existingTask.copy(status = true)
        taskRepository.save(updatedTask)
    }

    fun deactivate(id: Int) {
        val existingTask = taskRepository.findById(id).orElseThrow { RuntimeException("Tarea no encontrada") }
        val updatedTask = existingTask.copy(status = false, finishedAt = LocalDateTime.now())
        taskRepository.save(updatedTask)
    }

    fun delete(id: Int) {
        if (taskRepository.findById(id).isPresent) {
            taskRepository.deleteById(id)
        } else {
            throw RuntimeException("Tarea no encontrada")
        }
    }

    private fun Task.toResponse(assignedToName: String?) =
        TaskResponse(
            id = id,
            title = title,
            description = description,
            assignedTo = assignedTo,
            assignedToName = assignedToName,
            createdAt = createdAt ?: LocalDateTime.now(),
            finishedAt = finishedAt,
            status = status,
        )
}
