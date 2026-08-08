package com.vijaykumawat.Leyaa

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMessage(
    val id: String,
    val senderID: String,
    val text: String,
    val timestamp: Date?
)

class ChatRoom : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: "Chat Room"

        setContent {
            LeyaaTheme {
                ChatScreen(
                    roomID = roomID,
                    roomName = roomName,
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    roomID: String,
    roomName: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    
    // User info cache for avatars and names
    val userCache = remember { mutableStateMapOf<String, MemberData>() }
    var currentUserName by remember { mutableStateOf("Someone") }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Fetch current user name
    LaunchedEffect(currentUserId) {
        db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            currentUserName = doc.getString("fullname") ?: "Someone"
        }
    }

    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Listen to messages
    DisposableEffect(roomID) {
        val listener = db.collection(roomID)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { doc ->
                        val text = doc.getString("text") ?: return@mapNotNull null
                        val senderID = doc.getString("senderID") ?: return@mapNotNull null
                        val timestamp = doc.getDate("timestamp")
                        val id = doc.getString("id") ?: doc.id
                        
                        // Prefetch user data if not in cache
                        if (!userCache.containsKey(senderID)) {
                            db.collection("users").document(senderID).get().addOnSuccessListener { userDoc ->
                                val fullname = userDoc.getString("fullname") ?: "Unknown"
                                val email = userDoc.getString("email") ?: ""
                                val avatar = userDoc.getString("avatar") ?: ""
                                userCache[senderID] = MemberData(avatar, fullname, email)
                            }
                        }
                        
                        ChatMessage(id, senderID, text, timestamp)
                    }
                    messages = msgs
                    
                    // Scroll to bottom when new messages arrive
                    coroutineScope.launch {
                        if (msgs.isNotEmpty()) {
                            listState.animateScrollToItem(msgs.size - 1)
                        }
                    }
                }
            }
            
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text(roomName, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Wave functionality (notify all)
                        db.collection("rooms").document(roomID).get().addOnSuccessListener { doc ->
                            val members = doc.get("members") as? List<String> ?: return@addOnSuccessListener
                            for (member in members) {
                                if (member != currentUserId) {
                                    db.collection("users").document(member).get().addOnSuccessListener { userDoc ->
                                        val deviceToken = userDoc.getString("deviceToken")
                                        if (deviceToken != null) {
                                            val title = "Room: $roomName"
                                            val msg = "$currentUserName has posted a new message."
                                            val sender = FcmNotificationsSender(deviceToken, title, msg, context, context as android.app.Activity)
                                            sender.SendNotifications()
                                        }
                                    }
                                }
                            }
                            Toast.makeText(context, "Everyone notified of your new post.", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notify All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val text = inputText.trim()
                                inputText = ""
                                
                                val docRef = db.collection(roomID).document()
                                val chatObj = hashMapOf(
                                    "id" to docRef.id,
                                    "senderID" to currentUserId,
                                    "text" to text,
                                    "timestamp" to Date()
                                )
                                
                                docRef.set(chatObj)
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                val isMe = message.senderID == currentUserId
                val senderData = userCache[message.senderID]
                val senderName = if (isMe) "You" else (senderData?.fullname ?: "Loading...")
                
                MessageBubble(
                    message = message,
                    isMe = isMe,
                    senderName = senderName,
                    avatar = senderData?.avatar,
                    context = context,
                    onDelete = {
                        if (isMe) {
                            db.collection(roomID).document(message.id).delete()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "You can only delete your own messages", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    senderName: String,
    avatar: String?,
    context: Context,
    onDelete: () -> Unit
) {
    val formatter = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
    val timeString = message.timestamp?.let { formatter.format(it) } ?: ""

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isMe) {
            val resId = avatar?.let { context.resources.getIdentifier(it, "drawable", context.packageName) } ?: 0
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            if (isMe) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                onDelete()
                            }
                        }
                    )
                }
        ) {
            Surface(
                color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isMe) 20.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 20.dp
                ),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$senderName • $timeString",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (isMe) {
            Spacer(modifier = Modifier.width(8.dp))
            val resId = avatar?.let { context.resources.getIdentifier(it, "drawable", context.packageName) } ?: 0
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
