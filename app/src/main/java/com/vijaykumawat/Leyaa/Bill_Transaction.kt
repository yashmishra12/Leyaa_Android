package com.vijaykumawat.Leyaa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
    var deletedTransactions by remember { mutableStateOf<List<BillTransactionData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var netTotalForDialog by remember { mutableStateOf(0.0) }
    var settleAmountText by remember { mutableStateOf("") }
    
    if (showSettleDialog) {
        val maxAmount = kotlin.math.abs(netTotalForDialog)
        
        AlertDialog(
            onDismissRequest = { showSettleDialog = false },
            title = { Text("Settle Up") },
            text = {
                Column {
                    Text("How much are you settling? (Max: \$${String.format(java.util.Locale.US, "%.2f", maxAmount)})")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = settleAmountText,
                        onValueChange = { settleAmountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Amount") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = settleAmountText.toDoubleOrNull() ?: 0.0
                        if (amount <= 0 || amount > maxAmount) {
                            Toast.makeText(context, "Invalid amount.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        showSettleDialog = false
                        
                        if (amount >= maxAmount - 0.01) {
                            val allTransactions = getTransactions + payTransactions
                            val activeCollection = db.collection("${roomID}_BILLS")
                            val deletedCollection = db.collection("${roomID}_BILLS_DELETED")
                            
                            db.runBatch { batch ->
                                for (tx in allTransactions) {
                                    val txData = hashMapOf(
                                        "itemName" to tx.itemName,
                                        "itemPrice" to tx.itemPrice,
                                        "timestamp" to tx.timestamp,
                                        "payer" to tx.payer,
                                        "contributor" to tx.contributor
                                    )
                                    batch.delete(activeCollection.document(tx.docId))
                                    batch.set(deletedCollection.document(tx.docId), txData)
                                }
                            }.addOnSuccessListener {
                                val title = "Room: $roomName"
                                val message = "$userName has cleared all dues."
                                val sender = FcmNotificationsSender(memberDevToken, title, message, context.applicationContext, context as android.app.Activity)
                                sender.SendNotifications()
                                Toast.makeText(context, "All bills settled!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val payerId = if (netTotalForDialog > 0) memberID else currentUid
                            val contributorId = if (netTotalForDialog > 0) currentUid else memberID
                            
                            val partialBill = hashMapOf(
                                "itemName" to "Partial Settlement",
                                "itemPrice" to amount,
                                "timestamp" to java.util.Date(),
                                "payer" to payerId,
                                "contributor" to contributorId
                            )
                            
                            db.collection("${roomID}_BILLS").add(partialBill).addOnSuccessListener {
                                val title = "Room: $roomName"
                                val message = "$userName recorded a partial payment of \$${String.format(java.util.Locale.US, "%.2f", amount)}."
                                val sender = FcmNotificationsSender(memberDevToken, title, message, context.applicationContext, context as android.app.Activity)
                                sender.SendNotifications()
                                Toast.makeText(context, "Partial payment recorded!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    getTransactions = list.sortedByDescending { it.timestamp }
                }
            }
            
        // Listen to "PAY" transactions (I owe them)
        db.collection("${roomID}_BILLS")
            .whereEqualTo("contributor", currentUid)
            .whereEqualTo("payer", memberID)
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
                    payTransactions = list.sortedByDescending { it.timestamp }
                    isLoading = false
                }
            }
            
        // Listen to DELETED transactions
        db.collection("${roomID}_BILLS_DELETED")
            .limit(30)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = mutableListOf<BillTransactionData>()
                    for (doc in snapshot.documents) {
                        val payer = doc.getString("payer") ?: ""
                        val contributor = doc.getString("contributor") ?: ""
                        
                        if ((payer == currentUid && contributor == memberID) || 
                            (payer == memberID && contributor == currentUid)) {
                            list.add(
                                BillTransactionData(
                                    docId = doc.id,
                                    itemName = doc.getString("itemName") ?: "",
                                    itemPrice = doc.getDouble("itemPrice") ?: 0.0,
                                    timestamp = doc.getDate("timestamp"),
                                    payer = payer,
                                    contributor = contributor
                                )
                            )
                        }
                    }
                    deletedTransactions = list.sortedByDescending { it.timestamp }
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
                        val totalNet = getTransactions.sumOf { it.itemPrice } - payTransactions.sumOf { it.itemPrice }
                        if (totalNet == 0.0) {
                            Toast.makeText(context, "No active balance to settle.", Toast.LENGTH_SHORT).show()
                        } else {
                            netTotalForDialog = totalNet
                            settleAmountText = String.format(java.util.Locale.US, "%.2f", kotlin.math.abs(totalNet))
                            showSettleDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Settle Up", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
                }
            }
            
            val getTotal = getTransactions.sumOf { it.itemPrice }
            val payTotal = payTransactions.sumOf { it.itemPrice }
            val netTotal = getTotal - payTotal
            
            val netSummaryText = if (netTotal > 0) {
                "Net: $memberName owes you \$${String.format("%.1f", netTotal)}"
            } else if (netTotal < 0) {
                "Net: You owe $memberName \$${String.format("%.1f", -netTotal)}"
            } else {
                "Net: Settled up!"
            }

            Text(
                text = netSummaryText,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (netTotal > 0) MaterialTheme.colorScheme.primary else if (netTotal < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            
            Divider()

            Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
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
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        fontSize = 18.sp
                    )
                    Text(
                        text = "\$${String.format("%.1f", getTotal)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (getTransactions.isEmpty() && !isLoading) {
                        Text("No records", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp)) {
                            items(getTransactions, key = { it.docId }) { transaction ->
                                TransactionCard(transaction = transaction, onDelete = {
                                    val transactionData = hashMapOf(
                                        "itemName" to transaction.itemName,
                                        "itemPrice" to transaction.itemPrice,
                                        "timestamp" to transaction.timestamp,
                                        "payer" to transaction.payer,
                                        "contributor" to transaction.contributor
                                    )
                                    val activeRef = db.collection("${roomID}_BILLS").document(transaction.docId)
                                    val deletedRef = db.collection("${roomID}_BILLS_DELETED").document(transaction.docId)
                                    
                                    activeRef.delete().addOnSuccessListener {
                                        deletedRef.set(transactionData)
                                        Toast.makeText(context, "Bill Deleted", Toast.LENGTH_SHORT).show()
                                    }
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
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        fontSize = 18.sp
                    )
                    Text(
                        text = "\$${String.format("%.1f", payTotal)}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (payTransactions.isEmpty() && !isLoading) {
                        Text("No records", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(contentPadding = PaddingValues(8.dp)) {
                            items(payTransactions, key = { it.docId }) { transaction ->
                                TransactionCard(transaction = transaction, onDelete = {
                                    val transactionData = hashMapOf(
                                        "itemName" to transaction.itemName,
                                        "itemPrice" to transaction.itemPrice,
                                        "timestamp" to transaction.timestamp,
                                        "payer" to transaction.payer,
                                        "contributor" to transaction.contributor
                                    )
                                    val activeRef = db.collection("${roomID}_BILLS").document(transaction.docId)
                                    val deletedRef = db.collection("${roomID}_BILLS_DELETED").document(transaction.docId)
                                    
                                    activeRef.delete().addOnSuccessListener {
                                        deletedRef.set(transactionData)
                                        Toast.makeText(context, "Bill Deleted", Toast.LENGTH_SHORT).show()
                                    }
                                })
                            }
                        }
                    }
                }
            }
            
            if (deletedTransactions.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    "Recently Removed",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                LazyRow(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 96.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(deletedTransactions, key = { it.docId }) { transaction ->
                        DeletedTransactionCard(
                            transaction = transaction,
                            onRestore = {
                                val transactionData = hashMapOf(
                                    "itemName" to transaction.itemName,
                                    "itemPrice" to transaction.itemPrice,
                                    "timestamp" to transaction.timestamp,
                                    "payer" to transaction.payer,
                                    "contributor" to transaction.contributor
                                )
                                val activeRef = db.collection("${roomID}_BILLS").document(transaction.docId)
                                val deletedRef = db.collection("${roomID}_BILLS_DELETED").document(transaction.docId)
                                
                                deletedRef.delete().addOnSuccessListener {
                                    activeRef.set(transactionData)
                                    Toast.makeText(context, "Bill Restored", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
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
                text = "\$${String.format("%.1f", transaction.itemPrice)}",
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

@Composable
fun DeletedTransactionCard(transaction: BillTransactionData, onRestore: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onRestore)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Restore",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = transaction.itemName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "\$${String.format("%.1f", transaction.itemPrice)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
