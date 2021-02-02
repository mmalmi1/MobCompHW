package com.example.mobcomphw

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.content.Context
import android.view.LayoutInflater
import com.example.mobcomphw.databinding.ListItemBinding
import com.example.mobcomphw.db.PaymentInfo



class MyAdapter(context: Context, private  val list:List<PaymentInfo>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, container: ViewGroup?): View? {
        var rowBinding = ListItemBinding.inflate(inflater, container, false)
        //set payment info values to the list item
        rowBinding.reminderDate.text = list[position].name
        rowBinding.reminderMessage.text = list[position].accountNumber

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