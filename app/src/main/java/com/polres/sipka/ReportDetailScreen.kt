package com.polres.sipka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
internal fun ReportDetailScreen(
    report: ReportItem,
    viewer: LoginResponse? = null,
    onBack: () -> Unit,
    onUpdated: (ReportItem) -> Unit = {}
) {
    val canEdit = viewer?.isAdmin() == true
    var status by remember(report.id) { mutableStateOf(report.status ?: "pending") }
    var catatan by remember(report.id) { mutableStateOf(report.catatan_atasan.orEmpty()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        ScreenHeader(if (canEdit) "Kelola Laporan" else "Detail Laporan", onBack)
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Border)
        Spacer(Modifier.height(18.dp))

        // Hero Image Foto Dokumentasi Kegiatan
        if (!report.foto_url.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                AsyncImage(
                    model = report.foto_url,
                    contentDescription = "Foto Dokumentasi",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(18.dp))
        }

        DetailField("Judul", report.judul)
        DetailField("Jenis Kegiatan", report.jenis_kegiatan)
        DetailField("Tanggal Kegiatan", report.tanggal_kegiatan)
        DetailField("Lokasi", report.lokasi)
        DetailField("Deskripsi", report.deskripsi)

        if (canEdit) {
            Spacer(Modifier.height(8.dp))
            Text("Status", color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("pending", "approved", "rejected").forEach { option ->
                    FilterChip(
                        selected = status.equals(option, ignoreCase = true),
                        onClick = { status = option },
                        label = { Text(option.replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan admin") },
                minLines = 3,
                shape = RoundedCornerShape(4.dp)
            )
            if (message != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    message!!,
                    color = if (message!!.startsWith("Berhasil")) Color(0xFF16804A) else Color(0xFFB42318),
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    loading = true
                    message = null
                    scope.launch {
                        message = try {
                            val response = SipkaApi.service.updateReport(
                                report.id,
                                UpdateReportRequest(
                                    status = status,
                                    catatan_atasan = catatan,
                                    foto_url = report.foto_url
                                ),
                                editorId = viewer!!.id
                            )
                            if (response.isSuccessful) {
                                response.body()?.let(onUpdated)
                                "Berhasil memperbarui laporan."
                            } else "Gagal menyimpan perubahan laporan."
                        } catch (_: Exception) {
                            "Tidak dapat terhubung ke server."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Navy)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Simpan perubahan")
            }
        } else {
            DetailField("Status", report.status?.replaceFirstChar { it.uppercase() })
            DetailField("Catatan Atasan", report.catatan_atasan)
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = if (canEdit) Color(0xFF667085) else Navy)
        ) { Text("Tutup") }
    }
}

@Composable
private fun DetailField(label: String, value: String?) {
    Column(Modifier.fillMaxWidth().padding(bottom = 15.dp)) {
        Text(label, color = Muted, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value?.takeIf { it.isNotBlank() } ?: "Belum tersedia", color = Navy, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
