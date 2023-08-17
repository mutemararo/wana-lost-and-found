package com.wanalnf.wana_lost_and_found.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

interface AppContainer {
    val wanaRepository: WanaRepository
}

class DefaultContainer: AppContainer {

    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val databaseReference: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().reference
    }

    override val wanaRepository: WanaRepository by lazy {
        FirebaseRepository(firebaseAuth, databaseReference)
    }

}