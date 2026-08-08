package com.vijaykumawat.Leyaa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.speech.RecognizerIntent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import android.provider.MediaStore

import android.widget.Toast
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as rowItems
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme
import kotlinx.coroutines.launch
import java.util.UUID

data class RoomItem(
    val id: String,
    val name: String,
    val qty: String,
    val desc: String
)

fun getCategory(itemName: String): String {
    val name = itemName.lowercase()
    val categories = mapOf(
        "Dairy & Eggs" to listOf("milk", "egg", "cheese", "butter", "yogurt", "cream"),
        "Meat & Seafood" to listOf("chicken", "beef", "pork", "fish", "salmon", "tuna", "bacon", "sausage", "meat"),
        "Produce" to listOf("apple", "banana", "orange", "carrot", "tomato", "potato", "onion", "garlic", "lettuce", "spinach", "broccoli", "fruit", "veg"),
        "Bakery" to listOf("bread", "bagel", "croissant", "muffin", "cake", "pie", "bun"),
        "Pantry" to listOf("pasta", "rice", "flour", "sugar", "oil", "salt", "pepper", "spice", "sauce", "cereal", "bean", "can"),
        "Snacks" to listOf("chip", "cookie", "candy", "chocolate", "nut", "popcorn", "snack"),
        "Beverages" to listOf("water", "juice", "soda", "coffee", "tea", "beer", "wine", "drink"),
        "Household" to listOf("soap", "detergent", "paper towel", "toilet paper", "trash bag", "cleaner", "bleach", "sponge"),
        "Personal Care" to listOf("shampoo", "conditioner", "toothpaste", "toothbrush", "deodorant", "lotion", "body wash", "razor")
    )
    
    for ((category, keywords) in categories) {
        if (keywords.any { name.contains(it) }) {
            return category
        }
    }
    return "Other"
}

class Inside_Room_Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val roomID = intent.extras?.getString("document_ID") ?: ""
        
        setContent {
            LeyaaTheme {
                InsideRoomScreen(
                    roomID = roomID,
                    onNavigateBack = { finish() },
                    onBottomNavClick = { intent -> 
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    },
                    context = this
                )
            }
        }
    }
}

fun sendRoomNotification(action: String, roomID: String, roomName: String, userName: String, context: Context) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    
    db.collection("rooms").document(roomID).get().addOnSuccessListener { documentSnapshot ->
        val memberList = documentSnapshot.get("members") as? ArrayList<String> ?: return@addOnSuccessListener
        
        for (member in memberList) {
            if (member != auth.uid) {
                db.collection("users").document(member).get().addOnSuccessListener { ds ->
                    val userDeviceToken = ds.getString("deviceToken") ?: return@addOnSuccessListener
                    val title = "Room: $roomName"
                    val message = "$userName $action"
                    val sender = FcmNotificationsSender(userDeviceToken, title, message, context, context as Activity)
                    sender.SendNotifications()
                }
            }
        }
    }
    Toast.makeText(context, "Notification Sent!", Toast.LENGTH_SHORT).show()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsideRoomScreen(
    roomID: String,
    onNavigateBack: () -> Unit,
    onBottomNavClick: (Intent) -> Unit,
    context: ComponentActivity
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    var roomName by remember { mutableStateOf("Loading...") }
    var roomAvatar by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<RoomItem>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<RoomItem>>(emptyList()) }
    var boughtItems by remember { mutableStateOf<List<RoomItem>>(emptyList()) }
    var deletedItems by remember { mutableStateOf<List<RoomItem>>(emptyList()) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    
    // Edit item state
    var editingItem by remember { mutableStateOf<RoomItem?>(null) }
    var initialNameForAdd by remember { mutableStateOf("") }
    
    // Fetch user name
    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                userName = doc.getString("fullname") ?: ""
            }
        }
    }

    // Fetch Room Data & Realtime items
    DisposableEffect(roomID) {
        val roomRef = db.collection("rooms").document(roomID)
        
        // Initial fetch for title and avatar
        roomRef.get().addOnSuccessListener { doc ->
            roomName = doc.getString("title") ?: "Room"
            roomAvatar = doc.getString("avatar") ?: ""
        }
        
        // Listen for items
        val listener = roomRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                isLoading = false
                return@addSnapshotListener
            }
            
            if (snapshot != null && snapshot.exists()) {
                val itemsList = snapshot.get("newItems") as? List<Map<String, String>> ?: emptyList()
                items = itemsList.map { 
                    RoomItem(
                        id = it["id"] ?: "",
                        name = it["name"] ?: "",
                        qty = it["qty"] ?: "",
                        desc = it["desc"] ?: ""
                    )
                }
                val deletedList = snapshot.get("deletedItems") as? List<Map<String, String>> ?: emptyList()
                deletedItems = deletedList.map { 
                    RoomItem(
                        id = it["id"] ?: "",
                        name = it["name"] ?: "",
                        qty = it["qty"] ?: "",
                        desc = it["desc"] ?: ""
                    )
                }
                val boughtList = snapshot.get("boughtItems") as? List<Map<String, String>> ?: emptyList()
                boughtItems = boughtList.map { 
                    RoomItem(
                        id = it["id"] ?: "",
                        name = it["name"] ?: "",
                        qty = it["qty"] ?: "",
                        desc = it["desc"] ?: ""
                    )
                }
                isLoading = false
            }
        }
        
        onDispose {
            listener.remove()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Room Options",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text("Message Wall") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, ChatRoom::class.java).apply {
                            putExtra("roomID", roomID)
                            putExtra("roomName", roomName)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Email, "Message Wall") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                /*
                NavigationDrawerItem(
                    label = { Text("Notice Board") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, RoomNoticeBoardActivity::class.java).apply {
                            putExtra("roomID", roomID)
                            putExtra("roomName", roomName)
                            putExtra("userName", userName)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Email, "Chat") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                */
                NavigationDrawerItem(
                    label = { Text("Split Bill") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, SplitBill_Member_Info::class.java).apply {
                            putExtra("roomID", roomID)
                            putExtra("roomName", roomName)
                            putExtra("userName", userName)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.List, "Split Bill") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                /*
                NavigationDrawerItem(
                    label = { Text("Utility Reminders") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, RoomRemindersActivity::class.java).apply {
                            putExtra("roomID", roomID)
                            putExtra("roomName", roomName)
                            putExtra("userName", userName)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.DateRange, "Utility Reminders") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                */
                /*
                NavigationDrawerItem(
                    label = { Text("Chores") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, RoomChoresActivity::class.java).apply {
                            putExtra("roomID", roomID)
                            putExtra("roomName", roomName)
                            putExtra("userName", userName)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.CheckCircle, "Chores") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                */
                NavigationDrawerItem(
                    label = { Text("Room Members") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, RoomMemberInfo::class.java).apply {
                            putExtra("roomID", roomID)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Person, "Members") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Edit Room Name") },
                    selected = false,
                    onClick = { 
                        val intent = Intent(context, EditRoomName::class.java).apply {
                            putExtra("roomID", roomID)
                            putExtra("roomName", roomName)
                            putExtra("roomAvatar", roomAvatar)
                        }
                        context.startActivity(intent)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Edit, "Edit Name") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    "Notify Members",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                NavigationDrawerItem(
                    label = { Text("Doing Laundry") },
                    selected = false,
                    onClick = { 
                        sendRoomNotification("is going to do laundry", roomID, roomName, userName, context)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Notifications, "Notify") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Taking Trash Out") },
                    selected = false,
                    onClick = { 
                        sendRoomNotification("is going to take the trash out", roomID, roomName, userName, context)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Notifications, "Notify") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Cleaning Fridge") },
                    selected = false,
                    onClick = { 
                        sendRoomNotification("is going to clean the fridge", roomID, roomName, userName, context)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.Notifications, "Notify") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("Going Shopping") },
                    selected = false,
                    onClick = { 
                        sendRoomNotification("is going for shopping", roomID, roomName, userName, context)
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.ShoppingCart, "Notify") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                NavigationDrawerItem(
                    label = { Text("Leave Room") },
                    selected = false,
                    onClick = { 
                        showLeaveDialog = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Filled.ExitToApp, "Leave Room", tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (roomAvatar.isNotEmpty()) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val sanitizedAvatar = SanitizeItemName.sanitizeItemName(roomAvatar)
                                val resId = context.resources.getIdentifier(sanitizedAvatar, "drawable", context.packageName)
                                if (resId != 0) {
                                    androidx.compose.foundation.Image(
                                        painter = androidx.compose.ui.res.painterResource(id = resId),
                                        contentDescription = roomAvatar,
                                        modifier = Modifier.size(28.dp).padding(end = 8.dp)
                                    )
                                }
                            }
                            Text(roomName, fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
                    )
                )
            },
            bottomBar = {
                BottomNavBar(
                    currentRoute = "rooms", // We are technically still inside Rooms flow
                    onNavigate = { route ->
                        val intent = when (route) {
                            "rooms" -> Intent(context, RoomListActivity::class.java)
                            "invites" -> Intent(context, InvitationListActivity::class.java)
                            "alarms" -> Intent(context, Alarm_MainActivity::class.java)
                            "profile" -> Intent(context, ProfilePage::class.java)
                            else -> null
                        }
                        intent?.let { 
                            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            onBottomNavClick(it) 
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showQuickAddDialog = true },
                    modifier = Modifier.offset(y = 16.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, "Add Item")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (items.isEmpty()) {
                            val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulse")
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.1f,
                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                    animation = androidx.compose.animation.core.tween(1000),
                                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Empty",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .scale(pulseScale)
                                        .alpha(0.3f),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "No items here yet.\nTap + to add some!",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    fontSize = 18.sp
                                )
                            }
                        } else {
                            val groupedItems = items.groupBy { getCategory(it.name) }.toSortedMap(
                                compareBy<String> { if (it == "Other") 1 else 0 }.thenBy { it }
                            )
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Swipe left to delete, swipe right to mark as bought",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                )
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 88.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    groupedItems.forEach { (category, categoryItems) ->
                                    item(key = "header_$category") {
                                        Text(
                                            text = category,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 18.sp,
                                            modifier = Modifier.padding(
                                                top = if (category == groupedItems.keys.first()) 0.dp else 16.dp, 
                                                bottom = 4.dp
                                            )
                                        )
                                    }
                                    
                                    items(categoryItems, key = { it.id }) { item ->
                                        val dismissState = rememberSwipeToDismissBoxState(
                                            confirmValueChange = { dismissValue ->
                                                if (dismissValue == SwipeToDismissBoxValue.StartToEnd || dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                                    val itemMap = mapOf(
                                                        "id" to item.id,
                                                        "name" to item.name,
                                                        "qty" to item.qty,
                                                        "desc" to item.desc
                                                    )
                                                    val roomRef = db.collection("rooms").document(roomID)
                                                    db.runBatch { batch ->
                                                        batch.update(roomRef, "newItems", FieldValue.arrayRemove(itemMap))
                                                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                                            batch.update(roomRef, "boughtItems", FieldValue.arrayUnion(itemMap))
                                                        } else {
                                                            batch.update(roomRef, "deletedItems", FieldValue.arrayUnion(itemMap))
                                                        }
                                                    }.addOnSuccessListener {
                                                        if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                                            showSuccessAnimation = true
                                                        } else {
                                                            Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
                                                        }
                                                        roomRef.get().addOnSuccessListener { doc ->
                                                            if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                                                                val bItems = doc.get("boughtItems") as? List<Map<String, String>> ?: emptyList()
                                                                if (bItems.size > 10) {
                                                                    roomRef.update("boughtItems", bItems.takeLast(10))
                                                                }
                                                            } else {
                                                                val dItems = doc.get("deletedItems") as? List<Map<String, String>> ?: emptyList()
                                                                if (dItems.size > 10) {
                                                                    roomRef.update("deletedItems", dItems.takeLast(10))
                                                                }
                                                            }
                                                        }
                                                    }
                                                    true
                                                } else {
                                                    false
                                                }
                                            }
                                        )
                                        
                                        SwipeToDismissBox(
                                            state = dismissState,
                                            backgroundContent = {
                                                val color = when (dismissState.targetValue) {
                                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green
                                                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error // Red
                                                    else -> Color.Transparent
                                                }
                                                val icon = when (dismissState.targetValue) {
                                                    SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                                                    SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                                    else -> null
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(color, RoundedCornerShape(12.dp))
                                                        .padding(horizontal = 20.dp),
                                                    contentAlignment = when (dismissState.targetValue) {
                                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                                        else -> Alignment.CenterEnd
                                                    }
                                                ) {
                                                    icon?.let {
                                                        Icon(
                                                            imageVector = it,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.scale(1f + (dismissState.progress * 0.3f))
                                                        )
                                                    }
                                                }
                                            }
                                        ) {
                                            ItemCard(
                                                item = item,
                                                context = context,
                                                onDelete = {}, // Delete button removed in UI, handled by swipe
                                                onClick = { editingItem = item }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                    
                    if (boughtItems.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recently Bought",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            TextButton(
                                onClick = {
                                    db.collection("rooms").document(roomID).update("boughtItems", emptyList<Any>()).addOnSuccessListener {
                                        Toast.makeText(context, "Bought items cleared", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Clear All", fontSize = 12.sp)
                            }
                        }
                        LazyRow(
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 96.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems(boughtItems) { item ->
                                DeletedItemCard(
                                    item = item,
                                    context = context,
                                    onRestore = {
                                        val itemMap = mapOf(
                                            "id" to item.id,
                                            "name" to item.name,
                                            "qty" to item.qty,
                                            "desc" to item.desc
                                        )
                                        val roomRef = db.collection("rooms").document(roomID)
                                        db.runBatch { batch ->
                                            batch.update(roomRef, "boughtItems", FieldValue.arrayRemove(itemMap))
                                            batch.update(roomRef, "newItems", FieldValue.arrayUnion(itemMap))
                                        }.addOnSuccessListener {
                                            Toast.makeText(context, "Item Restored", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    if (deletedItems.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recently Removed",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                            TextButton(
                                onClick = {
                                    db.collection("rooms").document(roomID).update("deletedItems", emptyList<Any>()).addOnSuccessListener {
                                        Toast.makeText(context, "Removed items cleared", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("Clear All", fontSize = 12.sp)
                            }
                        }
                        LazyRow(
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 96.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems(deletedItems) { item ->
                                DeletedItemCard(
                                    item = item,
                                    context = context,
                                    onRestore = {
                                        val itemMap = mapOf(
                                            "id" to item.id,
                                            "name" to item.name,
                                            "qty" to item.qty,
                                            "desc" to item.desc
                                        )
                                        val roomRef = db.collection("rooms").document(roomID)
                                        db.runBatch { batch ->
                                            batch.update(roomRef, "deletedItems", FieldValue.arrayRemove(itemMap))
                                            batch.update(roomRef, "newItems", FieldValue.arrayUnion(itemMap))
                                        }.addOnSuccessListener {
                                            Toast.makeText(context, "Item Restored", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Are you sure?") },
            text = { Text("One of the group members will have to add you back.") },
            confirmButton = {
                Button(
                    onClick = {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            val roomRef = db.collection("rooms").document(roomID)
                            roomRef.get().addOnSuccessListener { doc ->
                                val members = doc.get("members") as? ArrayList<String>
                                roomRef.update("members", FieldValue.arrayRemove(uid)).addOnSuccessListener {
                                    if (members != null && members.size == 1) {
                                        roomRef.delete()
                                    }
                                    showLeaveDialog = false
                                    context.startActivity(Intent(context, RoomListActivity::class.java))
                                    context.finish()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQuickAddDialog) {
        QuickAddCatalog(
            context = context,
            onDismiss = { showQuickAddDialog = false },
            onItemSelected = { selectedName ->
                val uniqueID = UUID.randomUUID().toString()
                val newItemMap = mapOf(
                    "id" to uniqueID,
                    "name" to selectedName,
                    "qty" to "1",
                    "desc" to ""
                )
                val roomRef = db.collection("rooms").document(roomID)
                roomRef.update("newItems", FieldValue.arrayUnion(newItemMap)).addOnSuccessListener {
                    roomRef.update("lastItemID", uniqueID)
                }
            },
            onMultipleItemsSelected = { selectedNames ->
                val newItems = selectedNames.map { name ->
                    mapOf("id" to java.util.UUID.randomUUID().toString(), "name" to name, "qty" to "1", "desc" to "")
                }
                val roomRef = db.collection("rooms").document(roomID)
                roomRef.update("newItems", com.google.firebase.firestore.FieldValue.arrayUnion(*newItems.toTypedArray()))
            },
            onCustomItem = {
                initialNameForAdd = ""
                showQuickAddDialog = false
                showAddDialog = true
            }
        )
    }

    if (showAddDialog || editingItem != null) {
        val isEdit = editingItem != null
        val initialName = editingItem?.name ?: initialNameForAdd
        val initialQty = editingItem?.qty ?: ""
        val initialDesc = editingItem?.desc ?: ""
        
        ItemDialog(
            isEdit = isEdit,
            initialName = initialName,
            initialQty = initialQty,
            initialDesc = initialDesc,
            onDismiss = { 
                showAddDialog = false
                editingItem = null
                initialNameForAdd = ""
            },
            onSave = { name, qty, desc ->
                if (name.isBlank()) {
                    Toast.makeText(context, "Name required", Toast.LENGTH_SHORT).show()
                    return@ItemDialog
                }
                
                val roomRef = db.collection("rooms").document(roomID)
                
                if (isEdit && editingItem != null) {
                    val oldItemMap = mapOf(
                        "id" to editingItem!!.id,
                        "name" to editingItem!!.name,
                        "qty" to editingItem!!.qty,
                        "desc" to editingItem!!.desc
                    )
                    val newItemMap = mapOf(
                        "id" to editingItem!!.id,
                        "name" to name,
                        "qty" to qty,
                        "desc" to desc
                    )
                    
                    roomRef.update("newItems", FieldValue.arrayRemove(oldItemMap)).addOnSuccessListener {
                        roomRef.update("newItems", FieldValue.arrayUnion(newItemMap)).addOnSuccessListener {
                            Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val uniqueID = UUID.randomUUID().toString()
                    val newItemMap = mapOf(
                        "id" to uniqueID,
                        "name" to name,
                        "qty" to qty,
                        "desc" to desc
                    )
                    
                    roomRef.update("newItems", FieldValue.arrayUnion(newItemMap)).addOnSuccessListener {
                        roomRef.update("lastItemID", uniqueID).addOnSuccessListener {
                            Toast.makeText(context, "Item Added", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                
                showAddDialog = false
                editingItem = null
                initialNameForAdd = ""
            }
        )
    }
}

@Composable
fun ItemDialog(
    isEdit: Boolean,
    initialName: String,
    initialQty: String,
    initialDesc: String,
    onDismiss: () -> Unit,
    onSave: (name: String, qty: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var qty by remember { mutableStateOf(initialQty) }
    var desc by remember { mutableStateOf(initialDesc) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Item" else "Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantity (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim(), qty.trim(), desc.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ItemCard(
    item: RoomItem,
    context: android.content.Context,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val avatarName = SanitizeItemName.sanitizeItemName(item.name)
    val resId = context.resources.getIdentifier(avatarName, "drawable", context.packageName)
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "scale")
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
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
                    contentDescription = item.name,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Item",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.qty.isNotBlank() || item.desc.isNotBlank()) {
                    val subtext = mutableListOf<String>()
                    if (item.qty.isNotBlank()) subtext.add("Qty: ${item.qty}")
                    if (item.desc.isNotBlank()) subtext.add(item.desc)
                    Text(
                        text = subtext.joinToString(" • "),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddCatalog(
    context: android.content.Context,
    onDismiss: () -> Unit,
    onItemSelected: (String) -> Unit,
    onMultipleItemsSelected: (List<String>) -> Unit,
    onCustomItem: () -> Unit
) {
    var ocrResultItems by remember { mutableStateOf<List<String>>(emptyList()) }
    var showOcrDialog by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                results[0]
            }
            if (!spokenText.isNullOrBlank()) {
                searchQuery = spokenText
            }
        }
    }

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && cameraUri != null) {
            try {
                val image = InputImage.fromFilePath(context, cameraUri!!)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        val lines = visionText.text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                        if (lines.isNotEmpty()) {
                            ocrResultItems = lines
                            showOcrDialog = true
                        } else {
                            Toast.makeText(context, "No text found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "OCR failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val imagePath = File(context.cacheDir, "images")
            imagePath.mkdirs()
            val newFile = File(imagePath, "camera_image.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", newFile)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required for OCR", Toast.LENGTH_SHORT).show()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
            try {
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }

    var addedItemMessage by remember { mutableStateOf("") }
    
    LaunchedEffect(addedItemMessage) {
        if (addedItemMessage.isNotEmpty()) {
            delay(1500)
            addedItemMessage = ""
        }
    }
    
    val allItems = remember { SanitizeItemName.realAssetName.toList().sorted() }
    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allItems
        } else {
            val queryNoSpaces = searchQuery.replace("\\s".toRegex(), "")
            allItems.filter { 
                it.contains(searchQuery, ignoreCase = true) || 
                it.replace("\\s".toRegex(), "").contains(queryNoSpaces, ignoreCase = true)
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Quick Add") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        TextButton(onClick = onCustomItem) {
                            Text("Custom Item", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search items...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = {
                                micPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }) {
                                Icon(Icons.Filled.Mic, contentDescription = "Voice Input")
                            }
                            IconButton(onClick = {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Camera Input")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                if (showOcrDialog) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { showOcrDialog = false }) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
                        ) {
                            var checkedStates by remember { mutableStateOf(ocrResultItems.map { it to true }.toMap()) }
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Select items to add", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(modifier = Modifier.weight(1f)) {
                                    items(ocrResultItems) { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                checkedStates = checkedStates.toMutableMap().apply {
                                                    this[item] = !(this[item] ?: true)
                                                }
                                            }.padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = checkedStates[item] ?: true,
                                                onCheckedChange = { isChecked ->
                                                    checkedStates = checkedStates.toMutableMap().apply {
                                                        this[item] = isChecked
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(item)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    TextButton(onClick = { showOcrDialog = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = {
                                        val selectedItems = checkedStates.filter { it.value }.keys.toList()
                                        if (selectedItems.isNotEmpty()) {
                                            onMultipleItemsSelected(selectedItems)
                                            showOcrDialog = false
                                            onDismiss() // Close QuickAddCatalog too
                                        } else {
                                            Toast.makeText(context, "No items selected", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Text("Add Selected")
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = addedItemMessage.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = addedItemMessage, 
                            color = MaterialTheme.colorScheme.onPrimaryContainer, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (filteredItems.isEmpty() && searchQuery.isNotBlank()) {
                    Button(
                        onClick = { 
                            onItemSelected(searchQuery.trim())
                            addedItemMessage = "${searchQuery.trim()} Added!"
                            searchQuery = ""
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Add '${searchQuery.trim()}'")
                    }
                    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp).alpha(0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No items found for '${searchQuery}'",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems) { itemName ->
                            CatalogItemCard(
                                itemName = itemName,
                                context = context,
                                onClick = { 
                                    onItemSelected(itemName)
                                    addedItemMessage = "$itemName Added!"
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogItemCard(
    itemName: String,
    context: android.content.Context,
    onClick: () -> Unit
) {
    val avatarName = SanitizeItemName.sanitizeItemName(itemName)
    val resId = context.resources.getIdentifier(avatarName, "drawable", context.packageName)
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by androidx.compose.animation.core.animateFloatAsState(targetValue = if (isPressed) 0.90f else 1f, label = "scale")
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = itemName,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Item",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = itemName,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun DeletedItemCard(
    item: RoomItem,
    context: android.content.Context,
    onRestore: () -> Unit
) {
    val avatarName = SanitizeItemName.sanitizeItemName(item.name)
    val resId = context.resources.getIdentifier(avatarName, "drawable", context.packageName)
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onRestore)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = item.name,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Item",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.Add,
                contentDescription = "Restore",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
