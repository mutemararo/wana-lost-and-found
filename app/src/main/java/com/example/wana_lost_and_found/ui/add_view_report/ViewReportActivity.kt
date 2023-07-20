package com.example.wana_lost_and_found.ui.add_view_report

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.wana_lost_and_found.R
import com.example.wana_lost_and_found.databinding.ActivityViewReportBinding
import com.example.wana_lost_and_found.model.Report
import com.example.wana_lost_and_found.model.ReportStatus
import com.example.wana_lost_and_found.model.ReportType
import com.example.wana_lost_and_found.utils.REPORTED_BY_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_STATUS_EXTRA_KEY
import com.example.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.example.wana_lost_and_found.utils.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date


class ViewReportActivity : AppCompatActivity() {

    private val binding : ActivityViewReportBinding by lazy {
        ActivityViewReportBinding.inflate(layoutInflater)
    }
    private lateinit var receivingIntent : Intent

    private val databaseReference = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val storageReference = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        receivingIntent = intent
        initialize()
    }

    private fun initialize(){
        when(receivingIntent.getStringExtra(REPORTED_BY_EXTRA_KEY)) {
            firebaseAuth.currentUser?.uid ->{
                setUpActivityForMyReportViewing()
                displayItem()
            }
            else -> {
                setUpActivityForGeneralView()
                displayItem()
            }
        }

    }

    private fun setUpActivityForGeneralView() {
        showGeneralViewActionButtom()
        if(receivingIntent.getStringExtra(REPORT_TYPE_EXTRA_KEY) == ReportType.LOST.identity){
            setUpActivityForLostReport()
            setUpButtonFunctionalityForGeneralView(ReportType.LOST.identity)
        }else{
            setUpButtonFunctionalityForGeneralView(ReportType.FOUND.identity)
        }
    }

    private fun setUpButtonFunctionalityForGeneralView(reportType: String) {
        binding.run {
            if (reportType == ReportType.LOST.identity){
                buttonAction.text = getString(R.string.view_general_lost_report_action_button)
                buttonAction.setOnClickListener {
                    val dialogBuilder = MaterialAlertDialogBuilder(this@ViewReportActivity)
                        .setTitle("Found this item?")
                        .setMessage("If you found this item add a report to help the owner verify it\n\nContinue?")
                        .setNegativeButton("No"
                        ) { _, _ ->

                        }
                        .setPositiveButton("Yes"){_, _ ->
                            val responseIntent = Intent(this@ViewReportActivity, ActivityAddReport::class.java)
                            responseIntent.putExtra(REPORT_ID_EXTRA_KEY, receivingIntent.getStringExtra(
                                REPORT_ID_EXTRA_KEY))
                            responseIntent.putExtra(REPORT_STATUS_EXTRA_KEY, ReportStatus.FOUND.status)
                            responseIntent.putExtra(REPORT_TYPE_EXTRA_KEY, ReportType.FOUND.identity)
                            startActivity(responseIntent)
                        }
                        .create()

                    dialogBuilder.show()
                }
            }else{
                if (receivingIntent.getStringExtra(REPORT_STATUS_EXTRA_KEY) == ReportStatus.CLAIMED.status){
                    buttonAction.text = getString(R.string.button_collected)
                    buttonAction.setOnClickListener {
                        val dialogBuilder = MaterialAlertDialogBuilder(this@ViewReportActivity)
                            .setTitle("Collection Update")
                            .setMessage("Have you Collected this item, if so it will be remove from global database\n\nContinue?")
                            .setPositiveButton("Yes"){_, _ ->
                                makeToast(this@ViewReportActivity, "Great")
                            }
                            .setNegativeButton("No"){_, _ ->

                            }
                            .create()
                        dialogBuilder.show()
                    }

                }else{
                    buttonAction.text = getString(R.string.view_general_found_report_action_button)
                    buttonAction.setOnClickListener {
                        val dialogBuilder = MaterialAlertDialogBuilder(this@ViewReportActivity)
                            .setTitle(title)
                            .setMessage("If you are sure this is your item, you must collect it in 7 days at the Authority.\n\nContinue")
                            .setNegativeButton("No"
                            ) { _, _ ->

                            }
                            .setPositiveButton("Yes"){_, _ ->
                                showProgress()
                                //update the found item`s reportStatus variable to claimed (variable is used for pending collections)
                                databaseReference
                                    .child("reports")
                                    .child(receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                                    .child("reportStatus")
                                    .setValue(ReportStatus.CLAIMED.status)
                                    .addOnSuccessListener {
                                        makeToast(this@ViewReportActivity, "Item claimed successfully")

                                    }.addOnFailureListener {
                                        makeToast(this@ViewReportActivity, "Failed to make claim, try again later")
                                    }.addOnCanceledListener {
                                        makeToast(this@ViewReportActivity, "Failed to make claim, try again later")
                                    }

                                //update the found item`s responseTo variable to this@user uid (variable is used for pending collections)
                                databaseReference
                                    .child("reports")
                                    .child(receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                                    .child("responseTo")
                                    .setValue(firebaseAuth.currentUser!!.uid)
                                    .addOnSuccessListener {
                                        makeToast(this@ViewReportActivity, "See pending collections")
                                        hideProgress()
                                    }.addOnFailureListener {
                                        makeToast(this@ViewReportActivity, "Failed to claim item, try later")
                                    }

                                databaseReference
                                    .child("reports")
                                    .child(receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                                    .child("dateResponded")
                                    .setValue(System.currentTimeMillis().toString())
                                    .addOnSuccessListener {
                                        makeToast(this@ViewReportActivity, "See pending collections")
                                        hideProgress()
                                    }.addOnFailureListener {
                                        makeToast(this@ViewReportActivity, "Failed to claim item, try later")
                                    }
                            }
                            .create()
                        dialogBuilder.show()
                    }
                }
            }
        }
    }

    private fun showProgress(){
        if (binding.progressViewReport.visibility == View.GONE)
            binding.progressViewReport.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        if (binding.progressViewReport.visibility == View.VISIBLE)
            binding.progressViewReport.visibility = View.GONE
    }

    private fun showMyViewActionButtons(){
        binding.run {
            buttonActionPositive.visibility = View.VISIBLE
            buttonActionNegative.visibility = View.VISIBLE
        }
    }

    private fun showGeneralViewActionButtom(){
        binding.buttonAction.visibility = View.VISIBLE
    }



    private fun setUpActivityForMyReportViewing() {
        showMyViewActionButtons()
        if(receivingIntent.getStringExtra(REPORT_TYPE_EXTRA_KEY) == ReportType.LOST.identity){
            setUpActivityForLostReport()
            setUpButtonFunctionalityForMyReport()
        }else{
            setUpButtonFunctionalityForMyReport()
        }

    }

    private fun displayItem(){
        if (receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY).isNullOrEmpty()){
            makeToast(this@ViewReportActivity, "report does not exist")
            finish()
        }else{
            databaseReference
                .child(getString(R.string.reports_database_node))
                .child(receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                .addValueEventListener(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val report = snapshot.getValue(Report::class.java)
                        if (report == null){
                            makeToast(this@ViewReportActivity, "report does not exist")
                            finish()
                        }else{
                            val date = Date(report.dateReported.toLong())
                            val dateFormat = SimpleDateFormat("dd MMM yyyy")

                            binding.run {
                                chipReportDate.text = String.format(getString(R.string.view_report_date), dateFormat.format(date))
                                textViewDescription.text = report.description
                                textViewLocation.text = getString(R.string.report_city_and_country, report.city, report.country)
                                textViewTagLabel.text = report.tagLabel
                                reportName.text = report.itemName
                                textViewAuthorityNameAndLocation.text =
                                    getString(R.string.authority_name_and_location_for_view, report.nameOfAuthority, report.locationOfAuthority)
                                textViewAuthorityContact.text = report.contactOfAuthority
                                Glide.with(this@ViewReportActivity)
                                    .load(report.itemImage)
                                    .placeholder(R.drawable.icon_hourglass)
                                    .into(imageViewReportImage)
                            }
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

    }

    private fun setUpActivityForLostReport(){
        binding.authorityViewLayout.visibility = View.GONE
    }

    private fun setUpButtonFunctionalityForMyReport(){
        binding.run {
            buttonActionPositive.text = resources.getString(R.string.view_my_report_positive_button)
            buttonActionNegative.text = resources.getString(R.string.view_my_report_negative_button)

            buttonActionPositive.setOnClickListener {
                val intent = Intent(this@ViewReportActivity, ActivityAddReport::class.java)
                intent.putExtra(REPORT_ID_EXTRA_KEY,
                    receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY))
                intent.putExtra(REPORT_TYPE_EXTRA_KEY,
                    receivingIntent.getStringExtra(REPORT_TYPE_EXTRA_KEY))
                startActivity(intent)
            }
            buttonActionNegative.setOnClickListener {

                val dialogbuilder = MaterialAlertDialogBuilder(this@ViewReportActivity)
                    .setTitle("Deleting Report")
                    .setMessage("Are you sure you want to permanently remove this report?")
                    .setPositiveButton("Yes"){_, _ ->
                        showProgress()
                        databaseReference.child(getString(R.string.reports_database_node))
                            .child(receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                            .addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val report = snapshot.getValue(Report::class.java)
                                    if (report == null){
                                        makeToast(this@ViewReportActivity, "report does not exist")
                                        finish()
                                    }else{
                                        val reportImage = report.itemImage
                                        if (reportImage != "https://firebasestorage.googleapis.com/v0/b/wana-lost-and-found.appspot.com/o/report_images%2Freporters%2Fno_image.jpg?alt=media&token=5c0f116c-5304-4622-baaa-75bfd318268d"){
                                            storageReference.getReferenceFromUrl(reportImage)
                                                .delete()
                                                .addOnSuccessListener {
                                                    makeToast(this@ViewReportActivity, "image deleted")
                                                }
                                                .addOnFailureListener {
                                                    makeToast(this@ViewReportActivity, "image not deleted")
                                                }
                                        }else{
                                            makeToast(this@ViewReportActivity, "deleting..")
                                        }
                                    }


                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
                        databaseReference
                            .child(getString(R.string.reports_database_node))
                            .child(receivingIntent.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                            .removeValue().addOnSuccessListener {
                                makeToast(this@ViewReportActivity, "Report successfully deleted")
                                hideProgress()
                                finish()
                            }.addOnFailureListener {
                                makeToast(this@ViewReportActivity, "Failed to delete")
                                hideProgress()
                            }
                    }
                    .setNegativeButton("No"){_, _ ->

                    }
                    .create()
                dialogbuilder.show()
            }
        }
    }
}