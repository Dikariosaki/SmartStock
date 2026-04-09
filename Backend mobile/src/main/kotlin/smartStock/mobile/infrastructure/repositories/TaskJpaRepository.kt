package smartStock.mobile.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import smartStock.mobile.domain.entities.Task

@Repository
interface TaskJpaRepository : JpaRepository<Task, Int> {
    fun findByAssignedTo(userId: Int): List<Task>
}
