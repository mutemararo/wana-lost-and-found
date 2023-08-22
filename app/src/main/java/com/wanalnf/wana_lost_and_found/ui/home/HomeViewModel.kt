package com.wanalnf.wana_lost_and_found.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wanalnf.wana_lost_and_found.data.WanaRepository
import com.wanalnf.wana_lost_and_found.model.Report
import kotlinx.coroutines.launch
import java.lang.Exception

class HomeViewModel(val repository: WanaRepository): ViewModel() {

    private val _myReports = MutableLiveData<MutableList<Report>>()
    val myReports = _myReports

    init {
        getMyReports()
    }

    private fun getMyReports(){

        viewModelScope.launch {
            try {
                _myReports.value = repository.getMyReports()
            } catch (e: Exception) {
                print(e.message)
            }
        }
    }
}

class HomeViewModelFactory(private val repository: WanaRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}