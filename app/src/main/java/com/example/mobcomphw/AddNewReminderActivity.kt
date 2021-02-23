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
import android.widget.*
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
        var reminderDate = findViewById(R.id.editAddDate) as EditText
        var reminderTime = findViewById(R.id.editAddTime) as EditText
        val spinner = findViewById<View>(R.id.reminder_type_spinner) as Spinner

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            reminder_types
        )
        spinner.adapter = adapter

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            // TIME NOW
            var dateNow = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            var dateNowFormatted = dateNow.format(formatter)
            // WANTED REMINDER TIME
            var reminderDateString = "${reminderDate.text} ${reminderTime.text}"
            //var dateformatted2 : String = "null"
            var reminderCalendar : GregorianCalendar? = null
            var timeDiff : Long = 0
            var reminderSeen : Boolean = false
            if(reminderDateString == " ") {
                Log.d("Lab", "No reminder date set")
                reminderSeen = true
            }else {
                //dateformatted2 = reminderDateString.format(formatter)
                val dateparts = reminderDate.text.toString().split(".").toTypedArray()
                val timeparts = reminderTime.text.toString().split(":").toTypedArray()
                reminderCalendar = GregorianCalendar(
                    dateparts[2].toInt(),
                    dateparts[1].toInt() - 1,
                    dateparts[0].toInt(),
                    timeparts[0].toInt(),
                    timeparts[1].toInt()
                )
                timeDiff =  reminderCalendar.timeInMillis - Calendar.getInstance().timeInMillis
            }
            Log.d("Lab", timeDiff.toString())
            Log.d("Lab", reminderSeen.toString())
            val reminder = ReminderInfo(
                uid = null,
                creation_time = dateNowFormatted.toString(),
                message = reminderText.text.toString(),
                reminder_time = reminderDateString,
                location_x = "null",
                location_y = "null",
                creator_id = "null",
                reminder_seen = reminderSeen,
                reminder_type = spinner.selectedItem.toString()
                )
            val newReminder = Reminder(
                reminder.message,
                reminder.location_x,
                reminder.location_y,
                reminder.reminder_time,
                reminder.creation_time,
                reminder.creator_id,
                reminder.reminder_seen,
                spinner.selectedItem.toString())
            // Add to firebase
            val database = Firebase.database
            val reference = database.getReference("reminders")
            val key = reference.push().key
            if (key != null) {
                reference.child(key).setValue(newReminder)
            }

            AsyncTask.execute {
                //save reminder to room database
                val db = Room.databaseBuilder(
                    applicationContext,
                    ReminderDatabase::class.java,
                    "com.example.mobcomphw"
                ).build()
                val uid = db.reminderDao().insert(reminder).toInt()
                Log.d("Lab","New reminder created to DB: ${reminder.message} to ${reminder.reminder_time} and type ${spinner.selectedItem}")
                db.close()
                if(reminderDateString == " ") {
                    Log.d("Lab", "No reminder date set")
                }else {
                    scheduleJob(reminder.message, timeDiff, uid, reminder.reminder_time)
                }
            }
            if(reminderDateString == " ") {
                Toast.makeText(applicationContext, "New reminder set", Toast.LENGTH_LONG).show()
            }else {
                Toast.makeText(applicationContext, "New notification reminder set", Toast.LENGTH_LONG).show()
            }

            startActivity(
                Intent(applicationContext, MessageActivity::class.java)
            )
            finish()
        }
    }

    fun cancelJob(uid : Int) {
        val scheduler = getSystemService(AppCompatActivity.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(uid)
        Log.d("Lab", "Job cancelled $uid")
    }

    private fun scheduleJob(message : String, time : Long, uid : Int, reminderTime : String) {
        val componentName = ComponentName(this, ReminderJobService::class.java)
        val bundle = PersistableBundle()
        var notiMessage = "Reminder: ${message} at ${reminderTime}"
        Log.d("Lab", notiMessage)
        bundle.putString("message", notiMessage)
        bundle.putInt("uid", uid)
        val info = JobInfo.Builder(uid, componentName)
            .setRequiresCharging(false)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .setExtras(bundle)
            .setMinimumLatency(time)
            .build()

        val scheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Lab", "Job scheduled")
        } else {
            Log.d("Lab", "Job scheduling failed")
            scheduleJob(message, time, uid, reminderTime)
        }
    }
}