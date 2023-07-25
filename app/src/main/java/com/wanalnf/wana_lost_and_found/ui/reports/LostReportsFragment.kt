package com.wanalnf.wana_lost_and_found.ui.reports

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wanalnf.wana_lost_and_found.R
import com.wanalnf.wana_lost_and_found.databinding.FragmentLostReportsBinding
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.ui.add_view_report.ViewReportActivity
import com.wanalnf.wana_lost_and_found.utils.REPORTED_BY_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.makeToast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LostReportsFragment : Fragment() {

    private lateinit var binding: FragmentLostReportsBinding

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val lostItemsList = mutableListOf<Report>()
    private lateinit var listAdapter: ReportsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLostReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = ReportsListAdapter(requireActivity(), lostItemsList){reportId, reportedBy, reportType ->
            val viewIntent = Intent(requireActivity(), ViewReportActivity::class.java)
            viewIntent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
            viewIntent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
            viewIntent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
            requireActivity().startActivity(viewIntent)

        }
        binding.lostItemsList.adapter = listAdapter

        databaseReference
            .child(getString(R.string.reports_database_node))
            .orderByChild("reportType")
            .equalTo("lost")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (report in snapshot.children){
                        val reportItem = report.getValue(Report::class.java)

                        reportItem?.let { lostItemsList.add(it) }
                    }

                    if (lostItemsList.isEmpty()){
                        hideProgress()
                        showEmptyList()
                    }else{
                        hideProgress()
                        listAdapter.submitList(lostItemsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showEmptyList()
                    hideProgress()
                }

            })
    }

    private fun showProgress(){
        if (binding.progressLostItems.visibility == View.GONE)
            binding.progressLostItems.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        if (binding.progressLostItems.visibility == View.VISIBLE)
            binding.progressLostItems.visibility = View.GONE
    }

    private fun showEmptyList(){
        binding.imageEmptyLostRepository.visibility = View.VISIBLE
        binding.noLostItemTexts.visibility = View.VISIBLE
        binding.lostItemsList.visibility = View.GONE
    }

}