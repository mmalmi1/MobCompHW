package com.example.mobcomphw.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(PaymentInfo::class), version = 3)

abstract class AppDatabase:RoomDatabase() {
   abstract fun paymentDao(): PaymentDao
}