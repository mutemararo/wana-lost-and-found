package com.wanalnf.wana_lost_and_found.ui.add_view_report

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddViewViewModel(val application : Application): ViewModel() {
    private var _itemName = MutableLiveData<String>()
    val itemName: LiveData<String> = _itemName
    private lateinit var mBytes: ByteArray


    fun uploadReport(uri: Uri?){


    }

}
