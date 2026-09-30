package com.polres.sipka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var page by remember { mutableStateOf(AppPage.Login) }
            var user by remember { mutableStateOf<LoginResponse?>(null) }
            var loginNotice by remember { mutableStateOf<String?>(null) }
            var selectedReport by remember { mutableStateOf<ReportItem?>(null) }
            val scope = rememberCoroutineScope()

            if (page != AppPage.Login && page != AppPage.Home) {
                BackHandler {
                    if (page == AppPage.ReportDetail) {
                        page = if (user?.isAdmin() == true) AppPage.AdminReports else AppPage.Reports
                    } else if (page == AppPage.Register || page == AppPage.ForgotPassword) {
                        page = AppPage.Login
                    } else {
                        page = AppPage.Home
                    }
                }
            }

            MaterialTheme {
                Surface(Modifier.fillMaxSize().statusBarsPadding(), color = androidx.compose.ui.graphics.Color.White) {
                    when (page) {
                        AppPage.Login -> LoginScreen(
                            onRegister = { loginNotice = null; page = AppPage.Register },
                            onForgotPassword = { page = AppPage.ForgotPassword },
                            notice = loginNotice,
                            onLogin = { user = it; page = AppPage.Home }
                        )
                        AppPage.Register -> RegisterScreen(
                            onBackToLogin = { page = AppPage.Login },
                            onRegistered = { loginNotice = it; page = AppPage.Login }
                        )
                        AppPage.ForgotPassword -> ForgotPasswordScreen(onBackToLogin = { page = AppPage.Login })
                        AppPage.Home -> user?.let { activeUser ->
                            ProfileDashboardScreen(
                                user = activeUser,
                                onUserUpdated = { user = it },
                                onReports = { page = AppPage.Reports },
                                onAdminReports = { page = AppPage.AdminReports },
                                onDocumentation = { page = AppPage.Documentation },
                                onArchive = { page = AppPage.Archive },
                                onLogs = { page = AppPage.ActivityLogs },
                                onNotifications = { page = AppPage.Notifications },
                                onCreateReport = { page = AppPage.CreateReport },
                                onLogout = {
                                    val uid = activeUser.id
                                    scope.launch { runCatching { SipkaApi.service.logout(uid) } }
                                    user = null
                                    page = AppPage.Login
                                }
                            )
                        } ?: run { page = AppPage.Login }
                        AppPage.Reports, AppPage.AdminReports -> user?.let { activeUser ->
                            ReportListScreen(activeUser, onBack = { page = AppPage.Home }, onCreateReport = { page = AppPage.CreateReport }) {
                                selectedReport = it
                                page = AppPage.ReportDetail
                            }
                        } ?: run { page = AppPage.Login }
                        AppPage.Archive -> user?.let { activeUser ->
                            ReportListScreen(activeUser, onBack = { page = AppPage.Home }, onCreateReport = { page = AppPage.CreateReport }, archiveOnly = true) {
                                selectedReport = it
                                page = AppPage.ReportDetail
                            }
                        } ?: run { page = AppPage.Login }
                        AppPage.Documentation, AppPage.ActivityLogs -> user?.let { activeUser ->
                            ActivityDataScreen(activeUser, if (page == AppPage.Documentation) "Dokumentasi" else "Log Aktivitas", page == AppPage.Documentation) { page = AppPage.Home }
                        } ?: run { page = AppPage.Login }
                        AppPage.CreateReport -> user?.let { activeUser ->
                            ReportFormScreen(activeUser, onBack = { page = AppPage.Home }) {
                                selectedReport = it
                                page = AppPage.ReportDetail
                            }
                        } ?: run { page = AppPage.Login }
                        AppPage.ReportDetail -> selectedReport?.let { report ->
                            ReportDetailScreen(
                                report = report,
                                viewer = user,
                                onBack = { page = if (user?.isAdmin() == true) AppPage.AdminReports else AppPage.Reports },
                                onUpdated = { selectedReport = it }
                            )
                        } ?: run { page = AppPage.Reports }
                        AppPage.Notifications -> user?.let { activeUser ->
                            NotificationScreen(activeUser) { page = AppPage.Home }
                        } ?: run { page = AppPage.Login }
                    }
                }
            }
        }
    }
}

private enum class AppPage { Login, Register, ForgotPassword, Home, Reports, AdminReports, Archive, Documentation, ActivityLogs, CreateReport, ReportDetail, Notifications }
