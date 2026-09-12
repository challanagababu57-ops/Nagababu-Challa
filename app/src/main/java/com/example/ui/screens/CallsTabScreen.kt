package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CallEntity
import com.example.ui.components.MeeChaatAvatar
import com.example.ui.components.formatChatTimestamp
import com.example.ui.theme.CallRed
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.viewmodel.MeeChaatViewModel

@Composable
fun CallsTabScreen(
    viewModel: MeeChaatViewModel,
    modifier: Modifier = Modifier
) {
    val calls by viewModel.calls.collectAsState()
    var showLinkDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Create Call Link Banner
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLinkDialog = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("create_call_link_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(WhatsAppGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Create call link",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Create call link",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Share a link for your MEECHAAT call",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recent Calls Header
            item {
                Text(
                    text = "Recent",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // Calls List
            items(calls, key = { it.id }) { call ->
                CallItemRow(
                    call = call,
                    onCallClick = {
                        viewModel.startCall(call.contactName, isVideo = call.callType == "VIDEO")
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // FAB: New Call
        FloatingActionButton(
            onClick = {
                viewModel.startCall("Sarah Jenkins", isVideo = false)
            },
            containerColor = WhatsAppGreenLight,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_call_fab")
        ) {
            Icon(
                imageVector = Icons.Default.AddIcCall,
                contentDescription = "New Call",
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = { Text("MEECHAAT Call Link", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Anyone with this link can join this call:")
                    Text(
                        text = "https://meechaat.call.app/link/mc-${System.currentTimeMillis() % 100000}",
                        color = WhatsAppGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Copy Link", color = WhatsAppGreenLight, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CallItemRow(
    call: CallEntity,
    onCallClick: () -> Unit
) {
    val isMissed = call.direction == "MISSED"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCallClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("call_item_${call.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeeChaatAvatar(name = call.contactName, size = 50.dp)

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.contactName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (isMissed) CallRed else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                when (call.direction) {
                    "MISSED" -> {
                        Icon(
                            imageVector = Icons.Default.CallMissed,
                            contentDescription = "Missed",
                            tint = CallRed,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    "INCOMING" -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallReceived,
                            contentDescription = "Incoming",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CallMade,
                            contentDescription = "Outgoing",
                            tint = WhatsAppGreen,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${formatChatTimestamp(call.timestamp)}${if (call.durationSeconds > 0) " (${call.durationSeconds / 60}m ${call.durationSeconds % 60}s)" else ""}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onCallClick) {
            Icon(
                imageVector = if (call.callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = if (call.callType == "VIDEO") "Video Call" else "Audio Call",
                tint = WhatsAppGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
