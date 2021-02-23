package com.example.mobcomphw.db

import androidx.room.*

@Dao
interface ReminderDao {
    @Transaction
    @Insert
    fun insert(reminderInfo: ReminderInfo): Long

    @Query("UPDATE reminderInfo SET message = :message WHERE uid =:id")
    fun update(id: Int, message: String)

    @Query("UPDATE reminderInfo SET reminder_seen = 1 WHERE uid =:id")
    fun updateToSeen(id: Int)

    @Query("DELETE FROM reminderInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("DELETE FROM reminderInfo")
    fun deleteAll()

    @Query("SELECT * FROM reminderInfo WHERE reminder_seen = 1")
    fun getReminderInfos(): List<ReminderInfo>

    @Query("SELECT * FROM reminderInfo")
    fun getAll(): List<ReminderInfo>
}