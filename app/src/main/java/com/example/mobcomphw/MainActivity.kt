package com.example.mobcomphw

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.room.Room
import com.example.mobcomphw.db.PaymentInfo
import com.example.mobcomphw.db.UserDatabase
import com.example.mobcomphw.db.UserInfo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var textFieldUsername = findViewById(R.id.editUsername) as EditText
        var textFieldPassword = findViewById(R.id.editPassword) as EditText

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            var username: String = textFieldUsername.text.toString()
            var password: String = textFieldPassword.text.toString()
            var refreshTask = CheckUser(username)
            refreshTask.execute(username, password)
        }
        findViewById<TextView>(R.id.registerTxt).setOnClickListener {
            startActivity(
                Intent(applicationContext, RegistrationActivity::class.java)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        var textFieldUsername = findViewById(R.id.editUsername) as EditText
        var textFieldPassword = findViewById(R.id.editPassword) as EditText
    }

    //android.os.AsyncTask<Params, Progress, Result>
    inner class CheckUser(username: String) : AsyncTask<String?, String?, Boolean>(){
        override fun doInBackground(vararg params: String?): Boolean {
            val db = Room
                .databaseBuilder(
                    applicationContext,
                    UserDatabase::class.java,
                    "com.example.mobcomphw"
                )
                .fallbackToDestructiveMigration().build()
            db.close()
            val dBpassword = db.userDao().getUser(params[0].toString())
            //Log.d("Lab", params[1].toString())

            db.close()
            return if(params[1].toString() == dBpassword) {
                Log.d("Lab", "Starting message activity")
                true
            }else {
                Log.d("Lab", "No match in DB ${dBpassword}")
                false
            }
            return true
        }

        override fun onPostExecute(authenticated: Boolean) {
            super.onPostExecute(authenticated)
            if(authenticated) {

                startActivity(
                    Intent(applicationContext, MessageActivity::class.java)
                )
            }else {
                Toast.makeText(applicationContext, "Login failed", Toast.LENGTH_SHORT).show()
            }


        }
    }

}