package com.vijaykumawat.Leyaa

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import java.util.UUID

data class SharedReminder(
    val id: String = "",
    val title: String = "",
    val dueDate: String = "",
    val amount: String = "",
    val createdBy: String = ""
)

class RoomRemindersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: ""
        val userName = intent.extras?.getString("userName") ?: ""

        setContent {
            LeyaaTheme {
                RoomRemindersScreen(
                    roomID = roomID,
                    roomName = roomName,
                    userName = userName,
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomRemindersScreen(
    roomID: String,
    roomName: String,
    userName: String,
    onNavigateBack: () -> Unit,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    var reminders by remember { mutableStateOf<List<SharedReminder>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Add dialog states
    var newTitle by remember { mutableStateOf("") }
    var newDueDate by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }

    DisposableEffect(roomID) {
        val remindersRef = db.collection("rooms").document(roomID).collection("sharedReminders")
        val listener = remindersRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetched = snapshot.documents.map { doc ->
                    SharedReminder(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        dueDate = doc.getString("dueDate") ?: "",
                        amount = doc.getString("amount") ?: "",
                        createdBy = doc.getString("createdBy") ?: ""
                    )
                }
                reminders = fetched
                isLoading = false
            }
        }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Utility Reminders") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (reminders.isEmpty()) {
                Text(
                    "No reminders yet.",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(reminder, onDelete = {
                            db.collection("rooms").document(roomID).collection("sharedReminders")
                                .document(reminder.id).delete()
                        })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Utility Reminder") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title (e.g. Rent, Internet)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDueDate,
                        onValueChange = { newDueDate = it },
                        label = { Text("Due Date (e.g. 5th of Month)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = { newAmount = it },
                        label = { Text("Amount (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank() && newDueDate.isNotBlank()) {
                        val reminderId = UUID.randomUUID().toString()
                        val reminderData = mapOf(
                            "title" to newTitle,
                            "dueDate" to newDueDate,
                            "amount" to newAmount,
                            "createdBy" to userName
                        )
                        db.collection("rooms").document(roomID).collection("sharedReminders")
                            .document(reminderId).set(reminderData)
                            .addOnSuccessListener {
                                sendRoomNotification("added a new bill reminder: $newTitle", roomID, roomName, userName, context)
                                Toast.makeText(context, "Reminder Added", Toast.LENGTH_SHORT).show()
                            }
                        showAddDialog = false
                        newTitle = ""
                        newDueDate = ""
                        newAmount = ""
                    } else {
                        Toast.makeText(context, "Title and Due Date are required", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ReminderCard(reminder: SharedReminder, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = reminder.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Due: ${reminder.dueDate}", fontSize = 14.sp)
                if (reminder.amount.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Amount: ${reminder.amount}", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Added by: ${reminder.createdBy}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Reminder", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
