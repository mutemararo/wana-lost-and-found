package com.example.wana_lost_and_found.ui.reports

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.wana_lost_and_found.R
import com.example.wana_lost_and_found.databinding.FragmentReportsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class ReportsFragment : Fragment() {

    companion object {
        fun newInstance() = ReportsFragment()
    }
    private lateinit var pagerAdapter: PagerAdapter
    private lateinit var binding : FragmentReportsBinding

    private lateinit var viewModel: ReportsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pagerAdapter = PagerAdapter(childFragmentManager, lifecycle)
        binding.tabLayout.run {
            addTab(this.newTab().setText("Lost"))
            addTab(this.newTab().setText("Found"))
        }
        binding.viewPager.adapter = pagerAdapter

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    binding.viewPager.currentItem = tab.position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.let {
                    it.selectTab(it.getTabAt(position))
                }
            }
        })
    }

}