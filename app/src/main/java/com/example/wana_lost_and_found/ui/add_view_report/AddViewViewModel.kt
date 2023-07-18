package com.example.wana_lost_and_found.ui.add_view_report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AddViewViewModel(val application : Application): ViewModel() {
    private var _itemName = MutableLiveData<String>()
    val itemName: LiveData<String> = _itemName
    private lateinit var mBytes: ByteArray

    val workmanager = WorkManager.getInstance(application)

    fun uploadReport(uri: Uri?){


    }

}
