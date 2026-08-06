package com.vijaykumawat.Leyaa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import java.util.*

class MemberAdd : ComponentActivity() {

    private var roomID: String? = null
    private var roomName: String? = null
    private var userName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        roomID = intent.getStringExtra("roomID")
        roomName = intent.getStringExtra("roomName")
        userName = intent.getStringExtra("userName")

        setContent {
            LeyaaTheme {
                MemberAddScreen(
                    roomID = roomID ?: "",
                    roomName = roomName ?: "",
                    userName = userName ?: "",
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberAddScreen(
    roomID: String,
    roomName: String,
    userName: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite to $roomName", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Image(
                painter = painterResource(id = R.drawable.add_member),
                contentDescription = "Add Member Image",
                modifier = Modifier
                    .size(200.dp)
                    .padding(16.dp)
            )

            OutlinedButton(
                onClick = {
                    val shareText = "Leyaa – Shop Better, Live Organized.\n\n" +
                            "Download here:\n" +
                            secretKey.playStoreURL

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Leyaa"))
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Icon(Icons.Filled.Share, contentDescription = "Share App", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share App")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Invite via Email",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message (optional)") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = "Message") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val emailStr = email.trim().lowercase(Locale.ROOT)
                    val messageStr = message.trim()

                    if (emailStr.isEmpty()) {
                        Toast.makeText(context, "Oops! Email is empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").whereEqualTo("email", emailStr).get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val qs = task.result
                                if (qs != null && qs.isEmpty) {
                                    isLoading = false
                                    Toast.makeText(context, "Uh Oh! It looks like the person you're trying to add isn't registered with us. Share the app with them", Toast.LENGTH_LONG).show()
                                } else {
                                    val request = hashMapOf<String, Any>(
                                        "message" to messageStr,
                                        "roomID" to roomID,
                                        "roomName" to roomName,
                                        "senderName" to userName,
                                        "receiverEmail" to emailStr
                                    )

                                    db.collection("roomRequest").add(request)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show()
                                            
                                            // Send Notification
                                            val firstDoc = qs!!.documents.firstOrNull()
                                            if (firstDoc != null) {
                                                val userDeviceToken = firstDoc.getString("deviceToken") ?: ""
                                                val notifTitle = "Room Join Invitation"
                                                val notifMessage = "$userName invited you to join $roomName"
                                                
                                                val notificationsSender = FcmNotificationsSender(
                                                    userDeviceToken,
                                                    notifTitle,
                                                    notifMessage,
                                                    context.applicationContext,
                                                    context as android.app.Activity
                                                )
                                                notificationsSender.SendNotifications()
                                            }

                                            email = ""
                                            message = ""
                                            isLoading = false
                                        }
                                        .addOnFailureListener {
                                            isLoading = false
                                            Toast.makeText(context, "Failed to send request", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Error checking user", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("SEND INVITE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
