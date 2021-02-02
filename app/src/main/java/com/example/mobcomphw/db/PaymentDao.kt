package com.example.mobcomphw.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PaymentDao {
    @Transaction
    @Insert
    fun insert(paymentInfo: PaymentInfo): Long

    @Query("DELETE FROM paymentInfo WHERE uid = :id")
    fun delete(id: Int)

    @Query("DELETE FROM paymentInfo")
    fun deleteAll()

    @Query("SELECT * FROM paymentInfo")
    fun getPaymentInfos(): List<PaymentInfo>
}