package com.vijaykumawat.Leyaa

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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

data class Chore(
    val id: String = "",
    val title: String = "",
    val frequency: String = "",
    val assignedTo: String = "",
    val isCompleted: Boolean = false
)

class RoomChoresActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: ""
        val userName = intent.extras?.getString("userName") ?: ""

        setContent {
            LeyaaTheme {
                RoomChoresScreen(
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
fun RoomChoresScreen(
    roomID: String,
    roomName: String,
    userName: String,
    onNavigateBack: () -> Unit,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    var chores by remember { mutableStateOf<List<Chore>>(emptyList()) }
    var members by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Add dialog states
    var newTitle by remember { mutableStateOf("") }
    var newFrequency by remember { mutableStateOf("") }
    var newAssignedTo by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    DisposableEffect(roomID) {
        // Fetch Members
        db.collection("rooms").document(roomID).get().addOnSuccessListener { doc ->
            val memberIDs = doc.get("members") as? List<String> ?: emptyList()
            val fetchedMembers = mutableListOf<String>()
            var completed = 0
            if (memberIDs.isEmpty()) return@addOnSuccessListener
            
            memberIDs.forEach { uid ->
                db.collection("users").document(uid).get().addOnSuccessListener { userDoc ->
                    fetchedMembers.add(userDoc.getString("fullname") ?: "Unknown")
                    completed++
                    if (completed == memberIDs.size) {
                        members = fetchedMembers
                    }
                }
            }
        }

        // Fetch Chores
        val choresRef = db.collection("rooms").document(roomID).collection("chores")
        val listener = choresRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetched = snapshot.documents.map { doc ->
                    Chore(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        frequency = doc.getString("frequency") ?: "",
                        assignedTo = doc.getString("assignedTo") ?: "",
                        isCompleted = doc.getBoolean("isCompleted") ?: false
                    )
                }
                chores = fetched
                isLoading = false
            }
        }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Chores") },
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
                Icon(Icons.Filled.Add, contentDescription = "Add Chore")
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
            } else if (chores.isEmpty()) {
                Text(
                    "No chores yet. Add some to get started!",
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
                    items(chores) { chore ->
                        ChoreCard(
                            chore = chore,
                            onToggleComplete = { isChecked ->
                                db.collection("rooms").document(roomID).collection("chores")
                                    .document(chore.id).update("isCompleted", isChecked)
                            },
                            onDelete = {
                                db.collection("rooms").document(roomID).collection("chores")
                                    .document(chore.id).delete()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Recurring Chore") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Chore Title (e.g. Vacuum)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFrequency,
                        onValueChange = { newFrequency = it },
                        label = { Text("Frequency (e.g. Weekly)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    
                    Box {
                        OutlinedTextField(
                            value = newAssignedTo,
                            onValueChange = { },
                            label = { Text("Assign To") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) {
                                    Icon(Icons.Filled.ArrowDropDown, "Dropdown")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            members.forEach { memberName ->
                                DropdownMenuItem(
                                    text = { Text(memberName) },
                                    onClick = {
                                        newAssignedTo = memberName
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank() && newAssignedTo.isNotBlank()) {
                        val choreId = UUID.randomUUID().toString()
                        val choreData = mapOf(
                            "title" to newTitle,
                            "frequency" to newFrequency,
                            "assignedTo" to newAssignedTo,
                            "isCompleted" to false
                        )
                        db.collection("rooms").document(roomID).collection("chores")
                            .document(choreId).set(choreData)
                            .addOnSuccessListener {
                                sendRoomNotification("assigned a new chore: $newTitle to $newAssignedTo", roomID, roomName, userName, context)
                                Toast.makeText(context, "Chore Added", Toast.LENGTH_SHORT).show()
                            }
                        showAddDialog = false
                        newTitle = ""
                        newFrequency = ""
                        newAssignedTo = ""
                    } else {
                        Toast.makeText(context, "Title and Assigned To are required", Toast.LENGTH_SHORT).show()
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
fun ChoreCard(chore: Chore, onToggleComplete: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = chore.isCompleted,
                onCheckedChange = { onToggleComplete(it) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chore.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (chore.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Assigned To: ${chore.assignedTo}", fontSize = 14.sp)
                if (chore.frequency.isNotBlank()) {
                    Text(text = "Frequency: ${chore.frequency}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Chore", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
