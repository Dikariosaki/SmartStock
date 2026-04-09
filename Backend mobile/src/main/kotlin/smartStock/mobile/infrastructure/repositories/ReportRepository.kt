package smartStock.mobile.infrastructure.repositories

import org.springframework.stereotype.Repository
import smartStock.mobile.application.interfaces.IReportRepository
import smartStock.mobile.domain.entities.Report
import java.util.Optional

@Repository
class ReportRepository(private val jpaRepository: ReportJpaRepository) : IReportRepository {
    override fun findAll(): List<Report> = jpaRepository.findAll()

    override fun findById(id: Int): Optional<Report> = jpaRepository.findById(id)

    override fun save(report: Report): Report = jpaRepository.save(report)

    override fun deleteById(id: Int) = jpaRepository.deleteById(id)
}
