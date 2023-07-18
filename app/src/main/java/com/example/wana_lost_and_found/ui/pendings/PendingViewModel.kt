package com.example.wana_lost_and_found.ui.pendings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PendingViewModel : ViewModel() {
    // TODO: Implement the ViewModel
}

//if there ia a repository declare PendingViewModelFactory(repository : DataRepository)
class PendingViewModelFactory : ViewModelProvider.NewInstanceFactory(){

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ((PendingViewModel::class.java) as T)

    //use (PendingViewModel(repository) as T) instead if there is a repository
}