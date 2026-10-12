package com.polres.sipka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun ForgotPasswordScreen(onBackToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var codeSent by remember { mutableStateOf(false) }
    var resetToken by remember { mutableStateOf<String?>(null) }
    var verifiedEmail by remember { mutableStateOf<String?>(null) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF2F4F7)) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE4E7EC)),
                shadowElevation = 2.dp
            ) {
                Column(Modifier.padding(20.dp)) {
                    // HEADER
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_humas),
                            contentDescription = "Logo Humas",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_polri),
                            contentDescription = "Logo Polri",
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                when {
                                    completed -> "Kata Sandi Berhasil Diperbarui"
                                    resetToken != null -> "Buat Kata Sandi Baru"
                                    else -> "Pemulihan Kata Sandi"
                                },
                                color = Navy,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "SIPKA Polres Sukoharjo",
                                color = Muted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        when {
                            completed -> "Kata sandi akun Anda telah berhasil diperbarui. Silakan kembali untuk masuk ke aplikasi."
                            resetToken != null -> "Identitas terverifikasi${if (!verifiedEmail.isNullOrBlank()) " ($verifiedEmail)" else ""}. Silakan tentukan kata sandi baru Anda."
                            codeSent -> "Kode verifikasi 6 digit telah dikirim ke email Anda. Masukkan kode tersebut untuk melanjutkan."
                            else -> "Masukkan email akun Anda yang terdaftar untuk menerima kode verifikasi pemulihan kata sandi."
                        },
                        color = Muted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    if (completed) {
                        // SUCCESS STATE
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEBFDF2),
                            border = BorderStroke(1.dp, Color(0xFF73E2A7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    "✓ Kata Sandi Berhasil Disimpan!",
                                    color = Color(0xFF027A48),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Akun Anda kini dapat diakses dengan kata sandi yang baru. Klik tombol di bawah untuk kembali ke halaman login.",
                                    color = Color(0xFF027A48),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else if (resetToken == null) {
                        // STEP 1: VERIFIKASI DENGAN KODE EMAIL (OTP)
                        FieldLabel("Email Akun Terdaftar")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; message = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("nama@satuan.go.id", color = Color(0xFF9AA0A6), fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !codeSent && !loading
                        )

                        if (codeSent) {
                            Spacer(Modifier.height(12.dp))
                            FieldLabel("Kode Verifikasi (6 digit)")
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = code,
                                onValueChange = { code = it.filter(Char::isDigit).take(6); message = null },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Contoh: 123456", color = Color(0xFF9AA0A6), fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(4.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = !loading
                            )

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(
                                    onClick = {
                                        codeSent = false
                                        code = ""
                                        message = null
                                    },
                                    enabled = !loading
                                ) {
                                    Text("Ganti email / kirim ulang", color = Color(0xFF2F69B3), fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                when {
                                    !codeSent && email.isBlank() -> {
                                        message = "Email wajib diisi."
                                        isSuccessMessage = false
                                    }
                                    codeSent && code.length != 6 -> {
                                        message = "Masukkan 6 digit kode verifikasi."
                                        isSuccessMessage = false
                                    }
                                    else -> {
                                        loading = true
                                        message = null
                                        scope.launch {
                                            try {
                                                if (codeSent) {
                                                    val (isSuccess, resetRes, errorMsg) = withContext(Dispatchers.IO) {
                                                        try {
                                                            val response = SipkaApi.service.verifyPasswordReset(
                                                                VerifyResetCodeRequest(email.trim(), code)
                                                            )
                                                            if (response.isSuccessful) {
                                                                Triple(true, response.body(), null)
                                                            } else {
                                                                Triple(false, null, "Kode salah atau sudah kedaluwarsa.")
                                                            }
                                                        } catch (e: Exception) {
                                                            Triple(false, null, "Tidak dapat terhubung ke server backend.")
                                                        }
                                                    }
                                                    if (isSuccess && resetRes != null) {
                                                        resetToken = resetRes.resetToken
                                                        verifiedEmail = email.trim()
                                                        message = null
                                                    } else {
                                                        message = errorMsg ?: "Verifikasi gagal."
                                                        isSuccessMessage = false
                                                    }
                                                } else {
                                                    val (isSuccess, errorMsg) = withContext(Dispatchers.IO) {
                                                        try {
                                                            val response = SipkaApi.service.requestPasswordReset(
                                                                ForgotPasswordRequest(email.trim())
                                                            )
                                                            if (response.isSuccessful) {
                                                                Pair(true, null)
                                                            } else {
                                                                Pair(false, "Permintaan belum dapat diproses. Coba lagi nanti.")
                                                            }
                                                        } catch (e: Exception) {
                                                            Pair(false, "Tidak dapat terhubung ke server backend.")
                                                        }
                                                    }
                                                    if (isSuccess) {
                                                        codeSent = true
                                                        message = "Jika email terdaftar, kode verifikasi telah dikirimkan."
                                                        isSuccessMessage = true
                                                    } else {
                                                        message = errorMsg ?: "Permintaan gagal."
                                                        isSuccessMessage = false
                                                    }
                                                }
                                            } finally {
                                                loading = false
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(
                                    if (codeSent) "Verifikasi Kode OTP" else "Kirim Kode Verifikasi",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        // STEP 2: USER MEMASUKKAN KATA SANDI BARU
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFEFF8FF),
                            border = BorderStroke(1.dp, Color(0xFFB2DDFF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "✓ Akun Terverifikasi: ${verifiedEmail.orEmpty()}",
                                    color = Color(0xFF175CD3),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        FieldLabel("Kata Sandi Baru (minimal 8 karakter)")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it; message = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Masukkan kata sandi baru", color = Color(0xFF9AA0A6), fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Text(if (passwordVisible) "Sembunyi" else "Lihat", color = Muted, fontSize = 11.sp)
                                }
                            }
                        )

                        Spacer(Modifier.height(12.dp))
                        FieldLabel("Ulangi Kata Sandi Baru")
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; message = null },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ketik ulang kata sandi baru", color = Color(0xFF9AA0A6), fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(4.dp),
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Text(if (confirmPasswordVisible) "Sembunyi" else "Lihat", color = Muted, fontSize = 11.sp)
                                }
                            }
                        )

                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = {
                                when {
                                    newPassword.length < 8 -> {
                                        message = "Kata sandi baru minimal 8 karakter."
                                        isSuccessMessage = false
                                    }
                                    newPassword != confirmPassword -> {
                                        message = "Konfirmasi kata sandi belum sama."
                                        isSuccessMessage = false
                                    }
                                    else -> {
                                        loading = true
                                        message = null
                                        scope.launch {
                                            try {
                                                val (isSuccess, errorMsg) = withContext(Dispatchers.IO) {
                                                    try {
                                                        val response = SipkaApi.service.completePasswordReset(
                                                            ResetPasswordRequest(resetToken!!, newPassword)
                                                        )
                                                        if (response.isSuccessful) {
                                                            Pair(true, null)
                                                        } else {
                                                            Pair(false, "Sesi pemulihan tidak valid atau sudah kedaluwarsa. Silakan ulangi proses.")
                                                        }
                                                    } catch (e: Exception) {
                                                        Pair(false, "Tidak dapat terhubung ke server backend.")
                                                    }
                                                }
                                                if (isSuccess) {
                                                    completed = true
                                                    message = null
                                                } else {
                                                    message = errorMsg ?: "Gagal memperbarui kata sandi."
                                                    isSuccessMessage = false
                                                }
                                            } finally {
                                                loading = false
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Navy)
                        ) {
                            if (loading) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Simpan & Ubah Kata Sandi Baru", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }

                    if (message != null) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            message!!,
                            color = if (isSuccessMessage) Color(0xFF16804A) else Color(0xFFB42318),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            if (completed) "Masuk Sekarang" else "Kembali ke Halaman Masuk",
                            color = Color(0xFF2F69B3),
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
