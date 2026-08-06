package com.vijaykumawat.Leyaa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme

class Room_Creation : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LeyaaTheme {
                RoomCreationScreen(
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomCreationScreen(
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    var roomName by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    val allAvatars = remember { SanitizeItemName.realAssetName.toList().sorted() }
    var selectedAvatar by remember { mutableStateOf(if (allAvatars.isNotEmpty()) allAvatars[0] else "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Room", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val name = roomName.trim()
                            if (name.isEmpty()) {
                                Toast.makeText(context, "Room name is missing", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val currentUser = auth.currentUser
                            if (currentUser == null) {
                                Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            isCreating = true
                            val userID = currentUser.uid
                            
                            val newRoom = hashMapOf(
                                "lastItemID" to "",
                                "members" to listOf(userID),
                                "newItems" to listOf<String>(),
                                "title" to name,
                                "avatar" to selectedAvatar
                            )
                            
                            db.collection("rooms").add(newRoom)
                                .addOnSuccessListener { documentRef ->
                                    Log.d("TAG", "onSuccess: Room ID $userID")
                                    
                                    // Update Device Token
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val token = task.result
                                            db.collection("users").document(userID).update("deviceToken", token)
                                        }
                                    }
                                    
                                    // Navigate to RoomListActivity
                                    val intent = Intent(context, RoomListActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    isCreating = false
                                    Log.d("TAG", "onFailure: $e")
                                    Toast.makeText(context, "Failed to create room", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isCreating
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("CREATE ROOM")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            Image(
                painter = painterResource(id = R.drawable.createroom),
                contentDescription = "Create Room",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = roomName,
                onValueChange = { roomName = it },
                label = { Text("Room Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Select Room Picture",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(allAvatars) { avatarName ->
                    val sanitized = SanitizeItemName.sanitizeItemName(avatarName)
                    val resId = context.resources.getIdentifier(sanitized, "drawable", context.packageName)
                    if (resId != 0) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selectedAvatar == avatarName) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedAvatar = avatarName }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = avatarName,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}
