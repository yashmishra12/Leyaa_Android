package com.vijaykumawat.Leyaa

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Locale
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
import androidx.compose.material.icons.filled.Notifications

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vijaykumawat.Leyaa.data.AlarmReminderContract
import com.vijaykumawat.Leyaa.ui.theme.LeyaaTheme

data class AlarmItem(
    val id: Long,
    val title: String,
    val date: String,
    val time: String,
    val active: String
)

class Alarm_MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LeyaaTheme {
                AlarmScreen(
                    onBottomNavClick = { intent ->
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    },
                    context = this
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    onBottomNavClick: (Intent) -> Unit,
    context: Context
) {
    var alarms by remember { mutableStateOf<List<AlarmItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadAlarms() {
        val projection = arrayOf(
            AlarmReminderContract.AlarmReminderEntry._ID,
            AlarmReminderContract.AlarmReminderEntry.KEY_TITLE,
            AlarmReminderContract.AlarmReminderEntry.KEY_DATE,
            AlarmReminderContract.AlarmReminderEntry.KEY_TIME,
            AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE
        )

        val cursor = context.contentResolver.query(
            AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        val newAlarms = mutableListOf<AlarmItem>()
        cursor?.use {
            val idIndex = it.getColumnIndex(AlarmReminderContract.AlarmReminderEntry._ID)
            val titleIndex = it.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TITLE)
            val dateIndex = it.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_DATE)
            val timeIndex = it.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_TIME)
            val activeIndex = it.getColumnIndex(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val title = it.getString(titleIndex)
                val date = it.getString(dateIndex)
                val time = it.getString(timeIndex)
                var active = it.getString(activeIndex)

                try {
                    val formatter = SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault())
                    val dateObj = formatter.parse("$date $time")
                    if (dateObj != null) {
                        if (active == "true" && System.currentTimeMillis() > dateObj.time + 2 * 60 * 1000) {
                            active = "false"
                            val uri = ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, id)
                            val values = ContentValues().apply { put(AlarmReminderContract.AlarmReminderEntry.KEY_ACTIVE, "false") }
                            context.contentResolver.update(uri, values, null, null)
                        }
                    }
                } catch (e: Exception) { }

                newAlarms.add(
                    AlarmItem(
                        id = id,
                        title = title,
                        date = date,
                        time = time,
                        active = active
                    )
                )
            }
        }
        alarms = newAlarms
        isLoading = false
    }

    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                loadAlarms()
            }
        }
        context.contentResolver.registerContentObserver(
            AlarmReminderContract.AlarmReminderEntry.CONTENT_URI,
            true,
            observer
        )
        loadAlarms()
        
        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Reminders", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "alarms",
                onNavigate = { route ->
                    val intent = when (route) {
                        "rooms" -> Intent(context, RoomListActivity::class.java)
                        "invites" -> Intent(context, InvitationListActivity::class.java)
                        "alarms" -> Intent(context, Alarm_MainActivity::class.java)
                        "profile" -> Intent(context, ProfilePage::class.java)
                        else -> null
                    }
                    intent?.let { 
                        it.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        onBottomNavClick(it) 
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, AddReminderActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Reminder")
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
            } else if (alarms.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "No Reminders",
                        modifier = Modifier.size(100.dp).alpha(0.3f),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No reminders set.\nTap + to create one.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onClick = {
                                val intent = Intent(context, AddReminderActivity::class.java)
                                val uri = ContentUris.withAppendedId(AlarmReminderContract.AlarmReminderEntry.CONTENT_URI, alarm.id)
                                intent.data = uri
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
fun AlarmCard(
    alarm: AlarmItem,
    onClick: () -> Unit
) {
    val isActive = alarm.active == "true"
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${alarm.date} at ${alarm.time}",
                        fontSize = 14.sp,
                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = if (isActive) "Active" else "Inactive",
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
