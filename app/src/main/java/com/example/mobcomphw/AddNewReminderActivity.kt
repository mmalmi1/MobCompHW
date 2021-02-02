package com.example.mobcomphw

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.mobcomphw.db.AppDatabase
import com.example.mobcomphw.db.PaymentInfo

class AddNewReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_reminder)
        var reminderText = findViewById(R.id.editAddReminder) as EditText

        var date = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        var dateFormatted = date.format(formatter)
        Log.d("Lab", dateFormatted.toString())

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val payment = PaymentInfo(
                uid = null,
                name = dateFormatted.toString(),
                accountNumber = reminderText.text.toString())

            AsyncTask.execute {
            //save payment to room datbase
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "com.example.mobcomphw"
            ).build()
            db.paymentDao().insert(payment)
            Log.d("Lab","New reminder created ${payment.accountNumber}")
            db.close()
        }
        startActivity(
            Intent(applicationContext, MessageActivity::class.java)
        )
        finish()
        }
    }
}