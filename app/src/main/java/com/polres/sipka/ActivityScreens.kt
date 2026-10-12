package com.polres.sipka

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
internal fun ProfileDashboardScreen(
    user: LoginResponse,
    onUserUpdated: (LoginResponse) -> Unit = {},
    onReports: () -> Unit,
    onAdminReports: () -> Unit = onReports,
    onDocumentation: () -> Unit,
    onArchive: () -> Unit,
    onLogs: () -> Unit,
    onNotifications: () -> Unit,
    onCreateReport: () -> Unit,
    onLogout: () -> Unit
) {
    var currentUser by remember { mutableStateOf(user) }
    var summary by remember { mutableStateOf<DashboardSummary?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser.id) {
        runCatching { SipkaApi.service.dashboard(currentUser.id) }.onSuccess { summary = it }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // App Bar Title & Notification Icon
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_humas),
                    contentDescription = "Logo Humas",
                    modifier = Modifier.size(42.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("SIPKA", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Polres Sukoharjo", color = Muted, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Box {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = onNotifications),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Border)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔔", fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ==========================================
            // HEADER DASHBOARD: ROW LAYOUT DENGAN FOTO & DATA
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 2.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sebelah Kiri: Foto Profil (CircleShape)
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFF2876A8), CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!currentUser.fotoProfil.isNullOrBlank()) {
                                AsyncImage(
                                    model = currentUser.fotoProfil,
                                    contentDescription = "Foto Profil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = currentUser.namaLengkap?.firstOrNull()?.uppercase() ?: "P",
                                    color = Navy,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.width(16.dp))

                        // Sebelah Kanan: Nama, NRP, Email, Badge Status & Kepala Dinas
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.namaLengkap ?: "Anggota SIPKA",
                                color = Navy,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "NRP: ${currentUser.nrp?.ifBlank { "-" } ?: "-"}",
                                color = Color(0xFF475467),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = currentUser.email,
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            // Badge Status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEFF8FF),
                                    border = BorderStroke(1.dp, Color(0xFFB2DDFF))
                                ) {
                                    Text(
                                        text = currentUser.statusAnggota ?: currentUser.role?.replaceFirstChar { it.uppercase() } ?: "Anggota",
                                        color = Color(0xFF175CD3),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (!currentUser.kepalaDinas.isNullOrBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF4F3FF),
                                        border = BorderStroke(1.dp, Color(0xFFD9D6FE))
                                    ) {
                                        Text(
                                            text = "Kepala: ${currentUser.kepalaDinas}",
                                            color = Color(0xFF5925DC),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
                    Spacer(Modifier.height(8.dp))

                    // Tombol Edit Profil
                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text("✏️ Edit Profil & Foto", fontSize = 12.sp, color = Navy, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ==========================================
            // STATISTIK LAPORAN
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatBox("Total Laporan", summary?.totalLaporan?.toString() ?: "-", Navy)
                    StatBox("Menunggu", summary?.menunggu?.toString() ?: "-", Color(0xFF2876A8))
                    StatBox("Disetujui", summary?.disetujui?.toString() ?: "-", Color(0xFF16A34A))
                }
            }

            // Tombol Buat Laporan (jika bukan admin)
            if (!currentUser.isAdmin()) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onCreateReport,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy)
                ) {
                    Text("+ Buat Laporan Baru", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("MENU UTAMA", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))

            // ==========================================
            // MENU NAVIGASI DASHBOARD
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column {
                    if (currentUser.isAdmin()) {
                        DashboardMenuItem("📋", "Data Laporan Admin", "Lihat dan edit semua laporan anggota", onAdminReports)
                        DividerLine()
                    }
                    DashboardMenuItem("📝", "Data Laporan", "Lihat laporan kinerja Anda", onReports)
                    DividerLine()
                    DashboardMenuItem("📁", "Dokumentasi", "Foto dan dokumentasi kegiatan kepolisian", onDocumentation)
                    DividerLine()
                    DashboardMenuItem("📦", "Arsip Laporan", "Laporan yang telah diverifikasi dan disetujui", onArchive)
                    DividerLine()
                    DashboardMenuItem("⏱️", "Log Aktivitas", "Riwayat pencatatan dan aktivitas akun", onLogs)
                    DividerLine()
                    DashboardMenuItem("🔔", "Notifikasi", "Pemberitahuan persetujuan dan catatan atasan", onNotifications)
                    DividerLine()
                    DashboardMenuItem("🚪", "Keluar Akun", "Keluar dari sesi aplikasi SIPKA", onLogout, isDestructive = true)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ==========================================
    // DIALOG EDIT PROFIL & GANTI FOTO
    // ==========================================
    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUser = currentUser,
            onDismiss = { showEditProfileDialog = false },
            onSaved = { updated ->
                currentUser = updated
                onUserUpdated(updated)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
private fun StatBox(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = Color(0xFF64748B), fontSize = 11.sp)
    }
}

@Composable
private fun DashboardMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (isDestructive) Color(0xFFDC2626) else Navy,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(subtitle, color = Color(0xFF64748B), fontSize = 11.sp)
        }
        Text("›", color = Color(0xFF94A3B8), fontSize = 20.sp)
    }
}

@Composable
private fun DividerLine() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFF1F5F9)))
}

@Composable
private fun EditProfileDialog(
    currentUser: LoginResponse,
    onDismiss: () -> Unit,
    onSaved: (LoginResponse) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.namaLengkap.orEmpty()) }
    var nrp by remember { mutableStateOf(currentUser.nrp.orEmpty()) }
    var statusAnggota by remember { mutableStateOf(currentUser.statusAnggota ?: "Anggota Unit") }
    var kepalaDinas by remember { mutableStateOf(currentUser.kepalaDinas.orEmpty()) }
    var photoUriString by remember { mutableStateOf(currentUser.fotoProfil.orEmpty()) }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUriString = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profil Pengguna", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Navy) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF2876A8), CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUriString.isNotBlank()) {
                            AsyncImage(
                                model = photoUriString,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = name.firstOrNull()?.uppercase() ?: "P",
                                color = Navy,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("📷 Ganti Foto Profil", fontSize = 12.sp, color = Navy)
                    }
                }

                Spacer(Modifier.height(14.dp))
                FieldLabel("Nama Lengkap")
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(Modifier.height(10.dp))
                FieldLabel("NRP (Nomor Registrasi Pokok)")
                OutlinedTextField(
                    value = nrp,
                    onValueChange = { nrp = it; errorMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(Modifier.height(10.dp))
                FieldLabel("Status Anggota / Unit")
                OutlinedTextField(
                    value = statusAnggota,
                    onValueChange = { statusAnggota = it; errorMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: Anggota Unit Reskrim") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(Modifier.height(10.dp))
                FieldLabel("Nama Kepala Dinas")
                OutlinedTextField(
                    value = kepalaDinas,
                    onValueChange = { kepalaDinas = it; errorMsg = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Contoh: AKBP Sigit, S.I.K., M.H.") },
                    singleLine = true,
                    shape = RoundedCornerShape(6.dp)
                )

                if (errorMsg != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg!!, color = Color(0xFFDC2626), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMsg = "Nama tidak boleh kosong."
                        return@Button
                    }
                    loading = true
                    errorMsg = null
                    scope.launch {
                        try {
                            val response = SipkaApi.service.updateProfile(
                                id = currentUser.id,
                                request = UpdateProfileRequest(
                                    namaLengkap = name.trim(),
                                    nrp = nrp.trim(),
                                    fotoProfil = photoUriString.trim().ifBlank { null },
                                    statusAnggota = statusAnggota.trim(),
                                    kepalaDinas = kepalaDinas.trim()
                                )
                            )
                            if (response.isSuccessful && response.body() != null) {
                                onSaved(response.body()!!)
                            } else {
                                errorMsg = "Gagal memperbarui profil (HTTP ${response.code()})."
                            }
                        } catch (e: Exception) {
                            errorMsg = "Koneksi ke backend gagal."
                        } finally {
                            loading = false
                        }
                    }
                },
                enabled = !loading,
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(6.dp)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Simpan")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                Text("Batal", color = Navy)
            }
        }
    )
}

// Model data gabungan untuk Dokumentasi yang hidup & lengkap
private data class DisplayDocItem(
    val id: Long,
    val title: String,
    val imageUrl: String,
    val lokasi: String,
    val tanggal: String,
    val deskripsi: String,
    val status: String,
    val jenisKegiatan: String
)

@Composable
internal fun ActivityDataScreen(
    user: LoginResponse,
    title: String,
    isDocumentation: Boolean,
    onBack: () -> Unit
) {
    var displayDocs by remember { mutableStateOf<List<DisplayDocItem>>(emptyList()) }
    var logs by remember { mutableStateOf<List<ActivityLogItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    val defaultFallbackPhoto = "https://images.unsplash.com/photo-1541872703-74c5e44368f9?auto=format&fit=crop&w=800&q=80"

    LaunchedEffect(user.id, isDocumentation) {
        loading = true
        runCatching {
            if (isDocumentation) {
                val allDocs = SipkaApi.service.documentation()
                val allReports = if (user.isAdmin()) SipkaApi.service.reports() else SipkaApi.service.reports(userId = user.id)

                val items = mutableListOf<DisplayDocItem>()

                // Tampilkan foto dari laporan-laporan yang telah dibuat & disetujui
                allReports.forEach { rep ->
                    val matchingDoc = allDocs.firstOrNull { it.laporan_id == rep.id }
                    val photoUrl = rep.foto_url
                        ?: matchingDoc?.file_url
                        ?: defaultFallbackPhoto

                    items.add(
                        DisplayDocItem(
                            id = rep.id,
                            title = rep.judul ?: "Laporan #${rep.id}",
                            imageUrl = photoUrl,
                            lokasi = rep.lokasi ?: matchingDoc?.alamat ?: "Polres Sukoharjo",
                            tanggal = rep.tanggal_kegiatan ?: rep.created_at?.take(10) ?: "Terbaru",
                            deskripsi = rep.deskripsi ?: "Dokumentasi kegiatan anggota kepolisian.",
                            status = rep.status ?: "pending",
                            jenisKegiatan = rep.jenis_kegiatan ?: "Kegiatan Polisi"
                        )
                    )
                }

                // Tambahkan dokumen mandiri dari tabel dokumentasi jika ada yang belum tercover
                allDocs.forEach { doc ->
                    if (items.none { it.id == doc.laporan_id }) {
                        items.add(
                            DisplayDocItem(
                                id = doc.id,
                                title = "Dokumentasi Lapangan #${doc.laporan_id ?: doc.id}",
                                imageUrl = doc.file_url ?: defaultFallbackPhoto,
                                lokasi = doc.alamat ?: "Polres Sukoharjo",
                                tanggal = doc.created_at?.take(10) ?: "Terbaru",
                                deskripsi = "Lampiran dokumentasi foto resmi kegiatan kepolisian di lapangan.",
                                status = "approved",
                                jenisKegiatan = "Dokumentasi"
                            )
                        )
                    }
                }

                displayDocs = items
            } else {
                logs = SipkaApi.service.activityLogs(user.id)
            }
        }.onFailure {
            error = "Data gagal dimuat dari server."
        }.onSuccess {
            error = null
        }
        loading = false
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF8FAFC)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            ScreenHeader(title = title, onBack = onBack)
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
                isDocumentation && displayDocs.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada foto dokumentasi laporan pada akun ini.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                }
                !isDocumentation && logs.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada log aktivitas untuk akun ini.", color = Color(0xFF64748B), fontSize = 14.sp)
                    }
                }
                isDocumentation -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(displayDocs, key = { it.id }) { item ->
                            DocumentationPhotoCard(
                                item = item,
                                onPhotoClick = { previewImageUrl = item.imageUrl }
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(logs, key = { it.id }) { log ->
                            ActivityTimelineItem(log = log)
                        }
                    }
                }
            }
        }
    }

    // Modal dialog untuk melihat foto dokumentasi resolusi penuh
    if (previewImageUrl != null) {
        Dialog(onDismissRequest = { previewImageUrl = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Foto Dokumentasi Kegiatan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = previewImageUrl,
                            contentDescription = "Preview Foto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { previewImageUrl = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup")
                    }
                }
            }
        }
    }
}

/**
 * Kartu Foto Dokumentasi yang Hidup, Menarik, dan Visual
 */
@Composable
private fun DocumentationPhotoCard(
    item: DisplayDocItem,
    onPhotoClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 2.dp
    ) {
        Column {
            // Tampilan Foto Kegiatan (AsyncImage)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clickable(onClick = onPhotoClick)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Overlay Badge Kategori & Status di atas Foto
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge Kategori
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Navy.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = item.jenisKegiatan,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Badge Status Persetujuan
                    val isApproved = item.status.equals("approved", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isApproved) Color(0xFF16A34A).copy(alpha = 0.9f) else Color(0xFFD97706).copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = if (isApproved) "✓ Disetujui" else "⏳ Menunggu",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Keterangan & Deskripsi Dokumentasi
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = item.title,
                    color = Navy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                // Baris Info Lokasi & Tanggal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.lokasi,
                        color = Color(0xFF475467),
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("📅", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = item.tanggal,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.deskripsi,
                    color = Color(0xFF475467),
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Format string log aktivitas sesuai format instruksi user:
 * [30 Sep 2026, 07:00] - Zhahir Furqon berhasil Login ke aplikasi.
 */
@Composable
private fun ActivityTimelineItem(log: ActivityLogItem) {
    val formattedTime = remember(log.createdAt) { formatLogTimestamp(log.createdAt) }
    val activityText = log.aktivitas.orEmpty()

    // Tentukan icon dan style sesuai tipe aktivitas
    val icon = when {
        activityText.contains("Login Gagal", ignoreCase = true) -> "⚠️"
        activityText.contains("Login", ignoreCase = true) -> "🔑"
        activityText.contains("Logout", ignoreCase = true) -> "🚪"
        activityText.contains("Menyetujui", ignoreCase = true) -> "✅"
        activityText.contains("Menolak", ignoreCase = true) -> "❌"
        activityText.contains("Membuat Laporan", ignoreCase = true) -> "📝"
        activityText.contains("Profil", ignoreCase = true) -> "👤"
        activityText.contains("Mendaftar", ignoreCase = true) -> "📋"
        else -> "📌"
    }

    val iconBgColor = when {
        activityText.contains("Login Gagal", ignoreCase = true) -> Color(0xFFFEE4E2)
        activityText.contains("Menolak", ignoreCase = true) -> Color(0xFFFEE4E2)
        activityText.contains("Menyetujui", ignoreCase = true) -> Color(0xFFD1FADF)
        activityText.contains("Membuat Laporan", ignoreCase = true) -> Color(0xFFEFF8FF)
        activityText.contains("Profil", ignoreCase = true) -> Color(0xFFF4F3FF)
        else -> Color(0xFFF1F5F9)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Aktivitas
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Narasi Log Aktivitas
            Column(Modifier.weight(1f)) {
                // Header Waktu [30 Sep 2026, 07:00]
                Text(
                    text = formattedTime,
                    color = Color(0xFF2876A8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                // Isi Catatan Aktivitas
                Text(
                    text = activityText,
                    color = Navy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Format tanggal dari "2026-09-30 07:00:00" atau ISO string menjadi "[30 Sep 2026, 07:00]"
 */
private fun formatLogTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) return "[Baru saja]"
    return try {
        val clean = raw.replace("T", " ").substringBefore(".")
        val parts = clean.trim().split(" ")
        val datePart = parts[0]
        val timePart = if (parts.size > 1) parts[1].take(5) else "00:00"

        val dateTokens = datePart.split("-")
        if (dateTokens.size == 3) {
            val year = dateTokens[0]
            val monthNum = dateTokens[1].toIntOrNull() ?: 1
            val day = dateTokens[2].toIntOrNull() ?: 1

            val months = listOf("", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
            val monthName = months.getOrElse(monthNum) { "Sep" }
            "[$day $monthName $year, $timePart]"
        } else {
            "[$raw]"
        }
    } catch (_: Exception) {
        "[$raw]"
    }
}
