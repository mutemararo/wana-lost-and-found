package com.wanalnf.wana_lost_and_found.utils

import android.app.Application
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.wanalnf.wana_lost_and_found.model.Report
import com.wanalnf.wana_lost_and_found.ui.login_signup.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

//redirect to login
fun redirectToLogin(context: Context){
    val intent = Intent(context, LoginActivity::class.java)
    context.startActivity(intent)
}

//toast maker
fun makeToast(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}


//reading information from the database

fun getData(){
    val reference: DatabaseReference = FirebaseDatabase.getInstance().reference

    //creating a query
    val query1 : Query = reference.child("reporters")
        .orderByKey()//Sorting method: order using the user ids(or top level object name). For order using a field use .orderByChild(field_name)
        .equalTo(FirebaseAuth.getInstance().currentUser!!.uid) //Filtering method

    query1.addListenerForSingleValueEvent(object : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            for (shot in snapshot.children){
                val reporter = shot.getValue(Report::class.java)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    })
}

