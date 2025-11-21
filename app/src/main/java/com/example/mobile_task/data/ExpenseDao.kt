package com.example.mobile_task.data

import androidx.room.*


@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(task: Expense)

    @Query("SELECT * FROM db")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM db WHERE date = :dayMillis")
    suspend fun getExpensesByDate(dayMillis: Long): List<Expense>
}