package com.wanalnf.wana_lost_and_found.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.wanalnf.wana_lost_and_found.R
import com.wanalnf.wana_lost_and_found.databinding.MyReportItemLayoutBinding
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.utils.TimeCalculations

class MyReportsAdapter(val context: Context, var reportList: MutableList<Report>,
        var onItemClick:(reportId: String, reportedBy: String, reportType: String) -> Unit) : ListAdapter<Report, MyReportsAdapter.SimpleAdapterViewHolder>(DiffCallBack){

    inner class SimpleAdapterViewHolder(val binding: MyReportItemLayoutBinding):
    RecyclerView.ViewHolder(binding.root){
        fun binder(report: Report){
            binding.run {
                myItemName.text = report.itemName
                myItemReportDate.text = TimeCalculations().calculateTimeBetweenDates(report.dateReported)
                myItemLocation.text =
                    String.format(context.resources.getString(R.string.my_report_city_and_country),
                        report.city, report.country)
                myItemCategory.text = report.reportType
                myItemImage.run {
                    Glide.with(context)
                        .load(report.itemImage)
                        .centerCrop()
                        .transform(RoundedCorners(15))
                        .placeholder(ContextCompat.getDrawable(context, R.drawable.loading_animation))
                        .into(this)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleAdapterViewHolder {
        val item = MyReportItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SimpleAdapterViewHolder(item)
    }

    override fun onBindViewHolder(holder: SimpleAdapterViewHolder, position: Int) {
        val report = reportList[position]
        holder.binder(report)
        holder.itemView.setOnClickListener {
            onItemClick(report.reportID, report.reportedBy, report.reportType)
        }
    }

    companion object{

        private val DiffCallBack = object : DiffUtil.ItemCallback<Report>(){
            override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
                return oldItem.reportID == newItem.reportID
            }

            override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
                return newItem.itemName == oldItem.itemName &&
                        newItem.dateReported == oldItem.dateReported
            }

        }
    }
}