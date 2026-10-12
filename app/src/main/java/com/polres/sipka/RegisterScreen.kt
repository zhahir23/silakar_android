package com.polres.sipka

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
internal fun RegisterScreen(onBackToLogin: () -> Unit, onRegistered: (String) -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var nrp by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var selectedSupervisor by remember { mutableStateOf<SupervisorOption?>(null) }
    var options by remember { mutableStateOf(RegistrationOptions(listOf("Anggota"), emptyList())) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        runCatching {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                SipkaApi.service.registrationOptions()
            }
        }.onSuccess { loaded ->
            options = loaded
            if (selectedRole.isBlank()) selectedRole = loaded.roles.firstOrNull().orEmpty()
        }
    }

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF2F4F7)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE4E7EC)),
                shadowElevation = 2.dp
            ) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_humas),
                            contentDescription = "Logo Humas",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_polri),
                            contentDescription = "Logo Polri",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("SIPKA", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Daftar akun baru", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Silakan lengkapi data diri Anda untuk mendaftar.", color = Muted, fontSize = 12.sp)

                    Spacer(Modifier.height(16.dp))
                    FieldLabel("Nama Lengkap")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fullName, onValueChange = { fullName = it; message = null },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("Masukkan nama lengkap", fontSize = 14.sp, color = Color(0xFF9AA0A6)) },
                        shape = RoundedCornerShape(3.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    FieldLabel("NRP (Nomor Registrasi Pokok)")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = nrp,
                        onValueChange = { nrp = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Masukkan NRP anda", fontSize = 14.sp, color = Color(0xFF9AA0A6)) },
                        shape = RoundedCornerShape(3.dp)
                    )

                    Spacer(Modifier.height(10.dp))
                    FieldLabel("Email")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it; message = null },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("nama@satuan.go.id", fontSize = 14.sp, color = Color(0xFF9AA0A6)) },
                        shape = RoundedCornerShape(3.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(Modifier.height(10.dp))
                    FieldLabel("Role / Jabatan")
                    Spacer(Modifier.height(4.dp))
                    DropdownField(selectedRole.ifBlank { "Pilih Role" }, options.roles) { selectedRole = it }

                    Spacer(Modifier.height(10.dp))
                    FieldLabel("Pilih Atasan")
                    Spacer(Modifier.height(4.dp))
                    DropdownField(
                        selectedSupervisor?.label ?: "Pilih Atasan (Opsional)",
                        listOf("Tanpa Atasan") + options.supervisors.map { it.label }
                    ) { selected ->
                        selectedSupervisor = options.supervisors.firstOrNull { it.label == selected }
                    }

                    Spacer(Modifier.height(10.dp))
                    FieldLabel("Kata sandi")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = password, onValueChange = { password = it; message = null },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("Minimal 8 karakter", fontSize = 14.sp, color = Color(0xFF9AA0A6)) },
                        shape = RoundedCornerShape(3.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(Modifier.height(10.dp))
                    FieldLabel("Konfirmasi Kata sandi")
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it; message = null },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        placeholder = { Text("Ulangi kata sandi", fontSize = 14.sp, color = Color(0xFF9AA0A6)) },
                        shape = RoundedCornerShape(3.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val effectiveRole = selectedRole.ifBlank { options.roles.firstOrNull() ?: "anggota" }
                            when {
                                fullName.isBlank() || email.isBlank() || password.isBlank() -> message = "Lengkapi semua data yang wajib diisi."
                                !email.contains('@') -> message = "Format email belum benar."
                                password.length < 8 -> message = "Kata sandi minimal 8 karakter."
                                password != confirmPassword -> message = "Konfirmasi kata sandi belum sama."
                                else -> {
                                    loading = true
                                    message = null
                                    scope.launch {
                                        message = try {
                                            val result = SipkaApi.service.register(
                                                RegisterRequest(
                                                    nama_lengkap = fullName.trim(),
                                                    email = email.trim(),
                                                    password = password,
                                                    role = effectiveRole,
                                                    atasan_id = selectedSupervisor?.id,
                                                    nrp = nrp.trim().ifBlank { null }
                                                )
                                            )
                                            if (result.isSuccessful) {
                                                onRegistered("Pendaftaran berhasil. Silakan masuk.")
                                                null
                                            } else if (result.code() == 409) "Email sudah terdaftar."
                                            else "Pendaftaran gagal. Periksa kembali data Anda."
                                        } catch (_: Exception) {
                                            "Tidak dapat terhubung ke server. Periksa koneksi backend."
                                        }
                                        loading = false
                                    }
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Daftar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (message != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(message!!, color = Color(0xFFB42318), fontSize = 12.sp)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("Sudah punya akun?", color = Muted, fontSize = 12.sp)
                        TextButton(onClick = onBackToLogin) { Text("Masuk", color = Color(0xFF2F69B3), fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownField(value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { if (options.isNotEmpty()) expanded = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(3.dp),
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = if (value.startsWith("Pilih")) Muted else Navy, fontSize = 14.sp)
                Text("v", color = Muted)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
        }
    }
}

@Composable
fun FieldLabel(text: String) {
    Text(text, color = Navy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
}
