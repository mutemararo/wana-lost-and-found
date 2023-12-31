package com.wanalnf.wana_lost_and_found.ui.add_view_report

import android.Manifest
import android.R
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.wanalnf.wana_lost_and_found.R.*
import com.wanalnf.wana_lost_and_found.databinding.FragmentAddReportBinding
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.model.ReportStatus
import com.wanalnf.wana_lost_and_found.model.ReportType
import com.wanalnf.wana_lost_and_found.utils.REPORT_ID_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_STATUS_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.REPORT_TYPE_EXTRA_KEY
import com.wanalnf.wana_lost_and_found.utils.makeToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet
import java.util.UUID

class ActivityAddReport : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private val binding : FragmentAddReportBinding by lazy{
        FragmentAddReportBinding.inflate(layoutInflater)
    }

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var extraIntent : Intent? = null
    private lateinit var countryName: String

    private var cameraUri : Uri? = null
    private val takePhotoIntent = registerForActivityResult(
        ActivityResultContracts
        .StartActivityForResult()){ result ->

        if (result.resultCode == RESULT_OK){

            binding.editReportImage.apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                visibility = View.VISIBLE
                setImageURI(cameraUri)

            }

        }
    }
    private var galleryUri: Uri? = null
    private val openGalleryIntent = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            val myUri = result.data?.data
            galleryUri = myUri
            Glide.with(this@ActivityAddReport)
                .load(galleryUri)
                .into(binding.editReportImage)
            binding.editReportImage.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }
    private lateinit var spinnerAdapter: ArrayAdapter<String>
    private lateinit var originalImageUrl: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var myUri : Uri? = null

        savedInstanceState?.let {
            try {
                if (it.getString("cameraUri").isNullOrEmpty()){
                    myUri = it.getString("galleryUri")!!.toUri()
                    galleryUri = myUri
                }else{
                    myUri = it.getString("cameraUri")!!.toUri()
                    cameraUri = myUri
                }
            }catch (e: NullPointerException){
                print(e)
            }
            if (myUri != null){

                Glide.with(this@ActivityAddReport)
                    .load(myUri)
                    .into(binding.editReportImage)
            }
        }

        extraIntent = intent

        initializer()

        val locales : SortedSet<String> = TreeSet()

        val countries = Locale.getAvailableLocales()


        for (locale in countries){
            if (!TextUtils.isEmpty(locale.displayCountry)){
                locales.add(locale.displayCountry)
            }
        }

        spinnerAdapter = ArrayAdapter(this@ActivityAddReport,
            R.layout.simple_spinner_dropdown_item, locales.toList())

        binding.run {
            countrySpinner.onItemSelectedListener = this@ActivityAddReport
            countrySpinner.adapter = spinnerAdapter
        }

        binding.editReportImage.setOnClickListener {
            requestPermissions()
        }

        binding.buttonSubmit.setOnClickListener{
            binding.run{
                if (extraIntent?.getStringExtra(REPORT_TYPE_EXTRA_KEY) == ReportType.FOUND.identity){
                    if(cameraUri != null || galleryUri != null) {
                        if (switchSubmittedToAuthority.isChecked) {
                            if (!(editName.text.isNullOrBlank() &&
                                        editCity.text.isNullOrBlank() &&
                                        editDescription.text.isNullOrBlank()) &&
                                !(editAuthorityName.text.isNullOrBlank() &&
                                        editAuthorityLocation.text.isNullOrBlank() &&
                                        editAthorityContact.text.isNullOrBlank())
                            ) {
                                if(cameraUri == null){
                                    CoroutineScope(Dispatchers.Main).launch {
                                        uploadReport(galleryUri)
                                    }
                                }
                                if (galleryUri == null){
                                    CoroutineScope(Dispatchers.Main).launch {
                                        uploadReport(cameraUri)
                                    }
                                }

                            }else{
                                makeToast(this@ActivityAddReport, getString(string.enter_all_fields))
                            }
                        }else{
                            if(!(editName.text.isNullOrBlank() &&
                                        editCity.text.isNullOrBlank() &&
                                        editDescription.text.isNullOrBlank())){
                                CoroutineScope(Dispatchers.Main).launch {
                                    uploadReport(if (cameraUri == null) galleryUri else cameraUri)
                                    makeToast(this@ActivityAddReport,
                                        getString(string.submission_notice_toast))
                                }
                            }else{
                                makeToast(this@ActivityAddReport, getString(string.enter_all_fields))
                            }

                        }
                    }else{
                        makeToast(this@ActivityAddReport,
                            getString(string.take_photo_or_select_image))
                    }
                }else{
                    if (!(editName.text.isNullOrBlank() &&
                                editCity.text.isNullOrBlank() &&
                                editDescription.text.isNullOrBlank())){
                        CoroutineScope(Dispatchers.Main).launch {
                            uploadReport(if (cameraUri == null) galleryUri
                            else if (galleryUri == null) cameraUri else null)
                        }
                    }else{
                        makeToast(this@ActivityAddReport, getString(string.enter_all_fields))
                    }

                }
            }
        }

        binding.buttonCancel.setOnClickListener {
            finish()
        }

    }


    private fun uploadReport(uri: Uri?){
        //=================compression==================
        showProgress()
        if (uri == null){
            val report = Report()
            val downloadUri = getString(string.default_image_url)

            val randomId = UUID.randomUUID().toString()
            uploadToDatabase(report, downloadUri, randomId)
            hideProgress()
            finish()
        }else{
            val baos = ByteArrayOutputStream()
            var bmp: Bitmap? = null
            try{
                bmp = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }catch (e: IOException){
                e.printStackTrace()
            }
            bmp?.compress(Bitmap.CompressFormat.JPEG, 56, baos)

            val fileInBytes = baos.toByteArray()
            //=================end compression===================

            val storageReference = FirebaseStorage.getInstance().reference
                .child("report_images")
                .child("${System.currentTimeMillis()}" + getFileExtension(uri))


            storageReference.putBytes(fileInBytes).addOnSuccessListener {

                storageReference.downloadUrl.addOnSuccessListener {
                    val report = Report()
                    val downloadUri = it.toString()

                    val randomId = UUID.randomUUID().toString()
                    uploadToDatabase(report, downloadUri, randomId)
                    hideProgress()
                    finish()
                }

            }
                .addOnFailureListener{
                    Toast.makeText(this@ActivityAddReport, "failed to upload", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnProgressListener {
                    var progress = 0.0
                    val currentProgress = (100 * it.bytesTransferred / it.totalByteCount)
                    if (currentProgress > (progress + 15)){
                        progress = ((100 * it.bytesTransferred) / it.totalByteCount).toDouble()
                        Toast.makeText(this@ActivityAddReport, "$progress% done", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun showProgress(){
        if(binding.progressAddReport.visibility == View.GONE)
            binding.progressAddReport.visibility = View.VISIBLE
    }
    private fun hideProgress(){
        if(binding.progressAddReport.visibility == View.VISIBLE)
            binding.progressAddReport.visibility = View.GONE
    }
    private fun uploadToDatabase(report: Report, downloadUri: String, randomId: String){
        report.reportID = if(extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY).isNullOrEmpty() || !(extraIntent?.getStringExtra(
                REPORT_STATUS_EXTRA_KEY).isNullOrEmpty())) randomId
                else extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY)!!
        report.reportedBy = firebaseAuth.currentUser!!.uid
        report.itemImage = if(extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY).isNullOrEmpty() || !(extraIntent?.getStringExtra(
                REPORT_STATUS_EXTRA_KEY).isNullOrEmpty())) downloadUri else originalImageUrl
        report.itemName = binding.editName.text?.trim().toString()
        report.description = binding.editDescription.text?.trim().toString()
        report.city = binding.editCity.text?.trim().toString()
        report.country = countryName
        report.submitted = binding.switchSubmittedToAuthority.isChecked
        report.dateReported = if(extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY).isNullOrEmpty() || !(extraIntent?.getStringExtra(
                REPORT_STATUS_EXTRA_KEY).isNullOrEmpty())) System.currentTimeMillis().toString()
                        else report.dateReported
        report.tagLabel = if(binding.switchHasTag.isChecked) binding.editTagLabel.text?.trim().toString() else "no tage"
        report.reportType = extraIntent?.getStringExtra(REPORT_TYPE_EXTRA_KEY)!!
        report.reportStatus = if(report.reportStatus != ReportStatus.REPORTED.status) report.reportStatus else ReportStatus.REPORTED.status
        report.responseTo = if (!(extraIntent?.getStringExtra(REPORT_STATUS_EXTRA_KEY).isNullOrEmpty()))
            extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY)!! else if (report.responseTo.isNotEmpty()) report.responseTo else " "
        report.dateResponded = if (!(extraIntent?.getStringExtra(REPORT_STATUS_EXTRA_KEY).isNullOrEmpty()))
            System.currentTimeMillis().toString() else if (report.dateResponded.isNotEmpty()) report.dateResponded else " "
        report.nameOfAuthority = binding.editAuthorityName.text?.trim().toString()
        report.locationOfAuthority = binding.editAuthorityLocation.text?.trim().toString()
        report.contactOfAuthority = binding.editAthorityContact.text?.trim().toString()

        //changing report status for a lost report being responded to, status == found(formerly reported)
        if (!(extraIntent?.getStringExtra(REPORT_STATUS_EXTRA_KEY).isNullOrEmpty())){
            databaseRef
                .child("reports")
                .child(extraIntent!!.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                .child("reportStatus")
                .setValue(extraIntent?.getStringExtra(REPORT_STATUS_EXTRA_KEY))

            databaseRef
                .child(getString(com.wanalnf.wana_lost_and_found.R.string.reports_database_node))
                .child(extraIntent!!.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                .child("responseTo")
                .setValue(randomId)

            databaseRef
                .child(getString(com.wanalnf.wana_lost_and_found.R.string.reports_database_node))
                .child(extraIntent!!.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                .child("dateResponded")
                .setValue(System.currentTimeMillis().toString())
        }

        databaseRef
            .child("reports")
            .child(randomId)
            .setValue(report)
    }
    private fun getFileExtension(uri: Uri): String?{
        val resolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(resolver.getType(uri))
    }

    private fun launchGallery() {
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        openGalleryIntent.launch(galleryIntent)
    }

    private fun launchCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DESCRIPTION, "taken now")
        values.put(MediaStore.Images.Media.TITLE, "picture")
        cameraUri = this@ActivityAddReport.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        takePhotoIntent.launch(cameraIntent)
    }

    private fun initializer(){

        when(extraIntent?.getStringExtra(REPORT_TYPE_EXTRA_KEY)){
            ReportType.FOUND.identity ->{
                prepareActivityForFoundReport()
            }
            ReportType.LOST.identity ->{
                prepareActivityForLostReport()
            }
        }
        if(!(extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY).isNullOrEmpty()) &&
                extraIntent?.getStringExtra(REPORT_STATUS_EXTRA_KEY).isNullOrEmpty()){

            databaseRef
                .child(getString(string.reports_database_node))
                .child(extraIntent?.getStringExtra(REPORT_ID_EXTRA_KEY)!!)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val report = snapshot.getValue(Report::class.java)

                        when(extraIntent?.getStringExtra(REPORT_TYPE_EXTRA_KEY)){
                            ReportType.LOST.identity ->{
                                populateViewsForLostReport(report)
                                originalImageUrl = report?.itemImage!!
                            }
                            ReportType.FOUND.identity -> {
                                populateViewsForFoundReport(report)
                                originalImageUrl = report?.itemImage!!
                            }
                        }
                        binding.run {
                            buttonSubmit.setOnClickListener {
                                showProgress()
                                uploadToDatabase(report!!, report.itemImage, report.reportID)
                                hideProgress()
                                makeToast(this@ActivityAddReport, "updated report")
                                finish()
                            }
                            buttonCancel.setOnClickListener {
                                finish()
                            }
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    private fun populateViewsForLostReport(report: Report?){
        val spinnerPosition = spinnerAdapter.getPosition(report?.country)
        binding.run {
            switchHasTag.isChecked = true
            layoutEditTagLabel.visibility = View.VISIBLE
            Glide.with(this@ActivityAddReport)
                .load(report?.itemImage)
                .placeholder(ContextCompat.getDrawable(this@ActivityAddReport, drawable.loading_animation))
                .into(editReportImage)
            editName.setText(report?.itemName, TextView.BufferType.SPANNABLE)
            editDescription.setText(report?.description, TextView.BufferType.SPANNABLE)
            editCity.setText(report?.city, TextView.BufferType.SPANNABLE)
            editTagLabel.setText(report?.tagLabel, TextView.BufferType.SPANNABLE)
            countrySpinner.setSelection(spinnerPosition)
        }

    }

    private fun populateViewsForFoundReport(report: Report?){
        populateViewsForLostReport(report)
        if (report!!.submitted){
            binding.apply {
                switchSubmittedToAuthority.isChecked = true
                layoutAuthorityDetails.visibility = View.VISIBLE
                editAuthorityName.setText(report.nameOfAuthority, TextView.BufferType.SPANNABLE)
                editAuthorityLocation.setText(report.locationOfAuthority, TextView.BufferType.SPANNABLE)
                editAthorityContact.setText(report.contactOfAuthority, TextView.BufferType.SPANNABLE)
            }
        }else{
            binding.switchSubmittedToAuthority.isChecked = false
        }
    }

    private fun prepareActivityForLostReport() {
        binding.switchSubmittedToAuthority.visibility = View.GONE
        binding.layoutAuthorityDetails.visibility = View.GONE
        title = "Report Lost Item"
        binding.switchHasTag.setOnClickListener {switch ->
            binding.layoutEditTagLabel.visibility =
                if((switch as Switch).isChecked) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun prepareActivityForFoundReport() {
        title = "Report Item Found"
        binding.switchSubmittedToAuthority.setOnClickListener { switch ->
            binding.layoutAuthorityDetails.visibility =
                if ((switch as Switch).isChecked) View.VISIBLE
                else View.GONE
        }
        binding.switchHasTag.setOnClickListener {switch ->
            binding.layoutEditTagLabel.visibility =
                if((switch as Switch).isChecked) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(cameraUri != null){
            outState.putString("cameraUri", cameraUri.toString())
        }else if (galleryUri != null){
            outState.putString("galleryUri", galleryUri.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val uri = savedInstanceState.getString("imageUri")
        if (cameraUri != null){
            cameraUri = uri?.toUri()
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val country = p0?.getItemAtPosition(p2).toString()
        countryName = country
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    private fun requestPermissions() {
        // below line is use to request permission in the current activity.
        // this method is use to handle error in runtime permissions
        Dexter.withContext(this@ActivityAddReport)
            // below line is use to request the number of permissions which are required in our app.
            .withPermissions(
                Manifest.permission.CAMERA
                // below is the list of permissions
                    )
            // after adding permissions we are calling an with listener method.
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    // this method is called when all permissions are granted
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        // do you work now
                        Toast.makeText(this@ActivityAddReport, "All the permissions are granted..", Toast.LENGTH_SHORT).show()
                        val dialogBuilder = MaterialAlertDialogBuilder(this@ActivityAddReport)
                            .setTitle("Select Option")
                            .setMessage("Choose where to get your photo")
                            .setNegativeButton("take photo"
                            ) { _, _ ->
                                launchCamera()
                            }
                            .setPositiveButton("select from files"){_, _ ->
                                launchGallery()
                            }
                            .create()

                        dialogBuilder.show()
                    }
                    // check for permanent denial of any permission
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                        // permission is denied permanently, we will show user a dialog message.
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(list: List<PermissionRequest>, permissionToken: PermissionToken) {
                    // this method is called when user grants some permission and denies some of them.
                    permissionToken.continuePermissionRequest()
                }
            }).withErrorListener {
                // we are displaying a toast message for error message.
                Toast.makeText(applicationContext, "Error occurred! ", Toast.LENGTH_SHORT).show()
            }
            // below line is use to run the permissions on same thread and to check the permissions
            .onSameThread().check()
    }

    // below is the shoe setting dialog method
    // which is use to display a dialogue message.
    private fun showSettingsDialog() {
        // we are displaying an alert dialog for permissions
        val builder = AlertDialog.Builder(this@ActivityAddReport)

        // below line is the title for our alert dialog.
        builder.setTitle("Need Permissions")

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, _ ->
            // this method is called on click on positive button and on clicking shit button
            // we are redirecting our user from our app to the settings page of our app.
            dialog.cancel()
            // below is the intent from which we are redirecting our user.
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, 101)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            // this method is called when user click on negative button.
            dialog.cancel()
        }
        // below line is used to display our dialog
        builder.show()
    }

}