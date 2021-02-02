package com.example.mobcomphw

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.mobcomphw.db.UserDatabase
import com.example.mobcomphw.db.UserInfo

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_registration)

        var username = findViewById(R.id.registerUsername) as EditText
        var password = findViewById(R.id.registerPassword) as EditText

        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            val userInstance = UserInfo(
                null,
                username = username.text.toString(),
                password = password.text.toString()
            )
            Log.d("Lab", userInstance.toString())

            AsyncTask.execute {
                //Save user to db
                val db = Room.databaseBuilder(
                    applicationContext,
                    UserDatabase::class.java,
                    "com.example.mobcomphw"
                ).build()
                val uuid = db.userDao().insert(userInstance).toInt()
                db.close()
            }
            finish()
            startActivity(
                Intent(applicationContext, MainActivity::class.java)
            )
        }
    }
}