package com.polres.sipka

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
@Composable
internal fun HomeScreen(
    user: LoginResponse,
    onCreateReport: () -> Unit,
    onViewReports: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    var summary by remember { mutableStateOf<DashboardSummary?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user.id) {
        loading = true
        runCatching { SipkaApi.service.dashboard(user.id) }
            .onSuccess { summary = it; error = null }
            .onFailure { error = "Dashboard tidak dapat dimuat. Pastikan server dan database aktif." }
        loading = false
    }

    Surface(Modifier.fillMaxSize(), color = Color.White) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("SP", color = Color(0xFF31558C), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(9.dp))
                Text("SIPKA", color = Navy, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Box {
                    Surface(
                        modifier = Modifier.size(38.dp).clickable(onClick = onOpenNotifications),
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text("!", color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).size(9.dp),
                        shape = RoundedCornerShape(50), color = Color(0xFF2876A8)
                    ) {}
                }
            }

            Spacer(Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(Modifier.size(54.dp), shape = RoundedCornerShape(2.dp), color = Color(0xFF536C9C)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("A", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    val greeting = when (hour) { in 4..10 -> "Selamat pagi"; in 11..14 -> "Selamat siang"; in 15..18 -> "Selamat sore"; else -> "Selamat malam" }
                    Text("$greeting, ${user.namaLengkap?.substringBefore(" ") ?: "Anggota"}", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("${user.role ?: "Anggota"} – SIPKA", color = Muted, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(34.dp))
            if (loading) {
                Box(Modifier.fillMaxWidth().height(75.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Navy, strokeWidth = 2.dp)
                }
            } else if (summary != null) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    StatItem("Total Laporan", summary!!.totalLaporan.toString(), Navy, Modifier.weight(1f))
                    StatItem("Menunggu", summary!!.menunggu.toString(), Color(0xFF2876A8), Modifier.weight(1f))
                    StatItem("Disetujui", summary!!.disetujui.toString(), Color(0xFF18A957), Modifier.weight(1f))
                }
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = Color(0xFFB42318), fontSize = 12.sp)
            }

            Spacer(Modifier.height(34.dp))
            Button(
                onClick = onCreateReport,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                Text("+ Buat Laporan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("LAPORAN TERBARU", color = Color(0xFF667085), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("Lihat Semua", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onViewReports))
            }
            Spacer(Modifier.height(18.dp))
            when {
                loading -> Unit
                summary?.laporanTerbaru.isNullOrEmpty() -> Text("Belum ada laporan.", color = Muted, fontSize = 13.sp)
                else -> summary!!.laporanTerbaru.forEachIndexed { index, report ->
                    DashboardReportRow(report, user.role)
                    if (index != summary!!.laporanTerbaru.lastIndex) Spacer(Modifier.height(26.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = Muted, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DashboardReportRow(report: ReportSummary, role: String?) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(report.judul ?: "Laporan", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AssistChip(
                onClick = {},
                label = { Text(report.status?.replaceFirstChar { it.uppercase() } ?: "Status", fontSize = 10.sp) },
                colors = AssistChipDefaults.assistChipColors(labelColor = Navy),
                border = BorderStroke(1.dp, Border),
                modifier = Modifier.height(29.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(report.deskripsi ?: "Deskripsi singkat laporan...", color = Muted, fontSize = 11.sp, maxLines = 1)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFDDE1E7)))
        Spacer(Modifier.height(9.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val date = report.createdAt?.replace("T", ", ")?.take(17) ?: ""
            Text(date, color = Muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
            Text(report.jenisKegiatan ?: role ?: "Unit", color = Navy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
