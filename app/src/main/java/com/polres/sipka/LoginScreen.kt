package com.polres.sipka

import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
internal fun LoginScreen(onRegister: () -> Unit, onForgotPassword: () -> Unit, notice: String? = null, onLogin: (LoginResponse) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf(notice) }
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF2F4F7)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(3.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE4E7EC)),
                shadowElevation = 2.dp
            ) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 28.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = RoundedCornerShape(2.dp),
                            border = BorderStroke(1.dp, Border),
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("❇", color = Navy, fontSize = 24.sp)
                            }
                        }
                        Spacer(Modifier.width(9.dp))
                        Text("SIPKA", color = Navy, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(22.dp))
                    Text("Masuk ke akun anggota", color = Navy, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Gunakan email satuan Anda untuk melanjutkan.", color = Muted, fontSize = 12.sp)

                    Spacer(Modifier.height(20.dp))
                    FieldLabel("Email")
                    Spacer(Modifier.height(5.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; message = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("nama@satuan.go.id", color = Color(0xFF9AA0A6), fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(3.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(Modifier.height(12.dp))
                    FieldLabel("Kata sandi")
                    Spacer(Modifier.height(5.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; message = null },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Masukkan kata sandi", color = Color(0xFF9AA0A6), fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(3.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "Sembunyi" else "Lihat", color = Muted, fontSize = 11.sp)
                            }
                        }
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onForgotPassword) {
                            Text("Lupa kata sandi?", color = Color(0xFF2F69B3), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(22.dp))
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                message = "Email dan kata sandi wajib diisi."
                            } else {
                                loading = true
                                message = null
                                scope.launch {
                                    val (isSuccess, userResponse, errorMessage) = withContext(Dispatchers.IO) {
                                        try {
                                            val response = SipkaApi.service.login(LoginRequest(email.trim(), password))
                                            if (response.isSuccessful) {
                                                val body = response.body()
                                                if (body != null) {
                                                    Triple(true, body, null)
                                                } else {
                                                    Triple(false, null, "Respons login kosong.")
                                                }
                                            } else {
                                                Triple(false, null, "Email atau kata sandi tidak sesuai.")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("SipkaAuth", "Login network error", e)
                                            Triple(false, null, "Tidak dapat terhubung ke server. Periksa koneksi backend.")
                                        }
                                    }
                                    if (isSuccess && userResponse != null) {
                                        onLogin(userResponse)
                                        message = null
                                    } else {
                                        message = errorMessage ?: "Login gagal."
                                    }
                                    loading = false
                                }
                            }
                        },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(5.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Navy)
                    ) {
                        if (loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Masuk", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (message != null) {
                        Spacer(Modifier.height(10.dp))
                        Text(message!!, color = if (message!!.startsWith("Berhasil")) Color(0xFF16804A) else Color(0xFFB42318), fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(20.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text("Belum punya akun?", color = Muted, fontSize = 12.sp)
                        TextButton(onClick = onRegister) {
                            Text("Daftar", color = Color(0xFF2F69B3), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
