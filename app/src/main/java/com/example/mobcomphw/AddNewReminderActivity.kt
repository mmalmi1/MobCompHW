package com.example.mobcomphw

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.mobcomphw.db.ReminderDatabase
import com.example.mobcomphw.db.ReminderInfo
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class AddNewReminderActivity : AppCompatActivity() {
    private lateinit var map: GoogleMap
    private val reminder_types = ArrayList<String>(
        Arrays.asList(
            "Miscellaneous",
            "Home chore",
            "School work",
            "Exercise",
            "Social"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_reminder)
        var reminderText = findViewById(R.id.editAddReminder) as EditText
        val spinner = findViewById<View>(R.id.reminder_type_spinner) as Spinner

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            reminder_types
        )
        spinner.adapter = adapter

        var date = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        var dateFormatted = date.format(formatter)
        Log.d("Lab", dateFormatted.toString())
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
        val reminder = ReminderInfo(
            uid = null,
            creation_time = dateFormatted.toString(),
            message = reminderText.text.toString(),
            reminder_time = "null",
            location_x = "null",
            location_y = "null",
            creator_id = "null",
            reminder_seen = false,
            reminder_type = spinner.selectedItem.toString()
            )
            val newReminder = Reminder(reminder.message,reminder.location_x,reminder.location_y,reminder.reminder_time,reminder.creation_time,reminder.creator_id,reminder.reminder_seen, spinner.selectedItem.toString())
            // Add to firebase

            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            Log.d("Lab","Reference ${reference}")
            if (key != null) {
                reference.child(key).setValue(newReminder)
            }
            //scheduleJob(reminder.message)

            AsyncTask.execute {
            //save reminder to room database
            val db = Room.databaseBuilder(
                applicationContext,
                ReminderDatabase::class.java,
                "com.example.mobcomphw"
            ).build()
            db.reminderDao().insert(reminder)
            Log.d("Lab","New reminder created to DB ${reminder.message} and type ${spinner.selectedItem}")
            db.close()

        }
        startActivity(
            Intent(applicationContext, MessageActivity::class.java)
        )
        finish()
        }
    }

    private fun scheduleJob(message : String) {
        val componentName = ComponentName(this, ReminderJobService::class.java)
        val bundle = PersistableBundle()
        bundle.putString("message", message)
        val info = JobInfo.Builder(321, componentName)
            .setRequiresCharging(false)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setExtras(bundle)
            .setTriggerContentMaxDelay(6000)
            .build()

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Lab", "Job scheduled")
        } else {
            Log.d("Lab", "Job scheduling failed")
            scheduleJob(message)
        }
    }

    private fun cancelJob() {
        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(321)
        Log.d("Lab", "Job cancelled")
    }

}