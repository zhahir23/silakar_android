package com.polres.sipka

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.GET

data class LoginRequest(val email: String, val password: String)
data class GoogleLoginRequest(val idToken: String)
data class ForgotPasswordRequest(val email: String)
data class VerifyResetCodeRequest(val email: String, val code: String)
data class VerifyResetCodeResponse(val resetToken: String)
data class GoogleResetPasswordResponse(val resetToken: String, val email: String?)
data class ResetPasswordRequest(val resetToken: String, val newPassword: String)
data class ApiMessageResponse(val message: String?)
data class LoginResponse(
    val id: Long,
    val namaLengkap: String?,
    val email: String,
    val role: String?,
    @com.google.gson.annotations.SerializedName(value = "nrp", alternate = ["nip"])
    val nrp: String? = null,
    val fotoProfil: String? = null,
    val statusAnggota: String? = null,
    val kepalaDinas: String? = null
)
data class RegisterRequest(
    val nama_lengkap: String,
    val email: String,
    val password: String,
    val role: String,
    val atasan_id: Long?,
    @com.google.gson.annotations.SerializedName(value = "nrp", alternate = ["nip"])
    val nrp: String? = null
)
data class UpdateProfileRequest(
    val namaLengkap: String? = null,
    @com.google.gson.annotations.SerializedName(value = "nrp", alternate = ["nip"])
    val nrp: String? = null,
    val fotoProfil: String? = null,
    val statusAnggota: String? = null,
    val kepalaDinas: String? = null
)
data class SupervisorOption(
    val id: Long,
    val namaLengkap: String?,
    val role: String?
) {
    val label: String
        get() = if (!role.isNullOrBlank()) "${namaLengkap ?: "Anggota #$id"} ($role)" else (namaLengkap ?: "Anggota #$id")
}
data class RegistrationOptions(val roles: List<String>, val supervisors: List<SupervisorOption>)
data class ReportSummary(
    val id: Long,
    val judul: String?,
    val deskripsi: String?,
    val status: String?,
    val createdAt: String?,
    val jenisKegiatan: String?
)
data class DashboardSummary(
    val userId: Long,
    val namaLengkap: String?,
    val role: String?,
    val totalLaporan: Long,
    val menunggu: Long,
    val disetujui: Long,
    val laporanTerbaru: List<ReportSummary>
)
data class ReportItem(
    val id: Long,
    val user_id: Long?,
    val judul: String?,
    val deskripsi: String?,
    val status: String?,
    val jenis_kegiatan: String?,
    val lokasi: String?,
    val tanggal_kegiatan: String?,
    val created_at: String?,
    val catatan_atasan: String?,
    val foto_url: String? = null
)
data class UpdateReportRequest(
    val judul: String? = null,
    val deskripsi: String? = null,
    val status: String? = null,
    val jenis_kegiatan: String? = null,
    val lokasi: String? = null,
    val tanggal_kegiatan: String? = null,
    val catatan_atasan: String? = null,
    val foto_url: String? = null
)
data class CreateReportRequest(
    val user_id: Long,
    val judul: String,
    val deskripsi: String,
    val jenis_kegiatan: String,
    val lokasi: String,
    val tanggal_kegiatan: String,
    val foto_url: String? = null
)
data class NotificationItem(
    val id: Long,
    val user_id: Long?,
    val judul: String?,
    val pesan: String?,
    val is_read: String?,
    val created_at: String?
)
data class DocumentationItem(
    val id: Long,
    val laporan_id: Long?,
    val file_url: String?,
    val alamat: String?,
    val latitude: Double?,
    val longitude: Double?,
    val created_at: String?
)
data class ActivityLogItem(
    val id: Long,
    val user_id: Long?,
    val aktivitas: String?,
    val createdAt: String?
)

interface AuthApi {
    @POST("api/auth/password-reset/request")
    suspend fun requestPasswordReset(@Body request: ForgotPasswordRequest): Response<ApiMessageResponse>

    @POST("api/auth/password-reset/verify")
    suspend fun verifyPasswordReset(@Body request: VerifyResetCodeRequest): Response<VerifyResetCodeResponse>

    @POST("api/auth/password-reset/google")
    suspend fun resetPasswordWithGoogle(@Body request: GoogleLoginRequest): Response<GoogleResetPasswordResponse>

    @POST("api/auth/password-reset/complete")
    suspend fun completePasswordReset(@Body request: ResetPasswordRequest): Response<ApiMessageResponse>

    @POST("api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<LoginResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Query("userId") userId: Long): Response<ApiMessageResponse>

    @GET("api/auth/options")
    suspend fun registrationOptions(): RegistrationOptions

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @PUT("api/auth/profile/{id}")
    suspend fun updateProfile(
        @Path("id") id: Long,
        @Body request: UpdateProfileRequest
    ): Response<LoginResponse>

    @GET("api/dashboard/{userId}")
    suspend fun dashboard(@Path("userId") userId: Long): DashboardSummary

    @GET("api/laporan")
    suspend fun reports(
        @Query("userId") userId: Long? = null,
        @Query("viewerId") viewerId: Long? = null
    ): List<ReportItem>

    @POST("api/laporan")
    suspend fun createReport(@Body request: CreateReportRequest): Response<ReportItem>

    @PUT("api/laporan/{id}")
    suspend fun updateReport(
        @Path("id") id: Long,
        @Body request: UpdateReportRequest,
        @Query("editorId") editorId: Long
    ): Response<ReportItem>

    @GET("api/notifikasi")
    suspend fun notifications(@Query("userId") userId: Long): List<NotificationItem>

    @GET("api/dokumentasi")
    suspend fun documentation(): List<DocumentationItem>

    @GET("api/log-aktivitas")
    suspend fun activityLogs(@Query("userId") userId: Long): List<ActivityLogItem>
}

object SipkaApi {
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    val service: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}
