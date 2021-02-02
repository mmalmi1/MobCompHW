package com.example.mobcomphw.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "users")
data class UserInfo(
    @PrimaryKey(autoGenerate = true) var uid: Int?,
    @ColumnInfo(name="username") var username:String,
    @ColumnInfo(name="password")  var password:String
)


