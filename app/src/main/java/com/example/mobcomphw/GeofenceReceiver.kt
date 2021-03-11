package com.example.mobcomphw

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.mobcomphw.db.ReminderDatabase
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.random.Random

class GeofenceReceiver : BroadcastReceiver() {
    lateinit var uid: String
    lateinit var message: String
    var reminderTime: Long = 0L

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Lab", "Received geofence")
        if (context != null) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            val geofencingTransition = geofencingEvent.geofenceTransition

            if (geofencingTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofencingTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
                // Retrieve data from intent
                if (intent != null) {
                    uid = intent.getStringExtra("uid")!!
                    message = intent.getStringExtra("message")!!
                    reminderTime = intent.getStringExtra("reminderTime").toLong()
                    Log.d("Lab", "Notification time $reminderTime")
                }
                if(reminderTime == 0L) {
                    showNotification(context, message, uid.toInt())
                    // remove geofence
                    val triggeringGeofences = geofencingEvent.triggeringGeofences
                    MapsActivity.removeGeofences(context, triggeringGeofences)
                }else if(reminderTime < Calendar.getInstance().timeInMillis) {
                    showNotification(context, message, uid.toInt())
                    // remove geofence
                    val triggeringGeofences = geofencingEvent.triggeringGeofences
                    MapsActivity.removeGeofences(context, triggeringGeofences)
                }
            }
        }
    }

    fun showNotification(context: Context?, message: String, uid : Int) {
        Log.d("Lab", "Notification call")
        val CHANNEL_ID = "REMINDER_NOTIFICATION_CHANNEL"
        var notificationId = 1589
        notificationId += Random(notificationId).nextInt(1, 30)

        val notificationBuilder =
            NotificationCompat.Builder(context!!.applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_fab_menu)
                .setContentTitle("NoteZ")
                .setContentText(message)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.app_name)
            }
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(notificationId, notificationBuilder.build())
        AsyncTask.execute {
            //save reminder as seen to room database
            val db = Room.databaseBuilder(
                context,
                ReminderDatabase::class.java,
                "com.example.mobcomphw"
            ).build()
            db.reminderDao().updateToSeen(uid)
            Log.d("Lab", "Notification $uid set to seen")
            db.close()
        }
    }
}