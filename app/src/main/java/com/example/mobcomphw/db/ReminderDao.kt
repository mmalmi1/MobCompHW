package com.example.mobcomphw.db

import androidx.room.*

@Dao
interface ReminderDao {
    @Transaction
    @Insert
    fun insert(reminderInfo: ReminderInfo): Long

    @Query("UPDATE reminderInfo SET message = :message WHERE uid =:id")
    fun update(id: Int, message: String)

    @Query("DELETE FROM reminderInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("DELETE FROM reminderInfo")
    fun deleteAll()

    @Query("SELECT * FROM reminderInfo")
    fun getReminderInfos(): List<ReminderInfo>
}