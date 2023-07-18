package com.example.wana_lost_and_found.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.wana_lost_and_found.R
import com.example.wana_lost_and_found.databinding.FragmentHomeBinding
import com.example.wana_lost_and_found.model.Report
import com.example.wana_lost_and_found.ui.add_view_report.ViewReportActivity
import com.example.wana_lost_and_found.utils.REPORTED_BY_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.example.wana_lost_and_found.utils.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private lateinit var binding : FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listOfReports = mutableListOf<Report>()
        val listAdapter = MyReportsAdapter(requireActivity(), listOfReports){reportId, reportedBy, reportType->
            val intent = Intent(requireContext(), ViewReportActivity::class.java)
            intent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
            intent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
            intent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
            requireActivity().startActivity(intent)
        }
        binding.simpleHomeList.adapter = listAdapter

        databaseRef
            .child(getString(R.string.reports_database_node))
            .orderByChild("reportedBy")
            .equalTo(firebaseAuth.currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (report in snapshot.children){
                        val item = report.getValue(Report::class.java)

                        item?.let { listOfReports.add(it) }
                    }
                    if(listOfReports.isEmpty()){
                        showEmptyFolder()
                        hideProgress()
                    }else{
                        listAdapter.submitList(listOfReports)
                        hideProgress()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    makeToast(requireContext(), "Check connection")
                }

            })
    }

    private fun showProgress(){
        if(binding.progressHomeList.visibility == View.GONE)
            binding.progressHomeList.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        if (binding.progressHomeList.visibility == View.VISIBLE)
            binding.progressHomeList.visibility = View.GONE
    }

    private fun showEmptyFolder(){
        binding.textNoReports.visibility = View.VISIBLE
        binding.imageEmptyFolder.visibility = View.VISIBLE
        binding.simpleHomeList.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}