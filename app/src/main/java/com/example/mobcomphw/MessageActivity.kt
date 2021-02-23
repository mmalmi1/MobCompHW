package com.example.mobcomphw

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mobcomphw.databinding.MessageLayoutBinding
import com.example.mobcomphw.db.ReminderInfo
import com.example.mobcomphw.db.ReminderDatabase
import kotlinx.android.synthetic.main.list_item.view.*


class MessageActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var binding: MessageLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MessageLayoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        listView = binding.listView

        var toggleFab: Boolean = true
        val fab: View = findViewById(R.id.fab_menu)
        val fab2: View = findViewById(R.id.fab_logout)
        val fab3: View = findViewById(R.id.fab_add_reminder)
        val fab4: View = findViewById(R.id.fab_profile)

        fab.setOnClickListener { view ->
            if(toggleFab) {
                showIn(fab2)
                showIn(fab3)
                showIn(fab4)
                toggleFab = false
            }else {
                showOut(fab2)
                showOut(fab3)
                showOut(fab4)
                toggleFab = true
            }
        }

        fab2.setOnClickListener { view ->
            startActivity(
                Intent(applicationContext, MainActivity::class.java)
            )
        }
        fab3.setOnClickListener { view ->
            startActivity(
                Intent(applicationContext, AddNewReminderActivity::class.java)
            )
        }
        fab4.setOnClickListener { view ->
            startActivity(
                Intent(applicationContext, profileActivity::class.java)
            )
        }
        //update userInterface
        refreshListView()
    }

    override fun onResume() {
        super.onResume()
        refreshListView()
    }

    fun refreshListView() {
        var refreshTask = LoadPaymentInfoEntries()
        refreshTask.execute()
    }

    private fun showIn(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 0f
        v.translationY = v.height.toFloat()
        v.animate()
            .setDuration(200)
            .translationY(0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                }
            })
            .alpha(1f)
            .start()
    }

    fun showOut(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 1f
        v.translationY = 0f
        v.animate()
            .setDuration(200)
            .translationY(v.height.toFloat())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    v.visibility = View.GONE
                    super.onAnimationEnd(animation)
                }
            }).alpha(0f)
            .start()
    }

    private fun init(v: View) {
        v.visibility = View.GONE
        v.translationY = v.height.toFloat()
        v.alpha = 0f
    }

    inner class LoadPaymentInfoEntries : AsyncTask<String?, String?, List<ReminderInfo>>() {
        override fun doInBackground(vararg params: String?): List<ReminderInfo> {
            val db = Room
                .databaseBuilder(
                    applicationContext,
                    ReminderDatabase::class.java,
                    "com.example.mobcomphw"
                )
                //.fallbackToDestructiveMigration()
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_4_3)
                .addMigrations(MIGRATION_3_5)
                .addMigrations(MIGRATION_4_5)
                .build()
            val reminderInfos = db.reminderDao().getReminderInfos()
            db.close()
            return reminderInfos
        }
        override fun onPostExecute(paymentInfos: List<ReminderInfo>?) {
            super.onPostExecute(paymentInfos)
            if (paymentInfos != null) {
                if (paymentInfos.isNotEmpty()) {
                    val adaptor = MyAdapter(applicationContext, paymentInfos)
                    listView.adapter = adaptor
                } else {
                    listView.adapter = null
                    Toast.makeText(applicationContext, "No items to show", Toast.LENGTH_SHORT).show()
                }
            }
        }
        private val MIGRATION_2_3  = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }
        private val MIGRATION_4_3  = object : Migration(4, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_4_5  = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
        private val MIGRATION_3_5  = object : Migration(3, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
            }
        }
    }
}