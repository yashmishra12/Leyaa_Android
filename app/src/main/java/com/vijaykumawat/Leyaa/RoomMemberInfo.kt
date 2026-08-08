package com.vijaykumawat.Leyaa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme

data class MemberData(
    val avatar: String,
    val fullname: String,
    val email: String
)

class RoomMemberInfo : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        
        setContent {
            LeyaaTheme {
                RoomMemberScreen(
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
fun RoomMemberScreen(
    roomID: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    var members by remember { mutableStateOf<List<MemberData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(roomID) {
        if (roomID.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("rooms").document(roomID).get().addOnSuccessListener { doc ->
            val memberIDs = doc.get("members") as? List<String> ?: emptyList()
            if (memberIDs.isEmpty()) {
                isLoading = false
            } else {
                val fetchedMembers = mutableListOf<MemberData>()
                var completedFetches = 0
                
                memberIDs.forEach { memberID ->
                    db.collection("users").document(memberID).get().addOnSuccessListener { userDoc ->
                        val fullname = userDoc.getString("fullname") ?: "Unknown"
                        val email = userDoc.getString("email") ?: ""
                        val avatar = userDoc.getString("avatar") ?: ""
                        
                        fetchedMembers.add(MemberData(avatar, fullname, email))
                        completedFetches++
                        
                        if (completedFetches == memberIDs.size) {
                            members = fetchedMembers
                            isLoading = false
                        }
                    }.addOnFailureListener {
                        completedFetches++
                        if (completedFetches == memberIDs.size) {
                            members = fetchedMembers
                            isLoading = false
                        }
                    }
                }
            }
        }.addOnFailureListener {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Room Members", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (members.isEmpty()) {
                Text(
                    text = "No members found.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(members) { member ->
                        MemberCard(member = member, context = context)
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: MemberData, context: android.content.Context) {
    val resId = context.resources.getIdentifier(member.avatar, "drawable", context.packageName)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = member.fullname,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.fullname,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                if (member.email.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = member.email,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            if (member.email.isNotBlank()) {
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                TextButton(onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(member.email))
                    android.widget.Toast.makeText(context, "Email copied", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Text("Copy")
                }
            }
        }
    }
}
