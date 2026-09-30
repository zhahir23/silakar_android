package com.polres.sipka

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
internal fun ReportFormScreen(
    user: LoginResponse,
    onBack: () -> Unit,
    onCreated: (ReportItem) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var categoryMenu by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Patroli", "Pengamanan", "Pelayanan Masyarakat", "Kegiatan Kepolisian", "Lainnya")

    // ==========================================
    // TASK 5: PHOTO ATTACHMENT (CAMERA & GALLERY)
    // ==========================================
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk Galeri Foto
    val pickFromGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    // Launcher untuk Membuka Kamera Bawaan HP
    val takeCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            selectedPhotoUri = tempCameraUri
        }
    }

    // Launcher Izin Akses Kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            takeCameraLauncher.launch(uri)
        } else {
            message = "Izin kamera dibutuhkan untuk mengambil foto secara langsung."
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            takeCameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // ==========================================
    // TASK 6: DETEKSI LOKASI OTOMATIS (GPS & GEOCODER)
    // ==========================================
    var isLocating by remember { mutableStateOf(false) }
    var gpsCoordinates by remember { mutableStateOf<String?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun retrieveCurrentLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            message = "Izin lokasi diperlukan untuk deteksi otomatis."
            return
        }

        isLocating = true
        message = null

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    val lat = loc.latitude
                    val lon = loc.longitude
                    gpsCoordinates = "%.6f, %.6f".format(lat, lon)

                    try {
                        val geocoder = Geocoder(context, Locale("id", "ID"))
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(lat, lon, 1) { addresses ->
                                isLocating = false
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    val fullAddress = addr.getAddressLine(0)
                                        ?: "${addr.subLocality ?: ""}, ${addr.locality ?: ""}, ${addr.subAdminArea ?: ""}".trim(',', ' ')
                                    location = fullAddress
                                } else {
                                    location = "Lat: %.6f, Lon: %.6f".format(lat, lon)
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(lat, lon, 1)
                            isLocating = false
                            if (!addresses.isNullOrEmpty()) {
                                val addr = addresses[0]
                                val fullAddress = addr.getAddressLine(0)
                                    ?: "${addr.subLocality ?: ""}, ${addr.locality ?: ""}, ${addr.subAdminArea ?: ""}".trim(',', ' ')
                                location = fullAddress
                            } else {
                                location = "Lat: %.6f, Lon: %.6f".format(lat, lon)
                            }
                        }
                    } catch (_: Exception) {
                        isLocating = false
                        location = "Lat: %.6f, Lon: %.6f".format(lat, lon)
                    }
                } else {
                    // Fallback to last known location
                    fusedLocationClient.lastLocation.addOnSuccessListener { fallbackLoc ->
                        isLocating = false
                        if (fallbackLoc != null) {
                            gpsCoordinates = "%.6f, %.6f".format(fallbackLoc.latitude, fallbackLoc.longitude)
                            location = "Lat: %.6f, Lon: %.6f".format(fallbackLoc.latitude, fallbackLoc.longitude)
                        } else {
                            message = "GPS tidak dapat menemukan posisi. Pastikan GPS HP aktif."
                        }
                    }.addOnFailureListener {
                        isLocating = false
                        message = "Gagal mengambil lokasi GPS."
                    }
                }
            }
            .addOnFailureListener {
                isLocating = false
                message = "Gagal menghubungi layanan lokasi perangkat."
            }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            retrieveCurrentLocation()
        } else {
            message = "Izin lokasi ditolak. Silakan ketik nama lokasi secara manual."
        }
    }

    fun requestLocation() {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            retrieveCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        ScreenHeader("Buat Laporan Baru", onBack)
        Spacer(Modifier.height(18.dp))

        ReportFieldLabel("Judul Laporan")
        OutlinedTextField(
            value = title,
            onValueChange = { title = it; message = null },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Contoh: Pengamanan Arus Lalu Lintas Pagi", fontSize = 14.sp) },
            singleLine = true,
            shape = RoundedCornerShape(6.dp)
        )
        Spacer(Modifier.height(12.dp))

        ReportFieldLabel("Jenis Kegiatan")
        Column {
            OutlinedButton(
                onClick = { categoryMenu = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Border)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category.ifBlank { "Pilih jenis kegiatan" }, color = if (category.isBlank()) Muted else Navy, fontSize = 14.sp)
                    Text("▾", color = Muted)
                }
            }
            DropdownMenu(expanded = categoryMenu, onDismissRequest = { categoryMenu = false }) {
                categories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { category = option; categoryMenu = false }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        ReportFieldLabel("Tanggal Kegiatan")
        OutlinedButton(
            onClick = {
                val today = Calendar.getInstance()
                DatePickerDialog(
                    context,
                    { _, year, month, day -> date = "%04d-%02d-%02d".format(year, month + 1, day) },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Border)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(date.ifBlank { "Pilih tanggal kegiatan" }, color = if (date.isBlank()) Muted else Navy, fontSize = 14.sp)
                Text("📅", fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(12.dp))

        // ==========================================
        // INPUT LOKASI + TOMBOL DETEKSI GPS OTOMATIS
        // ==========================================
        ReportFieldLabel("Lokasi Kegiatan")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = location,
                onValueChange = { location = it; message = null },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Nama lokasi atau alamat jalan", fontSize = 13.sp) },
                shape = RoundedCornerShape(6.dp)
            )
            Button(
                onClick = { requestLocation() },
                enabled = !isLocating,
                colors = ButtonDefaults.buttonColors(containerColor = Navy),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
            ) {
                if (isLocating) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Text("Ambil Lokasi", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Info box deteksi otomatis
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🛰️", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Deteksi Lokasi GPS Otomatis",
                        color = Color(0xFF166534),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (gpsCoordinates != null) "Koordinat GPS: $gpsCoordinates (Alamat otomatis terisi)"
                        else "Tekan tombol 'Ambil Lokasi' untuk mendeteksi koordinat GPS & alamat jalan saat ini secara otomatis.",
                        color = Color(0xFF15803D),
                        fontSize = 11.sp
                    )
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        ReportFieldLabel("Deskripsi / Narasi Kegiatan")
        OutlinedTextField(
            value = description,
            onValueChange = { description = it; message = null },
            modifier = Modifier.fillMaxWidth().height(130.dp),
            placeholder = { Text("Tuliskan detail laporan, hambatan, serta hasil kegiatan di lapangan secara rinci…", fontSize = 13.sp) },
            shape = RoundedCornerShape(6.dp)
        )
        Spacer(Modifier.height(14.dp))

        // ==========================================
        // TASK 5: LAMPIRAN FOTO (KAMERA & GALERI)
        // ==========================================
        ReportFieldLabel("Lampiran Foto Dokumentasi")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tombol 1: Kamera
            OutlinedButton(
                onClick = { launchCamera() },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Navy),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📷", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("Kamera", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Tombol 2: Galeri
            OutlinedButton(
                onClick = {
                    pickFromGalleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Navy),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🖼️", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text("Galeri", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Preview Foto (Thumbnail)
        if (selectedPhotoUri != null) {
            Spacer(Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                color = Color(0xFFF8FAFC)
            ) {
                Column(Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Preview Foto Terpilih", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy)
                        Text(
                            "Hapus ✕",
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { selectedPhotoUri = null }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = selectedPhotoUri,
                        contentDescription = "Preview Lampiran Laporan",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        if (message != null) {
            Text(message!!, color = Color(0xFFB42318), fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = {
                when {
                    title.isBlank() || category.isBlank() || date.isBlank() || location.isBlank() || description.isBlank() ->
                        message = "Lengkapi judul, jenis kegiatan, tanggal, lokasi, dan deskripsi."
                    else -> {
                        saving = true
                        message = null
                        scope.launch {
                            try {
                                val response = SipkaApi.service.createReport(
                                    CreateReportRequest(
                                        user_id = user.id,
                                        judul = title.trim(),
                                        deskripsi = description.trim(),
                                        jenis_kegiatan = category,
                                        lokasi = location.trim(),
                                        tanggal_kegiatan = date,
                                        foto_url = selectedPhotoUri?.toString()
                                    )
                                )
                                val saved = response.body()
                                if (response.isSuccessful && saved != null) onCreated(saved)
                                else message = "Laporan gagal disimpan (HTTP ${response.code()})."
                            } catch (_: Exception) {
                                message = "Tidak dapat menyimpan laporan. Periksa koneksi backend."
                            } finally {
                                saving = false
                            }
                        }
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy)
        ) {
            if (saving) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else Text("Simpan Laporan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("sipka_camera_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}

@Composable
private fun ReportFieldLabel(text: String) {
    Text(text, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(5.dp))
}
