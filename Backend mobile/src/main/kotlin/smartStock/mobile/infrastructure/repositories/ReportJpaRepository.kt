package smartStock.mobile.infrastructure.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import smartStock.mobile.domain.entities.Report

@Repository
interface ReportJpaRepository : JpaRepository<Report, Int>
