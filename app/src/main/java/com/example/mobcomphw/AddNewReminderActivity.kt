package com.example.mobcomphw

import android.Manifest
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.mobcomphw.db.ReminderDatabase
import com.example.mobcomphw.db.ReminderInfo
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private var text : String = ""
private var savedReminderDate : String = ""
private var savedReminderTime : String = ""
private var reminderlat : String? = ""
private var reminderlon : String? = ""


class AddNewReminderActivity : AppCompatActivity() {
    private val reminder_types = ArrayList<String>(
        Arrays.asList(
            "Miscellaneous",
            "Home chore",
            "School work",
            "Exercise",
            "Social"
        )
    )
    private lateinit var geofencingClient: GeofencingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_reminder)
        var reminderText = findViewById(R.id.editAddReminder) as EditText
        var reminderDate = findViewById(R.id.editAddDate) as EditText
        var reminderTime = findViewById(R.id.editAddTime) as EditText
        val spinner = findViewById<View>(R.id.reminder_type_spinner) as Spinner
        geofencingClient = LocationServices.getGeofencingClient(this)

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            reminder_types
        )
        spinner.adapter = adapter

        findViewById<Button>(R.id.addLocation).setOnClickListener {
            startActivity(
                Intent(applicationContext, MapsActivity::class.java)
            )
        }

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
            var reminderTimeInMillis : Long = 0
            var reminderSeen : Boolean = false
            if(reminderDateString.length < 3 && reminderlat.toString().length < 5) {
                Log.d("Lab", "No reminder date set or place set")
                reminderSeen = true
            }
            if(reminderDateString.length > 3) {
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
                reminderTimeInMillis = reminderCalendar.timeInMillis
                timeDiff =  reminderCalendar.timeInMillis - Calendar.getInstance().timeInMillis
            }
            Log.d("Lab", timeDiff.toString())
            Log.d("Lab", reminderSeen.toString())
            val reminder = ReminderInfo(
                uid = null,
                creation_time = dateNowFormatted.toString(),
                message = reminderText.text.toString(),
                reminder_time = reminderDateString,
                location_x = reminderlat.toString(),
                location_y = reminderlon.toString(),
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
            var uid : Int
            AsyncTask.execute {
                //save reminder to room database
                val db = Room.databaseBuilder(
                    applicationContext,
                    ReminderDatabase::class.java,
                    "com.example.mobcomphw"
                ).build()
                uid = db.reminderDao().insert(reminder).toInt()
                Log.d("Lab","New reminder created to DB: ${reminder.message} to ${reminder.reminder_time} and type ${spinner.selectedItem}")
                db.close()
                // Create geofence
                if(reminder.location_x.length > 4) {
                    Log.d("Lab", "Reminder location: " + reminder.location_x + " " + reminder.location_y)
                    val newLocation = LatLng(reminder.location_x.toDouble(), reminder.location_y.toDouble())
                    createGeoFence(newLocation, uid, geofencingClient, reminder.message, reminderTimeInMillis)
                }
                if(reminderDateString == " ") {
                    Log.d("Lab", "No reminder date set")
                }else {
                    // Schedule notification
                    scheduleJob(reminder.message, timeDiff, uid, reminder.reminder_time)
                }
            }
            if(reminderDateString == " ") {
                Toast.makeText(applicationContext, "New reminder set", Toast.LENGTH_LONG).show()
            }else {
                Toast.makeText(applicationContext, "New notification reminder set", Toast.LENGTH_LONG).show()
            }
            reminderText.setText("")
            reminderDate.setText("")
            reminderTime.setText("")
            savedReminderDate = ""
            savedReminderTime = ""
            reminderlat = ""
            reminderlon = ""

            startActivity(
                Intent(applicationContext, MessageActivity::class.java)
            )
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(applicationContext, MessageActivity::class.java))
    }

    override fun onPause() {
        super.onPause()
        var reminderText = findViewById(R.id.editAddReminder) as EditText
        var reminderDate = findViewById(R.id.editAddDate) as EditText
        var reminderTime = findViewById(R.id.editAddTime) as EditText
        text = reminderText.text.toString()
        savedReminderDate = reminderDate.text.toString()
        savedReminderTime = reminderTime.text.toString()
        Log.d("Lab", "Saved text $text $savedReminderDate $savedReminderTime")
    }

    override fun onResume() {
        super.onResume()
        var reminderText = findViewById(R.id.editAddReminder) as EditText
        var reminderDate = findViewById(R.id.editAddDate) as EditText
        var reminderTime = findViewById(R.id.editAddTime) as EditText
        var locationText = findViewById<TextView>(R.id.locationText)

        reminderText.setText(text)
        reminderDate.setText(savedReminderDate)
        reminderTime.setText(savedReminderTime)
        Log.d("Lab", "Loaded $text $savedReminderDate $savedReminderTime")
        val extras = intent.extras
        reminderlat = extras?.getString("latitude")
        reminderlon = extras?.getString("longitude")
        if(reminderlat != null) {
            Log.d("Lab", reminderlat + " " + reminderlon)
            locationText.setText("Reminder location: ${reminderlat!!.subSequence(0, 7)}, ${reminderlon!!.subSequence(0, 7)}")
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

    private fun createGeoFence(location: LatLng, uid: Int, geofencingClient: GeofencingClient, message: String , reminderTime: Long) {
        Log.d("Lab", "Creating Geofence")
        val geofence = Geofence.Builder()
            .setRequestId(GEOFENCE_ID)
            .setCircularRegion(location.latitude, location.longitude, GEOFENCE_RADIUS.toFloat())
            .setExpirationDuration(GEOFENCE_EXPIRATION.toLong())
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .setLoiteringDelay(GEOFENCE_DWELL_DELAY)
            .build()

        val geofenceRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        val intent = Intent(this, GeofenceReceiver::class.java)
            .putExtra("uid", uid.toString())
            .putExtra("message", message)
            .putExtra("reminderTime", reminderTime.toString())

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    GEOFENCE_LOCATION_REQUEST_CODE
                )
            } else {
                geofencingClient.addGeofences(geofenceRequest, pendingIntent)
            }
        } else {
            geofencingClient.addGeofences(geofenceRequest, pendingIntent)
        }
    }
}