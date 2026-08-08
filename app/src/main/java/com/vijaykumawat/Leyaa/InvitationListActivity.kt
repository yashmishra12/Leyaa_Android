package com.vijaykumawat.Leyaa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme

data class InviteData(
    val id: String,
    val senderName: String,
    val roomName: String,
    val message: String,
    val roomId: String
)

class InvitationListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeyaaTheme {
                InvitationScreen(
                    onBottomNavClick = { intent ->
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvitationScreen(
    onBottomNavClick: (Intent) -> Unit,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userEmail = auth.currentUser?.email ?: return
    val userId = auth.currentUser?.uid ?: return

    var invites by remember { mutableStateOf<List<InviteData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("roomRequest")
            .whereEqualTo("receiverEmail", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val newInvites = snapshot.documents.mapNotNull { doc ->
                        val senderName = doc.getString("senderName") ?: ""
                        val roomName = doc.getString("roomName") ?: ""
                        var message = doc.getString("message") ?: ""
                        val roomId = doc.getString("roomID") ?: ""
                        
                        if (message.isEmpty()) {
                            message = "Hey! Accept my room request. Thanks"
                        }
                        
                        InviteData(doc.id, senderName, roomName, message, roomId)
                    }
                    invites = newInvites
                    isLoading = false
                }
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = "invites",
                onNavigate = { route ->
                    val intent = when (route) {
                        "rooms" -> Intent(context, RoomListActivity::class.java)
                        "invites" -> Intent(context, InvitationListActivity::class.java)
                        "alarms" -> Intent(context, Alarm_MainActivity::class.java)
                        "profile" -> Intent(context, ProfilePage::class.java)
                        else -> null
                    }
                    intent?.let { 
                        it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        onBottomNavClick(it) 
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                text = "Invitations",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(24.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (invites.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.relax7),
                        contentDescription = "No Invites",
                        modifier = Modifier.size(250.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sit back and relax.\nYou have no pending invitations.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(invites) { invite ->
                        InviteCard(
                            invite = invite,
                            onAccept = {
                                db.collection("rooms").document(invite.roomId)
                                    .update("members", FieldValue.arrayUnion(userId))
                                    .addOnSuccessListener {
                                        db.collection("roomRequest").document(invite.id).delete()
                                        Toast.makeText(context, "Added to room!", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            onDecline = {
                                db.collection("roomRequest").document(invite.id).delete()
                                Toast.makeText(context, "Declined.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InviteCard(
    invite: InviteData,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var showDeclineDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "${invite.senderName} invited you to",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = invite.roomName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "\"${invite.message}\"",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDeclineDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Decline", color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Accept")
                }
            }
        }
    }

    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest = { showDeclineDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("Do you really want to decline this room invitation?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeclineDialog = false
                    onDecline()
                }) {
                    Text("Yes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}
