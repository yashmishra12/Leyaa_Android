package com.vijaykumawat.Leyaa

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import kotlinx.coroutines.tasks.await
import java.util.Date

class Split_Add_Bill : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("roomID") ?: ""
        
        setContent {
            LeyaaTheme {
                SplitAddBillScreen(
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
fun SplitAddBillScreen(
    roomID: String,
    onNavigateBack: () -> Unit,
    context: android.content.Context
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    var roomName by remember { mutableStateOf("Add Bill") }
    var itemName by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }

    
    // Store member data and their entered bill amounts
    var members by remember { mutableStateOf<List<MemberData>>(emptyList()) }
    val memberAmounts = remember { mutableStateMapOf<String, String>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(roomID) {
        if (roomID.isEmpty()) return@LaunchedEffect
        
        try {
            val roomDoc = db.collection("rooms").document(roomID).get().await()
            roomName = roomDoc.getString("title") ?: "Add Bill"
            
            val memberIDs = roomDoc.get("members") as? List<String> ?: emptyList()
            val fetchedMembers = mutableListOf<MemberData>()
            
            for (memberID in memberIDs) {
                if (memberID != currentUserId) {
                    val userDoc = db.collection("users").document(memberID).get().await()
                    val fullname = userDoc.getString("fullname") ?: "Unknown"
                    val email = userDoc.getString("email") ?: ""
                    val avatar = userDoc.getString("avatar") ?: ""
                    
                    fetchedMembers.add(MemberData(avatar, fullname, memberID)) // Using email field for uid
                    memberAmounts[memberID] = ""
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            if (itemName.trim().isEmpty()) {
                                Toast.makeText(context, "Item Name is required.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val billsToSave = mutableListOf<Map<String, Any>>()
                            for ((uid, amountStr) in memberAmounts) {
                                val amount = amountStr.toDoubleOrNull() ?: continue
                                if (amount > 0) {
                                    billsToSave.add(
                                        mapOf(
                                            "contributor" to uid,
                                            "itemName" to itemName.trim(),
                                            "itemPrice" to amount,
                                            "payer" to currentUserId,
                                            "timestamp" to Date()
                                        )
                                    )
                                }
                            }

                            if (billsToSave.isEmpty()) {
                                Toast.makeText(context, "Please enter at least one valid amount.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Save all bills
                            val batch = db.batch()
                            val collection = db.collection("${roomID}_BILLS")
                            
                            var recurringCollection = db.collection("rooms").document(roomID).collection("recurring_bills")
                            val dateFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
                            val currentMonth = dateFormat.format(java.util.Date())
                            
                            for (bill in billsToSave) {
                                val docRef = collection.document()
                                batch.set(docRef, bill)
                                
                                if (isRecurring) {
                                    val recurringDoc = recurringCollection.document()
                                    val recBill = bill.toMutableMap()
                                    recBill["lastProcessedMonth"] = currentMonth
                                    batch.set(recurringDoc, recBill)
                                }
                            }

                            batch.commit().addOnSuccessListener {
                                Toast.makeText(context, "Bill Added.", Toast.LENGTH_SHORT).show()
                                itemName = ""
                                totalAmount = ""
                                isRecurring = false
                                memberAmounts.keys.forEach { memberAmounts[it] = "" }
                            }.addOnFailureListener {
                                Toast.makeText(context, "Failed to save bill.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("SAVE BILL", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name is required.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            OutlinedTextField(
                value = totalAmount,
                onValueChange = { totalAmount = it },
                label = { Text("Total Bill Amount (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val total = totalAmount.toDoubleOrNull() ?: return@Button
                        // Split evenly among all members plus the payer
                        val equalShare = total / (members.size + 1)
                        members.forEach { member ->
                            memberAmounts[member.email] = String.format(java.util.Locale.US, "%.2f", equalShare)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Split Equally")
                }
                
                OutlinedButton(
                    onClick = {
                        members.forEach { member ->
                            memberAmounts[member.email] = "0.0"
                        }
                    }
                ) {
                    Text("Set All Zero")
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recurring Monthly Bill", fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No other members to split with.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(members) { member ->
                        val amount = memberAmounts[member.email] ?: "" // email holds uid here
                        
                        SplitAddBillCard(
                            member = member,
                            amount = amount,
                            onAmountChange = { newAmount ->
                                memberAmounts[member.email] = newAmount
                            },
                            context = context
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplitAddBillCard(
    member: MemberData,
    amount: String,
    onAmountChange: (String) -> Unit,
    context: android.content.Context
) {
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

            Text(
                text = member.fullname,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            
            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = { Text("0.0") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(100.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
