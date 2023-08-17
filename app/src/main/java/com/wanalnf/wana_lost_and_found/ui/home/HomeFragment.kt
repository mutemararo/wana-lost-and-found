package com.wanalnf.wana_lost_and_found.ui.home


import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.wanalnf.wana_lost_and_found.R
import com.wanalnf.wana_lost_and_found.databinding.FragmentHomeBinding
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.ui.add_view_report.ViewReportActivity
import com.wanalnf.wana_lost_and_found.utils.REPORTED_BY_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.makeToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wanalnf.wana_lost_and_found.WanaApplication

const val TAG = "HomeFragment"
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val viewModel : HomeViewModel by activityViewModels {
        HomeViewModelFactory((activity?.application as WanaApplication).container.wanaRepository)
    }
    private lateinit var binding : FragmentHomeBinding
    private lateinit var listOfReports: MutableList<Report>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgress()

        viewModel.myReports.observe(viewLifecycleOwner){
            listOfReports = it

            val listAdapter = MyReportsAdapter(requireActivity(), listOfReports){reportId, reportedBy, reportType->
                val intent = Intent(requireActivity(), ViewReportActivity::class.java)
                intent.putExtra(REPORT_ID_EXTRA_KEY, reportId)
                intent.putExtra(REPORTED_BY_EXTRA_KEY, reportedBy)
                intent.putExtra(REPORT_TYPE_EXTRA_KEY, reportType)
                requireActivity().startActivity(intent)
            }
            binding.simpleHomeList.adapter = listAdapter

            if (isNetworkAvailable()){

                if(listOfReports.isEmpty()){
                    showEmptyFolder()
                    hideProgress()
                }else{
                    listAdapter.submitList(listOfReports)
                    hideProgress()
                }


            }else{
                context?.let { makeToast(it, "no network") }
            }
        }



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

    private fun isNetworkAvailable(): Boolean{
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return if(networkCapabilities == null){
                Log.d(TAG, "Device is offline")
                false
            }else{
                Log.d(TAG, "Device is online")
                true
            }
        }else{
            val activeNetwork = connectivityManager.activeNetworkInfo

            return if (activeNetwork?.isConnectedOrConnecting!!){
                Log.d(TAG, "Device online")
                true
            }else{
                Log.d(TAG, "Device offline")
                false
            }
        }

    }
}