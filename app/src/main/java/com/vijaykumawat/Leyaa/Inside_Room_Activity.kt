package com.vijaykumawat.Leyaa

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
                        startActivity(intent)
                        overridePendingTransition(0, 0)
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
    
    var roomName by remember { mutableStateOf("Loading...") }
    var roomAvatar by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<RoomItem>>(emptyList()) }
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
                    icon = { Icon(Icons.Filled.Email, "Chat") },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
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
                        containerColor = MaterialTheme.colorScheme.background
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
                } else if (items.isEmpty()) {
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
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(items) { item ->
                            ItemCard(
                                item = item,
                                context = context,
                                onDelete = {
                                    val itemMap = mapOf(
                                        "id" to item.id,
                                        "name" to item.name,
                                        "qty" to item.qty,
                                        "desc" to item.desc
                                    )
                                    db.collection("rooms").document(roomID)
                                        .update("newItems", FieldValue.arrayRemove(itemMap))
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                onClick = {
                                    editingItem = item
                                }
                            )
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
                    roomRef.update("lastItemID", uniqueID).addOnSuccessListener {
                        Toast.makeText(context, "$selectedName Added", Toast.LENGTH_SHORT).show()
                    }
                }
                showQuickAddDialog = false
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
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = item.name,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Item",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                if (item.qty.isNotBlank()) {
                    Text(
                        text = "Qty: ${item.qty}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.desc.isNotBlank()) {
                    Text(
                        text = item.desc,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
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
    onCustomItem: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val allItems = remember { SanitizeItemName.realAssetName.toList().sorted() }
    val filteredItems = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            allItems
        } else {
            val sanitizedQuery = SanitizeItemName.sanitizeItemName(searchQuery)
            allItems.filter { 
                it.contains(searchQuery, ignoreCase = true) || 
                (sanitizedQuery != "imagenotfound" && it.contains(sanitizedQuery, ignoreCase = true))
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
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search items...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                if (filteredItems.isEmpty() && searchQuery.isNotBlank()) {
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
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { onItemSelected(searchQuery.trim()) }) {
                                Text("Add '${searchQuery.trim()}'")
                            }
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
                                onClick = { onItemSelected(itemName) }
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
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
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
