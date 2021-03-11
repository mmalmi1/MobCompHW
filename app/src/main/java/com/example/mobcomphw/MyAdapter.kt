package com.example.mobcomphw

import android.content.Context
import android.os.AsyncTask
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import com.example.mobcomphw.databinding.ListItemBinding
import com.example.mobcomphw.db.ReminderDatabase
import com.example.mobcomphw.db.ReminderInfo
import java.util.*
import kotlin.random.Random


class MyAdapter(context: Context, private  val list:List<ReminderInfo>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mTTS: TextToSpeech? = null

    override fun getView(position: Int, convertView: View?, container: ViewGroup): View? {
        var rowBinding = ListItemBinding.inflate(inflater, container, false)
        // Get context from parent
        val context: Context = container.context
        val editText = EditText(context)
        //set payment info values to the list item
        rowBinding.reminderDate.text = list[position].creation_time
        rowBinding.reminderMessage.text = list[position].message

        when(list[position].reminder_type) {
            "Miscellaneous" ->  rowBinding.reminderImageType.setImageResource(R.drawable.ic_miscellaneous)
            "Home chore" ->  rowBinding.reminderImageType.setImageResource(R.drawable.ic_home_chore)
            "School work" ->  rowBinding.reminderImageType.setImageResource(R.drawable.ic_school_work)
            "Exercise" ->  rowBinding.reminderImageType.setImageResource(R.drawable.ic_dumbell)
            "Social" ->  rowBinding.reminderImageType.setImageResource(R.drawable.ic_social)

        }
        mTTS = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Init
                rowBinding.textToSpeechButton.isEnabled = true
            } else {
                rowBinding.textToSpeechButton.isEnabled = false
                Log.d("Lab", "Text to speech init failed")
            }
        })
        // Make the microphone button speak
        rowBinding.textToSpeechButton.setOnClickListener() {
            var textId : Int = 500
            mTTS!!.speak(rowBinding.reminderMessage.text.toString(), TextToSpeech.QUEUE_FLUSH, null, Random(textId).nextInt(1, 30).toString())
        }

        rowBinding.deleteButton.setOnClickListener() {
            Log.d("Lab", "Delete " +  rowBinding.reminderMessage.text + ' ' + list[position].uid)
            val selectedPayment = list[position]
            val message =
                "Do you want to delete ${selectedPayment.message} ?"
            // Show AlertDialog to delete the reminder
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete reminder?")
                .setMessage(message)
                .setPositiveButton("Delete") { _, _ ->
                    //delete from database
                    AsyncTask.execute {
                        val db = Room
                            .databaseBuilder(
                                context,
                                ReminderDatabase::class.java,
                                "com.example.mobcomphw"
                            )
                            .build()
                        db.reminderDao().delete(selectedPayment.uid!!)
                    }
                    //refresh payments list
                    if (context is MessageActivity) {
                        (context).refreshListView()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Do nothing
                    dialog.dismiss()
                    //refresh payments list
                    if (context is MessageActivity) {
                        (context).refreshListView()
                    }
                }
                .show()
        }
        rowBinding.editButton.setOnClickListener() {
            val selectedPayment = list[position]
            editText.setText(selectedPayment.message)
            val message =
                "Type to edit reminder ${selectedPayment.message}?"
            // Show AlertDialog to delete the reminder
            val builder2 = AlertDialog.Builder(context)
            builder2.setTitle("Edit reminder")
                .setView(editText)
                .setMessage(message)
                .setPositiveButton("Confirm") { _, _ ->
                    //delete from database
                    AsyncTask.execute {
                        val db = Room
                            .databaseBuilder(
                                context,
                                ReminderDatabase::class.java,
                                "com.example.mobcomphw"
                            )
                            .build()
                        db.reminderDao().update(selectedPayment.uid!!, editText.text.toString())
                    }
                    //refresh payments list
                    if (context is MessageActivity) {
                        (context).refreshListView()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    // Do nothing
                    dialog.dismiss()
                    //refresh payments list
                    if (context is MessageActivity) {
                        (context).refreshListView()
                    }
                }
                .show()
        }
        return rowBinding.root
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return list.size
    }

}