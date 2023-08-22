package com.wanalnf.wana_lost_and_found.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.wanalnf.wana_lost_and_found.model.Report


class FirebaseRepository(val firebaseAuth: FirebaseAuth, private val databaseReference: DatabaseReference):
    WanaRepository {
    override suspend fun getMyReports(): MutableList<Report> {

        val listOfReports = mutableListOf<Report>()
        databaseReference
            .child("reports")
            .orderByChild("reportedBy")
            .equalTo(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (report in snapshot.children){
                        val item = report.getValue(Report::class.java)

                        item?.let { listOfReports.add(it) }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        return listOfReports
    }

    override fun getGlobalLostReports(): List<Report> {
        TODO("Not yet implemented")
    }

    override fun getGlobalFoundReports(): List<Report> {
        TODO("Not yet implemented")
    }

    override fun getReportWithId(reportId: String): Report {
        TODO("Not yet implemented")
    }

    override fun uploadReportToDatabase(report: Report): Boolean {
        TODO("Not yet implemented")
    }
}