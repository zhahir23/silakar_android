package com.polres.sipka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun NotificationScreen(user: LoginResponse, onBack: () -> Unit) {
    var notifications by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        loading = true
        runCatching { SipkaApi.service.notifications(user.id) }
            .onSuccess { notifications = it; error = null }
            .onFailure { error = "Notifikasi gagal dimuat. Periksa koneksi backend." }
        loading = false
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ScreenHeader(title = "Notifikasi", onBack = onBack)
            Spacer(Modifier.height(14.dp))

            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Navy)
                    }
                }
                error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(error!!, color = Color(0xFFDC2626), fontSize = 14.sp)
                    }
                }
                notifications.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada notifikasi.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications, key = { it.id }) { notification ->
                            NotificationCardItem(notification = notification)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCardItem(notification: NotificationItem) {
    val unread = notification.is_read?.contains("belum", ignoreCase = true) == true || notification.is_read == "false"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (unread) Color(0xFFF0FDF4) else Color.White,
        border = BorderStroke(1.dp, if (unread) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (unread) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF16A34A))
                )
                Spacer(Modifier.width(10.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = notification.judul ?: "Informasi Sistem",
                    color = Navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!notification.pesan.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = notification.pesan,
                        color = Color(0xFF475467),
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = notification.created_at?.replace("T", " ") ?: "",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}
