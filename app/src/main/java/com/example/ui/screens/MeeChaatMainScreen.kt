package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UnreadBadgeGreen
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.theme.WhatsAppTopBarDark
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.MeeChaatViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeeChaatMainScreen(
    viewModel: MeeChaatViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_nav"
    ) { screen ->
        when (screen) {
            is Screen.Main -> {
                MainTabsHost(viewModel = viewModel, modifier = modifier)
            }
            is Screen.ChatDetail -> {
                ChatDetailScreen(chatId = screen.chatId, viewModel = viewModel, modifier = modifier)
            }
            is Screen.StatusView -> {
                StatusViewerScreen(statusId = screen.statusId, viewModel = viewModel, modifier = modifier)
            }
            is Screen.ActiveCall -> {
                ActiveCallScreen(
                    contactName = screen.contactName,
                    isVideoInitial = screen.isVideo,
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
            is Screen.Settings -> {
                SettingsScreen(viewModel = viewModel, modifier = modifier)
            }
            is Screen.ArchivedChats -> {
                ArchivedChatsScreen(viewModel = viewModel, modifier = modifier)
            }
            is Screen.Auth -> {
                AuthScreen(viewModel = viewModel, modifier = modifier)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsHost(
    viewModel: MeeChaatViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeChats by viewModel.activeChats.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val totalUnread = activeChats.sumOf { it.unreadCount }
    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search...", fontSize = 16.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("main_search_input")
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setSearchActive(false) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "MEECHAAT",
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = if (isDark) Color.White else WhatsAppGreen,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.testTag("app_brand_title")
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.addStatus("Snapshot at ${System.currentTimeMillis() % 1000}", "#005C4B") },
                            modifier = Modifier.testTag("main_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { viewModel.setSearchActive(true) },
                            modifier = Modifier.testTag("main_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = !showMenu },
                                modifier = Modifier.testTag("main_overflow_menu")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("New group") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.createNewChat("New Team Group", "", isGroup = true)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New broadcast") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Linked devices") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Starred messages") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Account & Sign In") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.navigateTo(Screen.Auth)
                                    },
                                    modifier = Modifier.testTag("menu_account_auth")
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        showMenu = false
                                        viewModel.navigateTo(Screen.Settings)
                                    },
                                    modifier = Modifier.testTag("menu_settings")
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.navigationBarsPadding().testTag("main_bottom_nav")
            ) {
                // 1. Chats Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.CHATS,
                    onClick = { viewModel.selectTab(MainTab.CHATS) },
                    icon = {
                        if (totalUnread > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = UnreadBadgeGreen,
                                        contentColor = Color.White
                                    ) {
                                        Text("$totalUnread", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.CHATS) Icons.Default.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = if (currentTab == MainTab.CHATS) Icons.Default.Chat else Icons.Outlined.Chat,
                                contentDescription = "Chats"
                            )
                        }
                    },
                    label = { Text("Chats", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppGreen,
                        selectedTextColor = WhatsAppGreen,
                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_chats")
                )

                // 2. Updates / Status Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.UPDATES,
                    onClick = { viewModel.selectTab(MainTab.UPDATES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.UPDATES) Icons.Default.Update else Icons.Outlined.Update,
                            contentDescription = "Updates"
                        )
                    },
                    label = { Text("Updates", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppGreen,
                        selectedTextColor = WhatsAppGreen,
                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_updates")
                )

                // 3. Communities Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.COMMUNITIES,
                    onClick = { viewModel.selectTab(MainTab.COMMUNITIES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.COMMUNITIES) Icons.Default.Groups else Icons.Outlined.Groups,
                            contentDescription = "Communities"
                        )
                    },
                    label = { Text("Communities", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppGreen,
                        selectedTextColor = WhatsAppGreen,
                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_communities")
                )

                // 4. Calls Tab
                NavigationBarItem(
                    selected = currentTab == MainTab.CALLS,
                    onClick = { viewModel.selectTab(MainTab.CALLS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == MainTab.CALLS) Icons.Default.Call else Icons.Outlined.Call,
                            contentDescription = "Calls"
                        )
                    },
                    label = { Text("Calls", fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = WhatsAppGreen,
                        selectedTextColor = WhatsAppGreen,
                        indicatorColor = WhatsAppGreenLight.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("nav_tab_calls")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.CHATS -> ChatsTabScreen(viewModel = viewModel)
                MainTab.UPDATES -> UpdatesTabScreen(viewModel = viewModel)
                MainTab.COMMUNITIES -> CommunitiesTabScreen(viewModel = viewModel)
                MainTab.CALLS -> CallsTabScreen(viewModel = viewModel)
            }
        }
    }
}
