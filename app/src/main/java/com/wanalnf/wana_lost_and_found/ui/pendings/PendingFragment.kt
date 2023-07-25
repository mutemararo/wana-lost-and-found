package com.wanalnf.wana_lost_and_found.ui.pendings

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wanalnf.wana_lost_and_found.R
import com.wanalnf.wana_lost_and_found.databinding.FragmentPendingBinding
import com.wanalnf.wana_lost_and_found.model.PendingType
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.model.ReportStatus
import com.wanalnf.wana_lost_and_found.model.ReportType
import com.wanalnf.wana_lost_and_found.ui.add_view_report.ViewReportActivity
import com.wanalnf.wana_lost_and_found.utils.REPORTED_BY_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_STATUS_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingFragment : Fragment() {

    private lateinit var binding: FragmentPendingBinding
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference

    private lateinit var listAdapter : PendingsAdapter
    private var pendingsList = mutableListOf<Report>()
    private var pendingType = PendingType.CLAIMS
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializer()

        binding.run {
            claimsChip.setOnClickListener{
                pendingType = PendingType.CLAIMS
                showPendingClaimsList(pendingType)
            }
            submissionsChip.setOnClickListener {
                pendingType = PendingType.SUBMISSIONS
                showPendingSubmissionsList(pendingType)
            }
            collectionsChip.setOnClickListener {
                pendingType = PendingType.COLLECTIONS
                showPendingCollectionsList(pendingType)
            }
        }

    }

    private fun initializer(){
        showPendingClaimsList(pendingType)
    }

    fun showProgress(){
        if (binding.pendingProgress.visibility == View.GONE)
            binding.pendingProgress.visibility = View.VISIBLE
    }

    fun hideProgress(){
        if (binding.pendingProgress.visibility == View.VISIBLE)
            binding.pendingProgress.visibility = View.GONE
    }

    private fun showEmptyListView(word: String){
        binding.run {
            noPendingText.visibility = View.VISIBLE
            noPendingText.text = activity?.getString(R.string.no_pending_items, word)
            noPendingImage.visibility = View.VISIBLE
            pendingsList.visibility = View.GONE
            pendingSectionDescription.visibility = View.GONE
        }
    }

    private fun showListView(){
        binding.run {
            noPendingText.visibility = View.GONE
            noPendingImage.visibility = View.GONE
            pendingsList.visibility = View.VISIBLE
            pendingSectionDescription.visibility = View.VISIBLE
        }
    }

    private fun showPendingClaimsList(pendingType: PendingType){
        showProgress()
        pendingsList = mutableListOf()
        listAdapter = PendingsAdapter(requireContext(), pendingsList, pendingType){reportId, reportedBy, reportType ->
            val claimIntent = Intent(requireContext(), ViewReportActivity::class.java)
            claimIntent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
            claimIntent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
            claimIntent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
            requireActivity().startActivity(claimIntent)
        }
        binding.pendingsList.adapter = listAdapter
        databaseReference
            .child(getString(R.string.reports_database_node))
            .orderByChild("reportedBy")
            .equalTo(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children){
                        val myReport = item.getValue(Report::class.java)
                        if (myReport?.reportStatus == ReportStatus.FOUND.status){
                            pendingsList.add(myReport)
                        }
                    }
                    if (pendingsList.isEmpty()){
                        showEmptyListView("claim")
                        hideProgress()
                    }else{
                        listAdapter.submitList(pendingsList)
                        showListView()
                        binding.pendingSectionDescription.text = activity?.getString(R.string.claims_description)
                        hideProgress()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun showPendingSubmissionsList(pendingType: PendingType){
        showProgress()
        pendingsList = mutableListOf()
        listAdapter = PendingsAdapter(requireContext(), pendingsList, pendingType){reportId, reportedBy, reportType ->
            val claimIntent = Intent(requireContext(), ViewReportActivity::class.java)
            claimIntent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
            claimIntent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
            claimIntent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
            requireActivity().startActivity(claimIntent)
        }
        binding.pendingsList.adapter = listAdapter
        databaseReference
            .child(getString(R.string.reports_database_node))
            .orderByChild("reportedBy")
            .equalTo(firebaseAuth.currentUser?.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children){
                        val report = item.getValue(Report::class.java)
                        if (report?.reportType == ReportType.FOUND.identity && !report.submitted){
                            pendingsList.add(report)
                        }
                    }

                    if (pendingsList.isEmpty()){
                        showEmptyListView("submit")
                        hideProgress()
                    }else{
                        listAdapter.submitList(pendingsList)
                        showListView()
                        binding.pendingSectionDescription.text = activity!!.getString(R.string.submissions_description)
                        hideProgress()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun showPendingCollectionsList(pendingType: PendingType){
        showProgress()
        pendingsList = mutableListOf()
        listAdapter = PendingsAdapter(requireContext(), pendingsList, pendingType){reportId, reportedBy, reportType ->
            val claimIntent = Intent(requireContext(), ViewReportActivity::class.java)
            claimIntent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
            claimIntent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
            claimIntent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
            claimIntent.putExtra(REPORT_STATUS_EXTRA_KEY, ReportStatus.CLAIMED.status)
            startActivity(claimIntent)
        }
        binding.pendingsList.adapter = listAdapter
        databaseReference
            .child(getString(R.string.reports_database_node))
            .orderByChild("responseTo")
            .equalTo(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children){
                        val report = item.getValue(Report::class.java)
                        if (report?.reportStatus == ReportStatus.CLAIMED.status){
                            pendingsList.add(report)
                        }
                    }
                    if (pendingsList.isEmpty()){
                        showEmptyListView("collect")
                        hideProgress()
                    }else{
                        listAdapter.submitList(pendingsList)
                        showListView()
                        binding.pendingSectionDescription.text = activity?.getString(R.string.collections_description)
                        hideProgress()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}


