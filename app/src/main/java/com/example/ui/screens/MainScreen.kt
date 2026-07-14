package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.SettingsSuggest
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.NotoSansDevanagari
import com.example.viewmodel.AiViewModel
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.CloudViewModel
import com.example.viewmodel.FileViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object LocalStorage : Screen("local_storage", "Local File Manager", Icons.Rounded.Storage)
    object CloudManager : Screen("gdrive_sim", "Google Drive Sim", Icons.Rounded.Cloud)
    object AiAssistant : Screen("ai_assistant", "AI Assistant", Icons.Rounded.AutoAwesome)
}

@Composable
fun MainScreen(
    fileViewModel: FileViewModel,
    cloudViewModel: CloudViewModel,
    aiViewModel: AiViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.LocalStorage.route

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val darkModeSetting by authViewModel.darkModeSetting.collectAsStateWithLifecycle()
    var isMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val screens = listOf(
        Screen.LocalStorage,
        Screen.CloudManager,
        Screen.AiAssistant
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Smart File Ultra",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = NotoSansDevanagari
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Secure AI Copilot",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    // Profile Section with Dropdown Trigger
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .clickable { isMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar Letter Circle
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val firstLetter = currentUser?.name?.firstOrNull()?.toString()?.uppercase() ?: "U"
                                Text(
                                    text = firstLetter,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentUser?.name?.substringBefore(" ") ?: "User",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Profile Dropdown
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = currentUser?.name ?: "User",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = currentUser?.email ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val providerName = if (currentUser?.provider == "google") "Google Account" else "Microsoft Account"
                                        Text(
                                            text = "Via $providerName",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Appearance",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = {},
                                enabled = false
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.SettingsSuggest,
                                                contentDescription = "System Theme",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("System Theme", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        if (darkModeSetting == "system") {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    authViewModel.setDarkModeSetting("system")
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.LightMode,
                                                contentDescription = "Light Theme",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Light Mode", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        if (darkModeSetting == "light") {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    authViewModel.setDarkModeSetting("light")
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.DarkMode,
                                                contentDescription = "Dark Theme",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Dark Mode", style = MaterialTheme.typography.bodyMedium)
                                        }
                                        if (darkModeSetting == "dark") {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = "Selected",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    authViewModel.setDarkModeSetting("dark")
                                    isMenuExpanded = false
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                                            contentDescription = "Sign Out",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sign Out",
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    authViewModel.logout(context)
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                tonalElevation = 4.dp
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title.substringBefore(" Sim").substringBefore(" File"),
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("tab_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.LocalStorage.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.LocalStorage.route) {
                LocalFileManagerScreen(viewModel = fileViewModel)
            }
            composable(Screen.CloudManager.route) {
                CloudManagerScreen(viewModel = cloudViewModel)
            }
            composable(Screen.AiAssistant.route) {
                AiAssistantScreen(viewModel = aiViewModel)
            }
        }
    }
}
