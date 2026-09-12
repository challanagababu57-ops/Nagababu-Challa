package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val isGroup: Boolean = false,
    val groupMembersCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val lastMessageSender: String = "",
    val lastMessageStatus: String = "READ", // SENDING, SENT, DELIVERED, READ
    val onlineStatus: String = "online",
    val phoneNumber: String? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String, // "me" or others
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "TEXT", // TEXT, IMAGE, VOICE, DOCUMENT, LOCATION
    val mediaUrl: String? = null,
    val durationSeconds: Int = 0,
    val isStarred: Boolean = false,
    val status: String = "READ", // SENDING, SENT, DELIVERED, READ
    val reaction: String? = null
)

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String? = null,
    val isMe: Boolean = false,
    val content: String,
    val mediaType: String = "TEXT", // TEXT, IMAGE
    val backgroundColorHex: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false,
    val viewCount: Int = 0
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val contactName: String,
    val contactAvatar: String? = null,
    val callType: String = "AUDIO", // AUDIO, VIDEO
    val direction: String = "INCOMING", // INCOMING, OUTGOING, MISSED
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val avatarUrl: String? = null,
    val isVerified: Boolean = true,
    val followersCount: String,
    val isFollowing: Boolean = false,
    val latestPost: String,
    val latestPostTime: String,
    val latestPostReactions: String
)
