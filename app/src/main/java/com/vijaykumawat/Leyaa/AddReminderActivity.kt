package com.vijaykumawat.Leyaa


import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vijaykumawat.Leyaa.data.AlarmReminderContract
import com.vijaykumawat.Leyaa.reminder.AlarmScheduler
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import java.util.*

class AddReminderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val uri: Uri? = intent.data

        setContent {
            LeyaaTheme {
                AddReminderScreen(
                    uri = uri,
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    uri: Uri?,
    onNavigateBack: () -> Unit,
    context: AddReminderActivity
) {
    var title by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    
    val calendar = Calendar.getInstance()
    var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var hour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var minute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }

    var isEditMode by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = calendar.timeInMillis
    )
    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = false
    )

    // Load existing data if URI is present
    LaunchedEffect(uri) {
        if (uri != null) {
            isEditMode = true
            val projection = arrayOf(
                AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
                AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
                AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
                AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE
            )
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    title = it.getString(it.getColumnIndexOrThrow(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE))
                    
                    val dateStr = it.getString(it.getColumnIndexOrThrow(AlarmReminderContract.AlarmReminderEntry.KEY_DATE))
                    val timeStr = it.getString(it.getColumnIndexOrThrow(AlarmReminderContract.AlarmReminderEntry.KEY_TIME))
                    active = it.getString(it.getColumnIndexOrThrow(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE)) == "true"
                    
                    // Parse Date (d/M/yyyy)
                    val dateParts = dateStr.split("/")
                    if (dateParts.size == 3) {
                        day = dateParts[0].toIntOrNull() ?: day
                        month = (dateParts[1].toIntOrNull() ?: (month + 1)) - 1
                        year = dateParts[2].toIntOrNull() ?: year
                    }
                    
                    // Parse Time (HH:mm)
                    val timeParts = timeStr.split(":")
                    if (timeParts.size == 2) {
                        hour = timeParts[0].toIntOrNull() ?: hour
                        minute = timeParts[1].toIntOrNull() ?: minute
                    }
                }
            }
        }
    }

    val formattedDate = "${day}/${month + 1}/${year}"
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Alarm" else "New Alarm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                        return@FloatingActionButton
                    }

                    val values = ContentValues().apply {
                        put(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE, title.trim())
                        put(AlarmReminderContract.AlarmReminderEntry.KEY_DATE, formattedDate)
                        put(AlarmReminderContract.AlarmReminderEntry.KEY_TIME, formattedTime)
                        put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE, if (active) "true" else "false")
                    }

                    val savedUri: Uri?
                    if (isEditMode && uri != null) {
                        context.contentResolver.update(uri, values, null, null)
                        savedUri = uri
                    } else {
                        savedUri = context.contentResolver.insert(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, values)
                    }

                    if (savedUri != null) {
                        // Schedule or Cancel alarm
                        val scheduler = AlarmScheduler()
                        if (active) {
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, year)
                                set(Calendar.MONTH, month)
                                set(Calendar.DAY_OF_MONTH, day)
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                                set(Calendar.SECOND, 0)
                            }
                            var selectedTimestamp = selectedCalendar.timeInMillis
                            
                            scheduler.setAlarm(context, selectedTimestamp, savedUri)
                        } else {
                            scheduler.cancelAlarm(context, savedUri)
                        }

                        Toast.makeText(context, "Alarm Saved", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    } else {
                        Toast.makeText(context, "Error saving alarm", Toast.LENGTH_SHORT).show()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Alarm Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Date", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = formattedDate,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Time", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = formattedTime,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Alarm Active", fontWeight = FontWeight.Bold)
                Switch(checked = active, onCheckedChange = { active = it })
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        year = cal.get(Calendar.YEAR)
                        month = cal.get(Calendar.MONTH)
                        day = cal.get(Calendar.DAY_OF_MONTH)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = timePickerState.hour
                    minute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }

    if (showDeleteDialog && uri != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Alarm?") },
            text = { Text("This will permanently remove the alarm.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    AlarmScheduler().cancelAlarm(context, uri)
                    context.contentResolver.delete(uri, null, null)
                    Toast.makeText(context, "Alarm Deleted", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
