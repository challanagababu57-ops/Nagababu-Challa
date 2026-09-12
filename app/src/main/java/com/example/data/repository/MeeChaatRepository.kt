package com.example.data.repository

import android.content.Context
import com.example.data.database.MeeChaatDatabase
import com.example.data.entity.CallEntity
import com.example.data.entity.ChannelEntity
import com.example.data.entity.ChatEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.StatusEntity
import com.example.data.firestore.FirestoreMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class MeeChaatRepository(private val database: MeeChaatDatabase) {
    private val chatDao = database.chatDao()
    private val messageDao = database.messageDao()
    private val statusDao = database.statusDao()
    private val callDao = database.callDao()
    private val channelDao = database.channelDao()
    private val firestoreService = FirestoreMessagingService()

    val activeChats: Flow<List<ChatEntity>> = chatDao.getAllActiveChats()
    val archivedChats: Flow<List<ChatEntity>> = chatDao.getArchivedChats()
    val allStatuses: Flow<List<StatusEntity>> = statusDao.getAllStatuses()
    val allCalls: Flow<List<CallEntity>> = callDao.getAllCalls()
    val allChannels: Flow<List<ChannelEntity>> = channelDao.getAllChannels()

    fun getChat(chatId: String): Flow<ChatEntity?> = chatDao.getChatById(chatId)
    fun getMessages(chatId: String): Flow<List<MessageEntity>> {
        syncFirestoreMessages(chatId)
        return messageDao.getMessagesForChat(chatId)
    }

    private fun syncFirestoreMessages(chatId: String) {
        if (!firestoreService.isConfigured()) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestoreService.observeMessages(chatId).collect { remoteMessages ->
                    if (remoteMessages.isNotEmpty()) {
                        messageDao.insertAll(remoteMessages)
                    }
                }
            } catch (_: Exception) {
                // Graceful fallback to local Room persistence
            }
        }
    }

    suspend fun markChatRead(chatId: String) {
        chatDao.markAsRead(chatId)
    }

    suspend fun togglePin(chatId: String, currentPinned: Boolean) {
        chatDao.updatePinned(chatId, !currentPinned)
    }

    suspend fun toggleMute(chatId: String, currentMuted: Boolean) {
        chatDao.updateMuted(chatId, !currentMuted)
    }

    suspend fun toggleArchive(chatId: String, currentArchived: Boolean) {
        chatDao.updateArchived(chatId, !currentArchived)
    }

    suspend fun deleteChat(chatId: String) {
        chatDao.deleteChat(chatId)
        messageDao.clearChatMessages(chatId)
    }

    suspend fun clearMessages(chatId: String) {
        messageDao.clearChatMessages(chatId)
    }

    suspend fun setReaction(messageId: String, reaction: String?) {
        messageDao.updateReaction(messageId, reaction)
    }

    suspend fun toggleStarred(messageId: String, currentStarred: Boolean) {
        messageDao.updateStarred(messageId, !currentStarred)
    }

    suspend fun sendMessage(
        chatId: String,
        text: String,
        type: String = "TEXT",
        mediaUrl: String? = null,
        durationSeconds: Int = 0
    ) {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "me",
            senderName = "You",
            text = text,
            timestamp = now,
            type = type,
            mediaUrl = mediaUrl,
            durationSeconds = durationSeconds,
            status = "READ"
        )
        messageDao.insertMessage(message)

        // Real-time synchronization to Cloud Firestore if enabled
        firestoreService.sendMessage(chatId, message)

        // Update chat preview
        val previewText = when (type) {
            "VOICE" -> "🎤 Voice message ($durationSeconds s)"
            "IMAGE" -> "📷 Photo"
            "DOCUMENT" -> "📄 Document"
            "LOCATION" -> "📍 Location"
            else -> text
        }
        val chat = chatDao.getChatById(chatId).first()
        if (chat != null) {
            chatDao.update(
                chat.copy(
                    lastMessageText = previewText,
                    lastMessageTimestamp = now,
                    lastMessageSender = "You",
                    lastMessageStatus = "READ"
                )
            )
        }

        // Trigger intelligent simulated auto-reply from contact
        triggerSimulatedReply(chatId, text)
    }

    private fun triggerSimulatedReply(chatId: String, userText: String) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1500)
            val chat = chatDao.getChatById(chatId).first() ?: return@launch
            val replyText = generateReply(chat.name, userText)
            val replyTime = System.currentTimeMillis()
            val replyMessage = MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = chatId,
                senderId = chat.id,
                senderName = chat.name,
                text = replyText,
                timestamp = replyTime,
                type = "TEXT",
                status = "READ"
            )
            messageDao.insertMessage(replyMessage)
            chatDao.update(
                chat.copy(
                    lastMessageText = replyText,
                    lastMessageTimestamp = replyTime,
                    lastMessageSender = chat.name,
                    lastMessageStatus = "READ",
                    onlineStatus = "online"
                )
            )
        }
    }

    private fun generateReply(contactName: String, query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hey there! How is everything going with you?"
            lower.contains("how are you") ->
                "I'm doing great! Just catching up on some work. What about you?"
            lower.contains("where") || lower.contains("meet") ->
                "Sounds like a plan! Let's meet up around the downtown area later this afternoon."
            lower.contains("call") ->
                "Sure, give me a few minutes to wrap this up and I'll jump on a call."
            lower.contains("photo") || lower.contains("pic") ->
                "Loved that picture! Looks fantastic ✨"
            lower.contains("yes") || lower.contains("sure") || lower.contains("ok") ->
                "Awesome! Looking forward to it 👍"
            lower.contains("thank") ->
                "You're very welcome! Always happy to help anytime 😊"
            else ->
                "Got it! Thanks for the update, will check it out shortly!"
        }
    }

    suspend fun addStatus(
        content: String,
        mediaType: String = "TEXT",
        bgColorHex: String? = "#005C4B"
    ) {
        val status = StatusEntity(
            id = UUID.randomUUID().toString(),
            userId = "me",
            userName = "My Status",
            isMe = true,
            content = content,
            mediaType = mediaType,
            backgroundColorHex = bgColorHex,
            timestamp = System.currentTimeMillis(),
            isViewed = true,
            viewCount = 0
        )
        statusDao.insertStatus(status)
    }

    suspend fun markStatusViewed(statusId: String) {
        statusDao.markStatusViewed(statusId)
    }

    suspend fun logCall(
        contactName: String,
        contactAvatar: String? = null,
        callType: String,
        direction: String,
        durationSeconds: Int = 0
    ) {
        val call = CallEntity(
            id = UUID.randomUUID().toString(),
            contactName = contactName,
            contactAvatar = contactAvatar,
            callType = callType,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSeconds
        )
        callDao.insertCall(call)
    }

    suspend fun clearCalls() {
        callDao.clearCallLog()
    }

    suspend fun toggleChannelFollow(channelId: String, currentFollow: Boolean) {
        channelDao.updateFollowing(channelId, !currentFollow)
    }

    suspend fun createNewChat(name: String, phoneNumber: String, isGroup: Boolean = false): String {
        val id = UUID.randomUUID().toString()
        val newChat = ChatEntity(
            id = id,
            name = name,
            isGroup = isGroup,
            groupMembersCount = if (isGroup) 3 else 0,
            onlineStatus = if (isGroup) "3 members" else "online",
            phoneNumber = phoneNumber,
            lastMessageText = "Tap to start conversation",
            lastMessageTimestamp = System.currentTimeMillis(),
            lastMessageSender = ""
        )
        chatDao.insertOrUpdate(newChat)
        return id
    }

    suspend fun checkAndSeedInitialData() {
        val existingChats = chatDao.getAllActiveChats().first()
        if (existingChats.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val oneMin = 60 * 1000L
        val oneHour = 60 * oneMin
        val oneDay = 24 * oneHour

        // Seed Chats
        val initialChats = listOf(
            ChatEntity(
                id = "chat_sarah",
                name = "Sarah Jenkins",
                isGroup = false,
                isPinned = true,
                unreadCount = 2,
                lastMessageText = "Are we still meeting at the café at 5? ☕",
                lastMessageTimestamp = now - 5 * oneMin,
                lastMessageSender = "Sarah Jenkins",
                lastMessageStatus = "READ",
                onlineStatus = "online",
                phoneNumber = "+1 555-0142"
            ),
            ChatEntity(
                id = "chat_family",
                name = "Family Group 🏡",
                isGroup = true,
                groupMembersCount = 6,
                isPinned = true,
                unreadCount = 5,
                lastMessageText = "Mom: Don't forget dad's birthday dinner on Sunday! 🎂",
                lastMessageTimestamp = now - 22 * oneMin,
                lastMessageSender = "Mom",
                lastMessageStatus = "READ",
                onlineStatus = "Mom, Dad, Emma, Alex, You",
                phoneNumber = null
            ),
            ChatEntity(
                id = "chat_david",
                name = "David Miller",
                isGroup = false,
                isPinned = false,
                unreadCount = 0,
                lastMessageText = "🎤 Voice message (0:18)",
                lastMessageTimestamp = now - 45 * oneMin,
                lastMessageSender = "David Miller",
                lastMessageStatus = "READ",
                onlineStatus = "last seen today at 11:20 AM",
                phoneNumber = "+1 555-0198"
            ),
            ChatEntity(
                id = "chat_tech",
                name = "Tech Innovations Team 💻",
                isGroup = true,
                groupMembersCount = 14,
                isPinned = false,
                unreadCount = 0,
                lastMessageText = "Marcus: Just pushed the latest v2.4 build to review.",
                lastMessageTimestamp = now - 2 * oneHour,
                lastMessageSender = "Marcus",
                lastMessageStatus = "READ",
                onlineStatus = "14 members",
                phoneNumber = null
            ),
            ChatEntity(
                id = "chat_emma",
                name = "Emma Watson",
                isGroup = false,
                isPinned = false,
                unreadCount = 0,
                lastMessageText = "Haha that video was absolutely hilarious! 😂",
                lastMessageTimestamp = now - 5 * oneHour,
                lastMessageSender = "Emma Watson",
                lastMessageStatus = "READ",
                onlineStatus = "last seen today at 09:14 AM",
                phoneNumber = "+1 555-0177"
            ),
            ChatEntity(
                id = "chat_liam",
                name = "Liam Roberts",
                isGroup = false,
                isPinned = false,
                unreadCount = 0,
                lastMessageText = "📍 Shared live location",
                lastMessageTimestamp = now - 1 * oneDay,
                lastMessageSender = "Liam Roberts",
                lastMessageStatus = "READ",
                onlineStatus = "offline",
                phoneNumber = "+1 555-0123"
            ),
            ChatEntity(
                id = "chat_sophia",
                name = "Sophia Chen",
                isGroup = false,
                isPinned = false,
                unreadCount = 0,
                lastMessageText = "📄 Sent project_specs_v3.pdf (2.4 MB)",
                lastMessageTimestamp = now - 2 * oneDay,
                lastMessageSender = "Sophia Chen",
                lastMessageStatus = "READ",
                onlineStatus = "last seen yesterday",
                phoneNumber = "+1 555-0189"
            )
        )
        chatDao.insertAll(initialChats)

        // Seed Messages for Sarah Jenkins
        val sarahMessages = listOf(
            MessageEntity(
                id = "m_sarah_1",
                chatId = "chat_sarah",
                senderId = "chat_sarah",
                senderName = "Sarah Jenkins",
                text = "Hey! Hope you're having a productive morning!",
                timestamp = now - 30 * oneMin,
                type = "TEXT",
                status = "READ"
            ),
            MessageEntity(
                id = "m_sarah_2",
                chatId = "chat_sarah",
                senderId = "me",
                senderName = "You",
                text = "Hey Sarah! Yes, just finishing up the MEECHAAT features.",
                timestamp = now - 25 * oneMin,
                type = "TEXT",
                status = "READ"
            ),
            MessageEntity(
                id = "m_sarah_3",
                chatId = "chat_sarah",
                senderId = "chat_sarah",
                senderName = "Sarah Jenkins",
                text = "That looks so clean! The green theme is spot on.",
                timestamp = now - 15 * oneMin,
                type = "TEXT",
                status = "READ",
                reaction = "❤️"
            ),
            MessageEntity(
                id = "m_sarah_4",
                chatId = "chat_sarah",
                senderId = "chat_sarah",
                senderName = "Sarah Jenkins",
                text = "Are we still meeting at the café at 5? ☕",
                timestamp = now - 5 * oneMin,
                type = "TEXT",
                status = "READ"
            )
        )

        // Seed Messages for David Miller (includes voice note)
        val davidMessages = listOf(
            MessageEntity(
                id = "m_david_1",
                chatId = "chat_david",
                senderId = "me",
                senderName = "You",
                text = "Hey David, did you check the audio sample?",
                timestamp = now - 1 * oneHour,
                type = "TEXT",
                status = "READ"
            ),
            MessageEntity(
                id = "m_david_2",
                chatId = "chat_david",
                senderId = "chat_david",
                senderName = "David Miller",
                text = "Yes! Check out this guitar riff I recorded earlier today 🎸",
                timestamp = now - 50 * oneMin,
                type = "TEXT",
                status = "READ"
            ),
            MessageEntity(
                id = "m_david_3",
                chatId = "chat_david",
                senderId = "chat_david",
                senderName = "David Miller",
                text = "Voice note riff",
                timestamp = now - 45 * oneMin,
                type = "VOICE",
                durationSeconds = 18,
                status = "READ",
                reaction = "🔥"
            )
        )

        // Seed Messages for Family Group
        val familyMessages = listOf(
            MessageEntity(
                id = "m_fam_1",
                chatId = "chat_family",
                senderId = "dad",
                senderName = "Dad",
                text = "Good morning everyone! Have a blessed day ahead.",
                timestamp = now - 3 * oneHour,
                type = "TEXT",
                status = "READ"
            ),
            MessageEntity(
                id = "m_fam_2",
                chatId = "chat_family",
                senderId = "emma",
                senderName = "Emma",
                text = "Morning Dad! See everyone this weekend!",
                timestamp = now - 2 * oneHour,
                type = "TEXT",
                status = "READ"
            ),
            MessageEntity(
                id = "m_fam_3",
                chatId = "chat_family",
                senderId = "mom",
                senderName = "Mom",
                text = "Don't forget dad's birthday dinner on Sunday! 🎂",
                timestamp = now - 22 * oneMin,
                type = "TEXT",
                status = "READ",
                reaction = "🎉"
            )
        )

        messageDao.insertAll(sarahMessages + davidMessages + familyMessages)

        // Seed Statuses
        val initialStatuses = listOf(
            StatusEntity(
                id = "st_sarah",
                userId = "chat_sarah",
                userName = "Sarah Jenkins",
                content = "Sunset walk by the beach... perfectly serene evening 🌅🌊",
                mediaType = "TEXT",
                backgroundColorHex = "#703ABE",
                timestamp = now - 35 * oneMin,
                isViewed = false,
                viewCount = 14
            ),
            StatusEntity(
                id = "st_david",
                userId = "chat_david",
                userName = "David Miller",
                content = "Live acoustic set tonight at The Blue Note! Come say hi 🎸🎶",
                mediaType = "TEXT",
                backgroundColorHex = "#005C4B",
                timestamp = now - 2 * oneHour,
                isViewed = false,
                viewCount = 28
            ),
            StatusEntity(
                id = "st_emma",
                userId = "chat_emma",
                userName = "Emma Watson",
                content = "Weekend hiking escape in the mountains ⛰️🌲 fresh air!",
                mediaType = "TEXT",
                backgroundColorHex = "#1E88E5",
                timestamp = now - 4 * oneHour,
                isViewed = true,
                viewCount = 42
            ),
            StatusEntity(
                id = "st_liam",
                userId = "chat_liam",
                userName = "Liam Roberts",
                content = "Coffee + Code + Rainy weather = Pure focus ☕💻☔",
                mediaType = "TEXT",
                backgroundColorHex = "#D81B60",
                timestamp = now - 6 * oneHour,
                isViewed = true,
                viewCount = 19
            )
        )
        statusDao.insertAll(initialStatuses)

        // Seed Calls
        val initialCalls = listOf(
            CallEntity(
                id = "call_1",
                contactName = "Sarah Jenkins",
                callType = "AUDIO",
                direction = "INCOMING",
                timestamp = now - 1 * oneHour,
                durationSeconds = 860
            ),
            CallEntity(
                id = "call_2",
                contactName = "David Miller",
                callType = "VIDEO",
                direction = "MISSED",
                timestamp = now - 3 * oneHour,
                durationSeconds = 0
            ),
            CallEntity(
                id = "call_3",
                contactName = "Liam Roberts",
                callType = "AUDIO",
                direction = "OUTGOING",
                timestamp = now - 1 * oneDay,
                durationSeconds = 225
            ),
            CallEntity(
                id = "call_4",
                contactName = "Sophia Chen",
                callType = "VIDEO",
                direction = "INCOMING",
                timestamp = now - 2 * oneDay,
                durationSeconds = 1480
            )
        )
        callDao.insertAll(initialCalls)

        // Seed Channels
        val initialChannels = listOf(
            ChannelEntity(
                id = "ch_meechaat",
                name = "MEECHAAT Official",
                handle = "@meechaat",
                isVerified = true,
                followersCount = "28.5M followers",
                isFollowing = true,
                latestPost = "Welcome to MEECHAAT! Enjoy high fidelity calls, status updates, encrypted chats, and seamless channels.",
                latestPostTime = "Today, 10:00 AM",
                latestPostReactions = "❤️ 45K  👍 28K  🎉 14K"
            ),
            ChannelEntity(
                id = "ch_tech",
                name = "Tech Radar Daily",
                handle = "@techradar",
                isVerified = true,
                followersCount = "14.2M followers",
                isFollowing = false,
                latestPost = "The new era of mobile intelligence is here. Faster processing, on-device models, and battery breakthroughs.",
                latestPostTime = "Yesterday",
                latestPostReactions = "💡 8.2K  🚀 12K"
            ),
            ChannelEntity(
                id = "ch_natgeo",
                name = "Nature & Planet",
                handle = "@natureplanet",
                isVerified = true,
                followersCount = "52.1M followers",
                isFollowing = true,
                latestPost = "Rare emerald aurora australis lights captured over southern skies 🌌",
                latestPostTime = "2 days ago",
                latestPostReactions = "❤️ 98K  ✨ 34K"
            )
        )
        channelDao.insertAll(initialChannels)
    }
}
