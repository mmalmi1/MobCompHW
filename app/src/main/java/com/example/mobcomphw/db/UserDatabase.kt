package com.example.mobcomphw.db

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = arrayOf(UserInfo::class), version = 4)

abstract class UserDatabase:RoomDatabase() {
    abstract fun userDao(): UserDao
}