package com.example.wana_lost_and_found.ui.reports

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wana_lost_and_found.R
import com.example.wana_lost_and_found.databinding.FragmentFoundReportsBinding
import com.example.wana_lost_and_found.model.Report
import com.example.wana_lost_and_found.ui.add_view_report.ActivityAddReport
import com.example.wana_lost_and_found.ui.add_view_report.ViewReportActivity
import com.example.wana_lost_and_found.utils.REPORTED_BY_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.example.wana_lost_and_found.utils.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FoundReportsFragment : Fragment() {


    private lateinit var binding: FragmentFoundReportsBinding

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val foundItemsList = mutableListOf<Report>()
    private lateinit var firebaseAuthStateListener: AuthStateListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFoundReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listAdapter = ReportsListAdapter(requireContext(), foundItemsList){reportId, reportedBy, reportType ->
            val intent = Intent(requireActivity(), ViewReportActivity::class.java)
            intent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
            intent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
            intent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
            requireActivity().startActivity(intent)
        }
        binding.foundItemsList.adapter = listAdapter

        databaseReference
            .child(getString(R.string.reports_database_node))
            .orderByChild("reportType")
            .equalTo("found")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (report in snapshot.children){
                        val reportItem = report.getValue(Report::class.java)

                        reportItem?.let { foundItemsList.add(it) }
                    }
                    if (foundItemsList.isEmpty()){
                        hideProgress()
                        showEmptyList()
                    }else{
                        hideProgress()
                        listAdapter.submitList(foundItemsList)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    makeToast(requireContext(), "failed to fetch items")
                    showEmptyList()
                    hideProgress()
                }

            })
    }

    private fun showProgress(){
        if (binding.progressFoundItems.visibility == View.GONE)
            binding.progressFoundItems.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        if (binding.progressFoundItems.visibility == View.VISIBLE)
            binding.progressFoundItems.visibility = View.GONE
    }

    private fun showEmptyList(){
        binding.imageEmptyFoundRepository.visibility = View.VISIBLE
        binding.noFoundItemTexts.visibility = View.VISIBLE
        binding.foundItemsList.visibility = View.GONE
    }


}