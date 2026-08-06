package com.vijaykumawat.Leyaa

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RoomListActivity : ComponentActivity() {

    private val viewModel: RoomListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LeyaaTheme {
                RoomListScreen(
                    viewModel = viewModel,
                    onCreateRoomClick = {
                        startActivity(Intent(this, Room_Creation::class.java))
                    },
                    onRoomClick = { roomId ->
                        val intent = Intent(this, Inside_Room_Activity::class.java).apply {
                            putExtra("document_ID", roomId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.startListening()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopListening()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomListScreen(
    viewModel: RoomListViewModel,
    onCreateRoomClick: () -> Unit,
    onRoomClick: (String) -> Unit
) {
    val rooms by viewModel.rooms.observeAsState(emptyList<RoomModel>())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Rooms", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoomClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Room")
            }
        },
        bottomBar = {
            val context = androidx.compose.ui.platform.LocalContext.current
            BottomNavBar(
                currentRoute = "rooms",
                onNavigate = { route ->
                    val intent = when (route) {
                        "rooms" -> Intent(context, RoomListActivity::class.java)
                        "invites" -> Intent(context, InvitationListActivity::class.java)
                        "alarms" -> Intent(context, Alarm_MainActivity::class.java)
                        "profile" -> Intent(context, ProfilePage::class.java)
                        else -> null
                    }
                    intent?.let { 
                        context.startActivity(it)
                        // Cast context to Activity if possible to override transition
                        (context as? android.app.Activity)?.overridePendingTransition(0, 0)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No rooms found. Create one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms, key = { it.id }) { room ->
                    RoomCard(room = room, onClick = { onRoomClick(room.id) })
                }
            }
        }
    }
}

@Composable
fun RoomCard(room: RoomModel, onClick: () -> Unit) {
    var netBalance by remember { mutableStateOf<Double?>(null) }
    var lastMessage by remember { mutableStateOf<String?>(null) }
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    LaunchedEffect(room.id) {
        if (currentUserId.isNotEmpty()) {
            try {
                var getSum = 0.0
                var paySum = 0.0
                
                val getDocs = db.collection("${room.id}_BILLS")
                    .whereEqualTo("payer", currentUserId)
                    .get().await()
                for (doc in getDocs.documents) getSum += doc.getDouble("itemPrice") ?: 0.0
                
                val payDocs = db.collection("${room.id}_BILLS")
                    .whereEqualTo("contributor", currentUserId)
                    .get().await()
                for (doc in payDocs.documents) paySum += doc.getDouble("itemPrice") ?: 0.0
                
                netBalance = getSum - paySum
            } catch (e: Exception) {
                netBalance = 0.0
            }
            
            try {
                val msgs = db.collection(room.id)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get().await()
                if (msgs.documents.isNotEmpty()) {
                    lastMessage = msgs.documents.first().getString("text")
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (room.avatar.isNotEmpty()) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val sanitizedAvatar = SanitizeItemName.sanitizeItemName(room.avatar)
                        val resId = context.resources.getIdentifier(sanitizedAvatar, "drawable", context.packageName)
                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = room.avatar,
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            Text(
                                text = room.title.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    } else {
                        Text(
                            text = room.title.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = room.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${room.itemCount} items: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        val context = androidx.compose.ui.platform.LocalContext.current
                        room.topItems.forEach { itemName ->
                            val sanitizedName = SanitizeItemName.sanitizeItemName(itemName)
                            val resId = context.resources.getIdentifier(sanitizedName, "drawable", context.packageName)
                            if (resId != 0) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = itemName,
                                    modifier = Modifier.size(16.dp).padding(end = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "Message",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = lastMessage ?: "No recent messages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                if (netBalance != null) {
                    val formattedBalance = String.format(java.util.Locale.US, "$%.2f", Math.abs(netBalance!!))
                    if (netBalance!! > 0.01) {
                        Text(
                            text = "Owed $formattedBalance",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF4CAF50), // Green
                            fontWeight = FontWeight.Bold
                        )
                    } else if (netBalance!! < -0.01) {
                        Text(
                            text = "Owe $formattedBalance",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Settled up",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
