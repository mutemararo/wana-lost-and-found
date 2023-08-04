package com.wanalnf.wana_lost_and_found.ui.pendings

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.wanalnf.wana_lost_and_found.R
import com.wanalnf.wana_lost_and_found.databinding.PendingItemviewBinding
import com.wanalnf.wana_lost_and_found.model.PendingType
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.model.ReportType
import com.wanalnf.wana_lost_and_found.utils.TimeCalculations

class PendingsAdapter(val context: Context, val reportsList: MutableList<Report>, val pendingType: PendingType, val onButtonClick: (reportId: String, reportedBy: String,  reportType: String) -> Unit) :
    ListAdapter<Report, PendingsAdapter.PendingsViewHolder>(DiffCallback){

    inner class PendingsViewHolder(val binding: PendingItemviewBinding):
            ViewHolder(binding.root){
                fun bind(report: Report){

                    binding.run {
                        pendingItemTitle.text = report.itemName
                        pendingItemTime.text = when (pendingType) {
                            PendingType.CLAIMS -> context.getString(
                                R.string.pending_claim_time_text, TimeCalculations().calculateTimeBetweenDates(report.dateResponded))
                            PendingType.SUBMISSIONS -> context.getString(R.string.pending_submission_time_text, TimeCalculations().calculateTimeBetweenDates(report.dateReported))
                            else -> context.getString(R.string.pending_collection_time_text, TimeCalculations().calculateTimeBetweenDates(report.dateResponded))
                        }

                        pendingItemImage.apply {
                            Glide.with(context)
                                .load(report.itemImage)
                                .placeholder(ContextCompat.getDrawable(context, R.drawable.loading_animation))
                                .into(this)
                        }
                    }
                }
            }

    companion object{
        private val DiffCallback = object : DiffUtil.ItemCallback<Report>(){
            override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.reportID == newItem.reportID
            }

            override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.itemName == newItem.itemName &&
                        oldItem.itemImage == newItem.itemImage &&
                        oldItem.dateReported == newItem.dateReported &&
                        oldItem.description == newItem.description
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingsViewHolder {
        val itemView = PendingItemviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PendingsViewHolder, position: Int) {
        val reportItem = reportsList[position]
        holder.bind(reportItem)
        holder.binding.pendingItemButtonSeeReport.setOnClickListener {
            when(pendingType){
                PendingType.CLAIMS ->{
                    onButtonClick(reportItem.responseTo, reportItem.reportID, ReportType.FOUND.identity)
                }
                PendingType.SUBMISSIONS ->{
                    onButtonClick(reportItem.reportID, reportItem.reportedBy, reportItem.reportType)
                }
                PendingType.COLLECTIONS ->{
                    onButtonClick(reportItem.reportID, reportItem.reportedBy, reportItem.reportType)
                }
            }
        }
    }
}