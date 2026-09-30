package com.polres.sipka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ReportListScreen(
    user: LoginResponse,
    onBack: () -> Unit,
    onCreateReport: () -> Unit,
    archiveOnly: Boolean = false,
    onSelectReport: (ReportItem) -> Unit
) {
    var reports by remember { mutableStateOf<List<ReportItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(if (archiveOnly) "Disetujui" else "Semua") }

    val screenTitle = when {
        archiveOnly -> "Arsip Laporan"
        user.isAdmin() -> "Data Laporan Admin"
        else -> "Laporan Kinerja"
    }

    LaunchedEffect(user.id, archiveOnly) {
        loading = true
        runCatching {
            if (user.isAdmin()) SipkaApi.service.reports(viewerId = user.id)
            else SipkaApi.service.reports(userId = user.id)
        }
            .onSuccess { reports = it; error = null }
            .onFailure { error = "Daftar laporan gagal dimuat. Pastikan backend aktif." }
        loading = false
    }

    val filteredReports = reports.filter { report ->
        val status = report.status.orEmpty().lowercase()
        if (archiveOnly) {
            status in listOf("approved", "disetujui", "diterima")
        } else {
            when (selectedFilter) {
                "Menunggu" -> status in listOf("pending", "menunggu", "menunggu persetujuan")
                "Disetujui" -> status in listOf("approved", "disetujui", "diterima")
                "Ditolak" -> status in listOf("rejected", "ditolak")
                else -> true
            }
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header Screen dengan Back Button 48dp
            ScreenHeader(title = screenTitle, onBack = onBack)

            // Tombol Buat Laporan (jika bukan arsip dan bukan admin)
            if (!archiveOnly && !user.isAdmin()) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onCreateReport,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("+ Buat Laporan Baru", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Filter Tabs
            if (!archiveOnly) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Menunggu", "Disetujui", "Ditolak").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier.clickable { selectedFilter = filter },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Navy else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) Navy else Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.White else Navy,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Body Content (Loading, Error, Empty, List)
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
                filteredReports.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (reports.isEmpty()) {
                                if (user.isAdmin()) "Belum ada laporan di sistem." else "Belum ada laporan untuk akun ini."
                            } else {
                                "Tidak ada laporan dengan status $selectedFilter."
                            },
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredReports, key = { it.id }) { report ->
                            ReportCardItem(report = report, onClick = { onSelectReport(report) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCardItem(
    report: ReportItem,
    onClick: () -> Unit
) {
    val statusRaw = report.status.orEmpty().lowercase()
    val (statusLabel, statusBg, statusColor) = when (statusRaw) {
        "approved", "disetujui", "diterima" -> Triple("Disetujui", Color(0xFFDCFCE7), Color(0xFF16A34A))
        "rejected", "ditolak" -> Triple("Ditolak", Color(0xFFFEE2E2), Color(0xFFDC2626))
        else -> Triple("Menunggu", Color(0xFFEFF8FF), Color(0xFF2876A8))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.judul ?: "Laporan Kegiatan",
                    color = Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (!report.deskripsi.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = report.deskripsi,
                    color = Color(0xFF475467),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏷️ ${report.jenis_kegiatan ?: "Kegiatan"}",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
                Text(
                    text = "📅 ${report.tanggal_kegiatan ?: report.created_at?.take(10) ?: "-"}",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
internal fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp)
        ) {
            Text("‹", color = Navy, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(6.dp))
        Text(title, color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
