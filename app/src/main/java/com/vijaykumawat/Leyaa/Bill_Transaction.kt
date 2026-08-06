package com.vijaykumawat.Leyaa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BillTransactionData(
    val docId: String,
    val itemName: String,
    val itemPrice: Double,
    val timestamp: Date?,
    val payer: String,
    val contributor: String
)

class Bill_Transaction : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        val memberID = intent.extras?.getString("memberID") ?: ""
        
        setContent {
            LeyaaTheme {
                BillTransactionScreen(
                    roomID = roomID,
                    memberID = memberID,
                    onNavigateBack = { finish() },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillTransactionScreen(
    roomID: String,
    memberID: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var memberName by remember { mutableStateOf("Transactions") }
    var roomName by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var memberDevToken by remember { mutableStateOf("") }
    
    var getTransactions by remember { mutableStateOf<List<BillTransactionData>>(emptyList()) }
    var payTransactions by remember { mutableStateOf<List<BillTransactionData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(roomID, memberID) {
        if (roomID.isEmpty() || memberID.isEmpty()) return@LaunchedEffect
        
        try {
            val userDoc = db.collection("users").document(currentUid).get().await()
            userName = userDoc.getString("fullname") ?: ""
            
            val memberDoc = db.collection("users").document(memberID).get().await()
            memberName = memberDoc.getString("fullname") ?: "Transactions"
            memberDevToken = memberDoc.getString("deviceToken") ?: ""
            
            val roomDoc = db.collection("rooms").document(roomID).get().await()
            roomName = roomDoc.getString("title") ?: ""
        } catch (e: Exception) {}
        
        // Listen to "GET" transactions (they owe me)
        db.collection("${roomID}_BILLS")
            .whereEqualTo("contributor", memberID)
            .whereEqualTo("payer", currentUid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = mutableListOf<BillTransactionData>()
                    for (doc in snapshot.documents) {
                        list.add(
                            BillTransactionData(
                                docId = doc.id,
                                itemName = doc.getString("itemName") ?: "",
                                itemPrice = doc.getDouble("itemPrice") ?: 0.0,
                                timestamp = doc.getDate("timestamp"),
                                payer = currentUid,
                                contributor = memberID
                            )
                        )
                    }
                    getTransactions = list
                }
            }
            
        // Listen to "PAY" transactions (I owe them)
        db.collection("${roomID}_BILLS")
            .whereEqualTo("contributor", currentUid)
            .whereEqualTo("payer", memberID)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = mutableListOf<BillTransactionData>()
                    for (doc in snapshot.documents) {
                        list.add(
                            BillTransactionData(
                                docId = doc.id,
                                itemName = doc.getString("itemName") ?: "",
                                itemPrice = doc.getDouble("itemPrice") ?: 0.0,
                                timestamp = doc.getDate("timestamp"),
                                payer = memberID,
                                contributor = currentUid
                            )
                        )
                    }
                    payTransactions = list
                    isLoading = false
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(memberName, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Notifications / Dues Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        val title = "Room: $roomName"
                        val message = "$userName requested you to clear dues."
                        val sender = FcmNotificationsSender(memberDevToken, title, message, context.applicationContext, context as android.app.Activity)
                        sender.SendNotifications()
                        Toast.makeText(context, "Notification Sent", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ask", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val title = "Room: $roomName"
                        val message = "$userName has cleared dues."
                        val sender = FcmNotificationsSender(memberDevToken, title, message, context.applicationContext, context as android.app.Activity)
                        sender.SendNotifications()
                        Toast.makeText(context, "Notification Sent", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Paid", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                }
            }
            
            Divider()

            Row(modifier = Modifier.fillMaxSize()) {
                // GET Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GET",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp
                    )
                    
                    if (getTransactions.isEmpty() && !isLoading) {
                        Text("No records", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp)) {
                            items(getTransactions) { transaction ->
                                TransactionCard(transaction = transaction, onDelete = {
                                    db.collection("${roomID}_BILLS").document(transaction.docId).delete()
                                })
                            }
                        }
                    }
                }

                // Vertical Divider
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )

                // PAY Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PAY",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp
                    )
                    
                    if (payTransactions.isEmpty() && !isLoading) {
                        Text("No records", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp)) {
                            items(payTransactions) { transaction ->
                                TransactionCard(transaction = transaction, onDelete = {
                                    db.collection("${roomID}_BILLS").document(transaction.docId).delete()
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: BillTransactionData, onDelete: () -> Unit) {
    val formatter = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val dateString = transaction.timestamp?.let { formatter.format(it) } ?: ""

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.itemName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete Bill",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$${String.format("%.1f", transaction.itemPrice)}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = dateString,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
