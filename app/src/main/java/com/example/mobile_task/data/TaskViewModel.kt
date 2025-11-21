package com.example.mobile_task.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

class TaskViewModel(private val dao: ExpenseDao) : ViewModel() {

    fun addExpense(title: String, amount: Double, date: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertExpense(Expense(title = title, amount = amount, date = date))
        }
    }
//
//    fun updateTask(task: Task) {
//        viewModelScope.launch(Dispatchers.IO) {
//            dao.updateTask(task)
//        }
//    }
//
    fun getAllExpenses(onResult: (List<Expense>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val expenses = dao.getAllExpenses()
            onResult(expenses)
        }
    }
    fun getExpensesByDate(selectedDateMillis: Long, onResult: (List<Expense>) -> Unit) {
        val filterDate = normalizeDate(selectedDateMillis) // normalize to midnight
        viewModelScope.launch(Dispatchers.IO) {
            val expenses = dao.getExpensesByDate(filterDate)
            onResult(expenses)
        }
    }

    // reuse normalizeDate function
    private fun normalizeDate(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
//
//
//    fun getTaskById(id: Int, onResult: (Task?) -> Unit) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val task = dao.getTask(id)
//            onResult(task)
//        }
//    }
//    fun deleteTaskById(id: Int) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val task = dao.deleteTaskById(id)
//        }
//    }

}
