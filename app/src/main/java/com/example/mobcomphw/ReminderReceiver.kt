package com.example.mobcomphw

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ReminderReceiver : BroadcastReceiver() {
    lateinit var key: String
    lateinit var text: String

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            Log.d("Lab", "Reminder receiver started")
            // Retrieve data from intent
            if (intent != null) {
                key = intent.getStringExtra("key")!!
                text = intent.getStringExtra("message")!!
            }

            /*val firebase = Firebase.database
            val reference = firebase.getReference("reminders")
            val reminderListener = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val reminder = snapshot.getValue<Reminder>()
                    if (reminder != null) {
                        Log.d("Lab", reminder.message)
                        ReminderJobService
                            .showNotification(
                                context.applicationContext,
                                "Reminder: ${reminder.creation_time} - ${reminder.message}"
                            )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("reminder:onCancelled: ${error.details}")
                }

            }
            val child = reference.child(key)
            child.addValueEventListener(reminderListener)*/

        }
    }

}