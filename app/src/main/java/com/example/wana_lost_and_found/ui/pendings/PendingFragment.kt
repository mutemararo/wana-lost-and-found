package com.example.wana_lost_and_found.ui.pendings

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wana_lost_and_found.R

class PendingFragment : Fragment() {

    companion object {
        fun newInstance() = PendingFragment()
    }

    private lateinit var viewModel: PendingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(PendingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}