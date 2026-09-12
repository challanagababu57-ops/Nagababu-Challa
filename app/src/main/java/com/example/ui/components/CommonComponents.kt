package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlueTick
import com.example.ui.theme.GreyTick
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun MeeChaatAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 50.dp,
    hasStatusStory: Boolean = false,
    isStatusViewed: Boolean = false,
    showOnlineBadge: Boolean = false,
    isGroup: Boolean = false
) {
    val initials = if (isGroup) {
        "👥"
    } else {
        name.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifEmpty { "?" }
    }

    val hash = abs(name.hashCode())
    val avatarColors = listOf(
        Pair(Color(0xFF008069), Color(0xFF00A884)),
        Pair(Color(0xFF075E54), Color(0xFF128C7E)),
        Pair(Color(0xFF3F51B5), Color(0xFF5C6BC0)),
        Pair(Color(0xFF7E57C2), Color(0xFF9575CD)),
        Pair(Color(0xFFE91E63), Color(0xFFF06292)),
        Pair(Color(0xFF00897B), Color(0xFF26A69A)),
        Pair(Color(0xFFFB8C00), Color(0xFFFFA726)),
        Pair(Color(0xFF43A047), Color(0xFF66BB6A))
    )
    val colorPair = avatarColors[hash % avatarColors.size]

    val borderModifier = if (hasStatusStory) {
        val borderColor = if (isStatusViewed) Color.Gray.copy(alpha = 0.6f) else WhatsAppGreenLight
        Modifier.border(2.5.dp, borderColor, CircleShape).padding(2.5.dp)
    } else {
        Modifier
    }

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(borderModifier)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(colorPair.first, colorPair.second)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }

        if (showOnlineBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(WhatsAppGreenLight)
            )
        }
    }
}

@Composable
fun MessageStatusTicks(
    status: String,
    modifier: Modifier = Modifier,
    size: Dp = 15.dp
) {
    when (status.uppercase()) {
        "READ" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = BlueTick,
                modifier = modifier.size(size)
            )
        }
        "DELIVERED" -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = GreyTick,
                modifier = modifier.size(size)
            )
        }
        "SENT" -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                tint = GreyTick,
                modifier = modifier.size(size)
            )
        }
        else -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Sending",
                tint = GreyTick,
                modifier = modifier.size(size * 0.85f)
            )
        }
    }
}

fun formatChatTimestamp(timestamp: Long): String {
    val now = Calendar.getInstance()
    val msgTime = Calendar.getInstance().apply { timeInMillis = timestamp }

    return if (now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR)) {
        if (now.get(Calendar.DAY_OF_YEAR) == msgTime.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        } else if (now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1) {
            "Yesterday"
        } else if (now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) < 7) {
            SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
        } else {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    } else {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }
}

fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}

fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes minutes ago"
        hours < 24 -> "$hours hours ago"
        days == 1L -> "Yesterday"
        else -> "$days days ago"
    }
}

@Composable
fun AttachmentOptionItem(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
