package com.wanalnf.wana_lost_and_found.ui.reports

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.wanalnf.wana_lost_and_found.R
import com.wanalnf.wana_lost_and_found.databinding.ReportItemViewBinding
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.utils.TimeCalculations

class ReportsListAdapter(val context: Context, val reportsList: MutableList<Report>,
        var onItemClick: (reportId: String, reportedBy: String, reportType: String) -> Unit):
    ListAdapter<Report, ReportsListAdapter.ReportsListViewHolder>(DiffCallback) {

    inner class ReportsListViewHolder(val binding: ReportItemViewBinding):
            ViewHolder(binding.root){
                fun bind(report: Report){
                    binding.run {
                        reportItemDate.text = TimeCalculations().calculateTimeBetweenDates(report.dateReported)
                        reportItemName.text = report.itemName
                        reportItemLocation.text = context.getString(R.string.report_city_and_country, report.city, report.country)
                        reportItemImage.apply {
                            Glide.with(context)
                                .load(report.itemImage)
                                .placeholder(ContextCompat.getDrawable(context, R.drawable.icon_hourglass))
                                .into(this)
                        }

                    }

                }
            }



    companion object{

        private val DiffCallback = object : DiffUtil.ItemCallback<Report>(){
            override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.reportID ==
                        newItem.reportID
            }

            override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.itemName == newItem.itemName &&
                        oldItem.itemImage == newItem.itemImage &&
                        oldItem.dateReported == newItem.dateReported &&
                        oldItem.description == newItem.description
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportsListViewHolder {
        val view = ReportItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportsListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportsListViewHolder, position: Int) {
        val report = reportsList[position]
        holder.bind(report)
        holder.itemView.setOnClickListener {
            onItemClick(report.reportID, report.reportedBy, report.reportType)
        }
    }
}