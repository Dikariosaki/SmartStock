package smartStock.mobile.application.interfaces

import smartStock.mobile.domain.entities.Report
import java.util.Optional

interface IReportRepository {
    fun findAll(): List<Report>

    fun findById(id: Int): Optional<Report>

    fun save(report: Report): Report

    fun deleteById(id: Int)
}
