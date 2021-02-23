package com.example.mobcomphw

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import com.example.mobcomphw.db.ReminderDatabase
import kotlin.random.Random


class ReminderJobService : JobService() {
    var jobCancelled = false

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("Lab", "Job started")
        doBackgroundWork(params)
        return true
    }

    private fun doBackgroundWork(params: JobParameters?) {
        Thread {
            kotlin.run {
                if (jobCancelled) {
                    return@Thread
                }
                var message = params?.extras?.getString("message")
                var uid = params?.extras?.getInt("uid")
                if (message != null && uid != null) {
                    showNotification(applicationContext, message, uid)
                }else {
                    //pass
                }
                jobFinished(params, false)
            }
        }.start()
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("Lab", "Job cancelled before completion")
        jobCancelled = true
        return true
    }

    companion object {
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
                //save reminder to room database
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
}