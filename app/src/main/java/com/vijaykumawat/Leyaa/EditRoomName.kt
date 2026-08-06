package com.vijaykumawat.Leyaa

import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme

class EditRoomName : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: ""
        val roomAvatar = intent.extras?.getString("roomAvatar") ?: ""
        
        setContent {
            LeyaaTheme {
                EditRoomNameScreen(
                    initialRoomName = roomName,
                    initialRoomAvatar = roomAvatar,
                    roomID = roomID,
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoomNameScreen(
    initialRoomName: String,
    initialRoomAvatar: String,
    roomID: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    var newRoomName by remember { mutableStateOf(initialRoomName) }
    var isSaving by remember { mutableStateOf(false) }

    val allAvatars = remember { SanitizeItemName.realAssetName.toList().sorted() }
    var selectedAvatar by remember { mutableStateOf(initialRoomAvatar.ifEmpty { if (allAvatars.isNotEmpty()) allAvatars[0] else "" }) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Room Name", fontWeight = FontWeight.Bold) },
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
                            if (newRoomName.trim().isEmpty()) {
                                Toast.makeText(context, "Room Name cannot be empty", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            
                            val updates = mapOf(
                                "title" to newRoomName.trim(),
                                "avatar" to selectedAvatar
                            )
                            
                            db.collection("rooms").document(roomID)
                                .update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                }
                                .addOnFailureListener {
                                    isSaving = false
                                    Toast.makeText(context, "Error updating room", Toast.LENGTH_SHORT).show()
                                }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("SAVE ROOM")
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
                painter = painterResource(id = R.drawable.edititem),
                contentDescription = "Edit Room Name",
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = newRoomName,
                onValueChange = { newRoomName = it },
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
