package com.example.data.firestore

import android.util.Log
import com.example.data.entity.MessageEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Service to manage real-time messaging synchronization with Cloud Firestore.
 * Provides real-time snapshot listeners for chat messages and cloud persistence.
 */
class FirestoreMessagingService {

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    private val firestore: FirebaseFirestore?
        get() = if (isFirebaseAvailable) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.w("FirestoreMessaging", "Firestore instance not available", e)
                null
            }
        } else null

    fun isConfigured(): Boolean = firestore != null

    /**
     * Publishes a message to Cloud Firestore in real time under the chats/{chatId}/messages collection.
     */
    fun sendMessage(chatId: String, message: MessageEntity, onComplete: ((Boolean) -> Unit)? = null) {
        val db = firestore ?: run {
            onComplete?.invoke(false)
            return
        }

        try {
            val messageMap = hashMapOf(
                "id" to message.id,
                "chatId" to message.chatId,
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "type" to message.type,
                "mediaUrl" to (message.mediaUrl ?: ""),
                "durationSeconds" to message.durationSeconds,
                "status" to message.status,
                "isStarred" to message.isStarred,
                "reaction" to (message.reaction ?: "")
            )

            db.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(message.id)
                .set(messageMap)
                .addOnSuccessListener {
                    onComplete?.invoke(true)
                }
                .addOnFailureListener { e ->
                    Log.w("FirestoreMessaging", "Failed to save message to Firestore", e)
                    onComplete?.invoke(false)
                }
        } catch (e: Exception) {
            Log.w("FirestoreMessaging", "Error writing message to Firestore", e)
            onComplete?.invoke(false)
        }
    }

    /**
     * Observes incoming messages for a specific chat from Firestore in real time.
     */
    fun observeMessages(chatId: String): Flow<List<MessageEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            channel.close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("FirestoreMessaging", "Listen failed for chat: $chatId", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val messages = snapshot.documents.mapNotNull { doc ->
                            try {
                                MessageEntity(
                                    id = doc.getString("id") ?: doc.id,
                                    chatId = doc.getString("chatId") ?: chatId,
                                    senderId = doc.getString("senderId") ?: "",
                                    senderName = doc.getString("senderName") ?: "",
                                    text = doc.getString("text") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    type = doc.getString("type") ?: "TEXT",
                                    mediaUrl = doc.getString("mediaUrl")?.ifEmpty { null },
                                    durationSeconds = (doc.getLong("durationSeconds") ?: 0L).toInt(),
                                    status = doc.getString("status") ?: "SENT",
                                    isStarred = doc.getBoolean("isStarred") ?: false,
                                    reaction = doc.getString("reaction")?.ifEmpty { null }
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(messages)
                    }
                }
        } catch (e: Exception) {
            Log.w("FirestoreMessaging", "Error registering Firestore listener", e)
        }

        awaitClose {
            registration?.remove()
        }
    }
}
