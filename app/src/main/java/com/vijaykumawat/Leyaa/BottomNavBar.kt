package com.vijaykumawat.Leyaa

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.ic_home_black_24dp), contentDescription = "Rooms") },
            label = { Text("Rooms") },
            selected = currentRoute == "rooms",
            onClick = { if (currentRoute != "rooms") onNavigate("rooms") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.ic_baseline_hourglass_bottom_24), contentDescription = "Alarms") },
            label = { Text("Alarms") },
            selected = currentRoute == "alarms",
            onClick = { if (currentRoute != "alarms") onNavigate("alarms") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.ic_notifications_black_24dp), contentDescription = "Invites") },
            label = { Text("Invites") },
            selected = currentRoute == "invites",
            onClick = { if (currentRoute != "invites") onNavigate("invites") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
        NavigationBarItem(
            icon = { Icon(painterResource(id = R.drawable.ic_baseline_person_24), contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute == "profile",
            onClick = { if (currentRoute != "profile") onNavigate("profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
    }
}
