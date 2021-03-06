package com.example.mobcomphw.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = arrayOf(ReminderInfo::class), version = 5, exportSchema = false)

abstract class ReminderDatabase:RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}