package com.vijaykumawat.Leyaa

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import kotlinx.coroutines.tasks.await

data class SplitMember(
    val uid: String,
    val fullname: String,
    val avatar: String,
    var payAmount: Double = 0.0,
    var getAmount: Double = 0.0
)

class SplitBill_Member_Info : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val roomName = intent.extras?.getString("roomName") ?: "Split Bill"
        val userName = intent.extras?.getString("userName") ?: "Someone"

        setContent {
            LeyaaTheme {
                SplitBillDashboardScreen(
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
fun SplitBillDashboardScreen(
    roomID: String,
    roomName: String,
    userName: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    var members by remember { mutableStateOf<List<SplitMember>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(roomID) {
        if (roomID.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val roomDoc = db.collection("rooms").document(roomID).get().await()
            val memberIDs = roomDoc.get("members") as? List<String> ?: emptyList()
            
            val fetchedMembers = mutableListOf<SplitMember>()
            
            for (memberID in memberIDs) {
                if (memberID != currentUserId) {
                    val userDoc = db.collection("users").document(memberID).get().await()
                    val fullname = userDoc.getString("fullname") ?: "Unknown"
                    val avatar = userDoc.getString("avatar") ?: ""
                    
                    val memberData = SplitMember(uid = memberID, fullname = fullname, avatar = avatar)
                    
                    // Fetch "Get" Amount (What they owe me)
                    val getDocs = db.collection("${roomID}_BILLS")
                        .whereEqualTo("contributor", memberID)
                        .whereEqualTo("payer", currentUserId)
                        .get().await()
                    
                    var getSum = 0.0
                    for (doc in getDocs.documents) {
                        getSum += doc.getDouble("itemPrice") ?: 0.0
                    }
                    memberData.getAmount = getSum
                    
                    // Fetch "Pay" Amount (What I owe them)
                    val payDocs = db.collection("${roomID}_BILLS")
                        .whereEqualTo("payer", memberID)
                        .whereEqualTo("contributor", currentUserId)
                        .get().await()
                        
                    var paySum = 0.0
                    for (doc in payDocs.documents) {
                        paySum += doc.getDouble("itemPrice") ?: 0.0
                    }
                    memberData.payAmount = paySum
                    
                    fetchedMembers.add(memberData)
                }
            }
            members = fetchedMembers
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }

    Scaffold(
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
                        db.collection("rooms").document(roomID).get().addOnSuccessListener { doc ->
                            val memberList = doc.get("members") as? List<String> ?: return@addOnSuccessListener
                            for (member in memberList) {
                                if (member != currentUserId) {
                                    db.collection("users").document(member).get().addOnSuccessListener { userDoc ->
                                        val deviceToken = userDoc.getString("deviceToken")
                                        if (deviceToken != null) {
                                            val title = "Room: $roomName"
                                            val msg = "$userName posted a new bill."
                                            val sender = FcmNotificationsSender(deviceToken, title, msg, context, context as android.app.Activity)
                                            sender.SendNotifications()
                                        }
                                    }
                                }
                            }
                            Toast.makeText(context, "Everyone notified of new bill", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Remind")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, Split_Add_Bill::class.java)
                    intent.putExtra("roomID", roomID)
                    context.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, "Add Bill")
            }
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
                    text = "No other members to split bills with.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(members) { member ->
                        SplitMemberCard(
                            member = member,
                            context = context,
                            onClick = {
                                val intent = Intent(context, Bill_Transaction::class.java)
                                intent.putExtra("memberID", member.uid)
                                intent.putExtra("roomID", roomID)
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplitMemberCard(
    member: SplitMember,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val resId = context.resources.getIdentifier(member.avatar, "drawable", context.packageName)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Pay: $${String.format("%.1f", member.payAmount)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Get: $${String.format("%.1f", member.getAmount)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val netAmount = member.getAmount - member.payAmount
                val netColor = if (netAmount >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                val netPrefix = if (netAmount >= 0) "+" else "-"
                
                Text(
                    text = "Net: $netPrefix$${String.format("%.1f", Math.abs(netAmount))}",
                    fontSize = 15.sp,
                    color = netColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
