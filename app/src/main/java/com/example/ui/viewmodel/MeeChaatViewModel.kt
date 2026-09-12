package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthUserState
import com.example.data.auth.FirebaseAuthService
import com.example.data.database.MeeChaatDatabase
import com.example.data.entity.CallEntity
import com.example.data.entity.ChannelEntity
import com.example.data.entity.ChatEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.StatusEntity
import com.example.data.repository.MeeChaatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Main : Screen()
    data class ChatDetail(val chatId: String) : Screen()
    data class StatusView(val statusId: String) : Screen()
    data class ActiveCall(val contactName: String, val isVideo: Boolean) : Screen()
    object Settings : Screen()
    object ArchivedChats : Screen()
    object Auth : Screen()
    object ProfileSetup : Screen()
}

enum class MainTab {
    CHATS, UPDATES, COMMUNITIES, CALLS
}

enum class ChatFilter {
    ALL, UNREAD, FAVOURITES, GROUPS
}

class MeeChaatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = MeeChaatDatabase.getDatabase(application)
    private val repository = MeeChaatRepository(database)

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Main)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _chatFilter = MutableStateFlow(ChatFilter.ALL)
    val chatFilter: StateFlow<ChatFilter> = _chatFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    // User Profile state
    private val _userName = MutableStateFlow("John Doe")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userStatusQuote = MutableStateFlow("Hey there! I am using MEECHAAT")
    val userStatusQuote: StateFlow<String> = _userStatusQuote.asStateFlow()

    private val _userPhone = MutableStateFlow("+1 234 567 8900")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userProfilePictureUri = MutableStateFlow<String?>(null)
    val userProfilePictureUri: StateFlow<String?> = _userProfilePictureUri.asStateFlow()

    val authService = FirebaseAuthService()
    val authUserState: StateFlow<AuthUserState> = authService.currentUserState

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
        viewModelScope.launch {
            authService.currentUserState.collect { userState ->
                if (userState.isSignedIn) {
                    if (!userState.displayName.isNullOrBlank()) {
                        _userName.value = userState.displayName
                    }
                    if (!userState.photoUrl.isNullOrBlank()) {
                        _userProfilePictureUri.value = userState.photoUrl
                    }
                }
            }
        }
    }

    val activeChats: StateFlow<List<ChatEntity>> = combine(
        repository.activeChats,
        _chatFilter,
        _searchQuery
    ) { chats, filter, query ->
        var list = when (filter) {
            ChatFilter.ALL -> chats
            ChatFilter.UNREAD -> chats.filter { it.unreadCount > 0 }
            ChatFilter.FAVOURITES -> chats.filter { it.isPinned }
            ChatFilter.GROUPS -> chats.filter { it.isGroup }
        }
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) || it.lastMessageText.lowercase().contains(q)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedChats: StateFlow<List<ChatEntity>> = repository.archivedChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statuses: StateFlow<List<StatusEntity>> = repository.allStatuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<ChannelEntity>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun navigateBack() {
        _currentScreen.value = Screen.Main
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun getChat(chatId: String): Flow<ChatEntity?> = repository.getChat(chatId)

    fun getMessages(chatId: String): Flow<List<MessageEntity>> = repository.getMessages(chatId)

    fun markChatRead(chatId: String) {
        viewModelScope.launch {
            repository.markChatRead(chatId)
        }
    }

    fun sendMessage(chatId: String, text: String, type: String = "TEXT", durationSeconds: Int = 0) {
        if (text.isBlank() && type == "TEXT") return
        viewModelScope.launch {
            repository.sendMessage(
                chatId = chatId,
                text = text,
                type = type,
                durationSeconds = durationSeconds
            )
        }
    }

    fun togglePin(chat: ChatEntity) {
        viewModelScope.launch {
            repository.togglePin(chat.id, chat.isPinned)
        }
    }

    fun toggleMute(chat: ChatEntity) {
        viewModelScope.launch {
            repository.toggleMute(chat.id, chat.isMuted)
        }
    }

    fun toggleArchive(chat: ChatEntity) {
        viewModelScope.launch {
            repository.toggleArchive(chat.id, chat.isArchived)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }

    fun clearChat(chatId: String) {
        viewModelScope.launch {
            repository.clearMessages(chatId)
        }
    }

    fun setReaction(messageId: String, reaction: String?) {
        viewModelScope.launch {
            repository.setReaction(messageId, reaction)
        }
    }

    fun addStatus(text: String, bgColorHex: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addStatus(text, mediaType = "TEXT", bgColorHex = bgColorHex)
        }
    }

    fun markStatusViewed(statusId: String) {
        viewModelScope.launch {
            repository.markStatusViewed(statusId)
        }
    }

    fun toggleChannelFollow(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleChannelFollow(channel.id, channel.isFollowing)
        }
    }

    fun startCall(contactName: String, isVideo: Boolean) {
        viewModelScope.launch {
            repository.logCall(
                contactName = contactName,
                callType = if (isVideo) "VIDEO" else "AUDIO",
                direction = "OUTGOING",
                durationSeconds = 0
            )
        }
        _currentScreen.value = Screen.ActiveCall(contactName, isVideo)
    }

    fun clearCalls() {
        viewModelScope.launch {
            repository.clearCalls()
        }
    }

    fun createNewChat(name: String, phone: String, isGroup: Boolean = false) {
        viewModelScope.launch {
            val newId = repository.createNewChat(name, phone, isGroup)
            _currentScreen.value = Screen.ChatDetail(newId)
        }
    }

    fun updateProfile(name: String, statusQuote: String) {
        _userName.value = name
        _userStatusQuote.value = statusQuote
    }

    fun signUpWithEmail(email: String, pass: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authService.signUpWithEmail(email, pass, name, onSuccess = {
            _userName.value = name
            onSuccess()
        }, onError = onError)
    }

    fun signInWithEmail(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authService.signInWithEmail(email, pass, onSuccess = { user ->
            user.displayName?.let { _userName.value = it }
            onSuccess()
        }, onError = onError)
    }

    fun signInWithGoogle(context: Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authService.signInWithGoogle(context, onSuccess = { user ->
                user.displayName?.let { _userName.value = it }
                onSuccess()
            }, onError = onError)
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        authService.sendPasswordResetEmail(email, onSuccess, onError)
    }

    fun signInAnonymously(onSuccess: () -> Unit, onError: (String) -> Unit) {
        authService.signInAnonymously(onSuccess = {
            onSuccess()
        }, onError = onError)
    }

    fun updateUserProfile(
        displayName: String,
        profilePictureUri: String?,
        statusQuote: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (displayName.isNotBlank()) {
            _userName.value = displayName.trim()
        }
        if (profilePictureUri != null) {
            _userProfilePictureUri.value = profilePictureUri
        }
        if (!statusQuote.isNullOrBlank()) {
            _userStatusQuote.value = statusQuote.trim()
        }
        val photoUri = profilePictureUri?.let { android.net.Uri.parse(it) }
        authService.updateUserProfile(
            displayName = displayName.trim(),
            photoUri = photoUri,
            onSuccess = onSuccess,
            onError = {
                // If remote update fails (e.g. offline or guest), local state was still updated cleanly
                onSuccess()
            }
        )
    }

    fun signOut() {
        authService.signOut()
    }
}
