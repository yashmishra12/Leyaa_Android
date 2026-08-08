package com.vijaykumawat.Leyaa

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class ThreadComment(
    val id: String = "",
    val text: String = "",
    val authorName: String = "",
    val timestamp: Date? = null
)

class ThreadedDiscussionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: ""
        val userName = intent.extras?.getString("userName") ?: ""
        val postID = intent.extras?.getString("postID") ?: ""
        val postTitle = intent.extras?.getString("postTitle") ?: ""
        val postBody = intent.extras?.getString("postBody") ?: ""
        val postAuthor = intent.extras?.getString("postAuthor") ?: ""

        setContent {
            LeyaaTheme {
                ThreadScreen(
                    roomID = roomID,
                    roomName = roomName,
                    userName = userName,
                    postID = postID,
                    postTitle = postTitle,
                    postBody = postBody,
                    postAuthor = postAuthor,
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    roomID: String,
    roomName: String,
    userName: String,
    postID: String,
    postTitle: String,
    postBody: String,
    postAuthor: String,
    onNavigateBack: () -> Unit,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    var comments by remember { mutableStateOf<List<ThreadComment>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(postID) {
        val commentsRef = db.collection("rooms").document(roomID)
            .collection("noticeBoard").document(postID)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            
        val listener = commentsRef.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                val fetched = snapshot.documents.map { doc ->
                    ThreadComment(
                        id = doc.id,
                        text = doc.getString("text") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        timestamp = doc.getDate("timestamp")
                    )
                }
                comments = fetched
                
                coroutineScope.launch {
                    if (fetched.isNotEmpty()) {
                        listState.animateScrollToItem(fetched.size) // +1 for the header
                    }
                }
            }
        }
        onDispose { listener.remove() }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                title = { Text("Discussion Thread") },
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header (The original post)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = postTitle, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = postBody, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = "Posted by $postAuthor", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Replies", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                
                // Comments
                items(comments) { comment ->
                    val isMe = comment.authorName == userName
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer)
                                .padding(12.dp)
                                .fillMaxWidth(0.8f)
                        ) {
                            Text(
                                text = comment.authorName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = comment.text,
                                fontSize = 15.sp,
                                color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
            
            // Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Write a reply...") },
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val commentId = UUID.randomUUID().toString()
                                val commentData = mapOf(
                                    "text" to inputText,
                                    "authorName" to userName,
                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )
                                db.collection("rooms").document(roomID)
                                    .collection("noticeBoard").document(postID)
                                    .collection("comments").document(commentId)
                                    .set(commentData)
                                    .addOnSuccessListener {
                                        sendRoomNotification("replied to $postTitle", roomID, roomName, userName, context)
                                    }
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
