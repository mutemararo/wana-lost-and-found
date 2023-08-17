package com.wanalnf.wana_lost_and_found.data

import com.wanalnf.wana_lost_and_found.model.Report

interface WanaRepository {

    fun getMyReports(): MutableList<Report>

    fun getGlobalLostReports(): List<Report>
    fun getGlobalFoundReports(): List<Report>
    fun getReportWithId(reportId: String): Report
    fun uploadReportToDatabase(report: Report): Boolean

}