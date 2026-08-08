package com.vijaykumawat.Leyaa

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import java.util.Date
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale

data class NoticePost(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val authorName: String = "",
    val isPinned: Boolean = false,
    val timestamp: Date? = null
)

class RoomNoticeBoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: ""
        val userName = intent.extras?.getString("userName") ?: ""

        setContent {
            LeyaaTheme {
                NoticeBoardScreen(
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
fun NoticeBoardScreen(
    roomID: String,
    roomName: String,
    userName: String,
    onNavigateBack: () -> Unit,
    context: Context
) {
    val db = FirebaseFirestore.getInstance()
    var posts by remember { mutableStateOf<List<NoticePost>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Add dialog states
    var newTitle by remember { mutableStateOf("") }
    var newBody by remember { mutableStateOf("") }
    var newIsPinned by remember { mutableStateOf(false) }

    DisposableEffect(roomID) {
        val postsRef = db.collection("rooms").document(roomID).collection("noticeBoard")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            
        val listener = postsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                isLoading = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetched = snapshot.documents.map { doc ->
                    NoticePost(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        body = doc.getString("body") ?: "",
                        authorName = doc.getString("authorName") ?: "",
                        isPinned = doc.getBoolean("isPinned") ?: false,
                        timestamp = doc.getDate("timestamp")
                    )
                }
                
                // Sort so pinned are at the top, then by timestamp (which they already are)
                posts = fetched.sortedByDescending { it.isPinned }
                isLoading = false
            }
        }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notice Board & Chat") },
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
                Icon(Icons.Filled.Add, contentDescription = "Add Post")
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
            } else if (posts.isEmpty()) {
                Text(
                    "No posts yet. Start a discussion!",
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
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            onClick = {
                                val intent = Intent(context, ThreadedDiscussionActivity::class.java).apply {
                                    putExtra("roomID", roomID)
                                    putExtra("roomName", roomName)
                                    putExtra("userName", userName)
                                    putExtra("postID", post.id)
                                    putExtra("postTitle", post.title)
                                    putExtra("postBody", post.body)
                                    putExtra("postAuthor", post.authorName)
                                }
                                context.startActivity(intent)
                            },
                            onTogglePin = {
                                db.collection("rooms").document(roomID).collection("noticeBoard")
                                    .document(post.id).update("isPinned", !post.isPinned)
                            },
                            onDelete = {
                                db.collection("rooms").document(roomID).collection("noticeBoard")
                                    .document(post.id).delete()
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
            title = { Text("New Post") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBody,
                        onValueChange = { newBody = it },
                        label = { Text("Details / Message") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = newIsPinned, onCheckedChange = { newIsPinned = it })
                        Text("Pin to top")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isNotBlank()) {
                        val postId = UUID.randomUUID().toString()
                        val postData = mapOf(
                            "title" to newTitle,
                            "body" to newBody,
                            "authorName" to userName,
                            "isPinned" to newIsPinned,
                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                        db.collection("rooms").document(roomID).collection("noticeBoard")
                            .document(postId).set(postData)
                            .addOnSuccessListener {
                                sendRoomNotification("created a new post: $newTitle", roomID, roomName, userName, context)
                                Toast.makeText(context, "Post Added", Toast.LENGTH_SHORT).show()
                            }
                        showAddDialog = false
                        newTitle = ""
                        newBody = ""
                        newIsPinned = false
                    } else {
                        Toast.makeText(context, "Title is required", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Post")
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
fun PostCard(post: NoticePost, onClick: () -> Unit, onTogglePin: () -> Unit, onDelete: () -> Unit) {
    val formatter = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val timeStr = post.timestamp?.let { formatter.format(it) } ?: "Just now"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (post.isPinned) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (post.isPinned) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = post.title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onTogglePin, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (post.isPinned) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Pin",
                        tint = if (post.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.body, 
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "By ${post.authorName} • $timeStr", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
