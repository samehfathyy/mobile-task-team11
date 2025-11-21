package com.example.mobile_task

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mobile_task.data.TaskDatabase
import com.example.mobile_task.data.TaskViewModel
import com.example.mobile_task.data.TaskViewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mobile_task.data.Expense
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

var dateglobal: Long = 0

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val db = TaskDatabase.getDatabase(this)
        val factory = TaskViewModelFactory(db.ExpenseDao())
        setContent {
            val navController = rememberNavController()
            val viewModel: TaskViewModel = viewModel(factory = factory)
            NavHost(navController, startDestination = "home"){
                composable("home"){
                    HomeScreen(navController,viewModel)
                }
                composable ("filter"){
                    FilterScreen(dateglobal,navController,viewModel)
                }
                composable ("ShowAllScreen"){
                    ShowAllScreen(navController,viewModel)
                }


            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController,
               viewModel: TaskViewModel,
){
    var expenses by remember { mutableStateOf(emptyList<Expense>()) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val context = LocalContext.current

    val datePickerState = rememberDatePickerState()
    val selectedDateMillis = datePickerState.selectedDateMillis
    val formattedDate = remember(selectedDateMillis) {
        selectedDateMillis?.let { millis ->
            try {
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
            } catch (e: Exception) {
                "Invalid date"
            }
        } ?: "No date selected"
    }



    LaunchedEffect(Unit) {
        viewModel.getAllExpenses { loadedTasks ->
            expenses = loadedTasks
        }
    }
    Scaffold() {
            inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                .fillMaxHeight(),
        ){
            OutlinedTextField(
                value = title,
                onValueChange = {title=it},

                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = {amount=it},

                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth()
            )
            Row() {


                Button(
                    onClick = {
                        if (selectedDateMillis == null) {
                            Toast.makeText(context, "Please select date", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (amount == "") {
                            Toast.makeText(context, "Please enter amount", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (title == "") {
                            Toast.makeText(context, "Please enter title", Toast.LENGTH_LONG).show()
                            return@Button

                        }
                        val cleanDate = normalizeDate(selectedDateMillis)
                        viewModel.addExpense(
                            title = title,
                            amount = amount.toDouble(),
                            date = cleanDate
                        )
                    },

                ) {
                    Text("Add")
                }
                Button(
                    onClick = {
                        val filterDate = normalizeDate(selectedDateMillis ?: System.currentTimeMillis())
                        dateglobal = filterDate
                        navController.navigate("filter")
                    }
                ) {
                    Text("filter")
                }
                Button(
                    onClick = {
                        navController.navigate("ShowAllScreen")
                    }
                ) {
                    Text("show all")
                }
                //end of btn
            }
        }
    }
}
fun normalizeDate(millis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = millis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

@Composable
fun FilterScreen(date: Long,navController: NavController,
                viewModel: TaskViewModel){
    var expenses by remember { mutableStateOf(emptyList<Expense>()) }
    var totalAmount by remember { mutableStateOf(0.0) } // to store sum

    LaunchedEffect(Unit) {
        viewModel.getExpensesByDate(date) { list ->
            expenses = list
            totalAmount = list.sumOf { it.amount }
        }
    }
    Scaffold() {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 20.dp)) {
            Text(
                text = "Total: $totalAmount",
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Date: ${formatDate(dateglobal)}",
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(20.dp))
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp)) {
            items(expenses) { expense ->
                Row(Modifier.padding(bottom = 10.dp)) {
                Text("${expense.title} " ?: "", style = TextStyle(fontSize = 22.sp))
                    Text("${expense.amount.toString()} " ?: "", style = TextStyle(fontSize = 22.sp))
                    Text("(${ formatDate(expense.date) })" ?: "", style = TextStyle(fontSize = 22.sp))

                }
            }
        }
    }
    }

}


@Composable
fun ShowAllScreen(navController: NavController,
                 viewModel: TaskViewModel){
    var expenses by remember { mutableStateOf(emptyList<Expense>()) }

    LaunchedEffect(Unit) {
        viewModel.getAllExpenses() { list ->
            expenses = list
        }
    }
    Scaffold() {
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 20.dp)) {
            items(expenses){
                    expense ->

                Row(Modifier.clickable(onClick = {
                    dateglobal = expense.date
                    navController.navigate("filter")
                }).padding(bottom = 10.dp)) {

                        Text("${expense.title} " ?: "", style = TextStyle(fontSize = 22.sp))
                        Text("${expense.amount.toString()} " ?: "", style = TextStyle(fontSize = 22.sp))
                        Text("(${ formatDate(expense.date) })" ?: "", style = TextStyle(fontSize = 22.sp))


//                Text(expense.title?:"", style = TextStyle(fontSize = 22.sp))
//                    Text(formatDate(expense.date), style = TextStyle(fontSize = 22.sp))
                }

            }
        }
    }

}
fun formatDate(millis: Long, pattern: String = "dd MMM yyyy"): String {
    return try {
        val date = Date(millis)
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(date)
    } catch (e: Exception) {
        "Invalid date"
    }
}