package com.example.mobcomphw.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao {
    @Transaction
    @Insert
    fun insert(users: UserInfo): Long

    @Query("DELETE FROM users WHERE username = :name")
    fun delete(name: String)

    @Query("DELETE FROM users")
    fun deleteAll()

    @Query("SELECT password FROM users WHERE username = :username LIMIT 1")
    fun getUser(username: String): String
}