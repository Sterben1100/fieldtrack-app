package com.aswfoods.fieldtrack

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aswfoods.fieldtrack.ui.components.NetworkStatusBar
import com.aswfoods.fieldtrack.ui.screens.dashboard.DashboardScreen
import com.aswfoods.fieldtrack.ui.screens.dashboard.DashboardViewModel
import com.aswfoods.fieldtrack.ui.screens.leader.LeaderSurveyScreen
import com.aswfoods.fieldtrack.ui.screens.leader.LeaderSurveyViewModel
import com.aswfoods.fieldtrack.ui.screens.login.LoginScreen
import com.aswfoods.fieldtrack.ui.screens.login.LoginViewModel
import com.aswfoods.fieldtrack.ui.screens.member.MemberSurveyScreen
import com.aswfoods.fieldtrack.ui.screens.member.MemberSurveyViewModel
import com.aswfoods.fieldtrack.ui.theme.FieldTrackTheme

/**
 * Activity utama Single-Activity Architecture untuk aplikasi FieldTrack Pro.
 */
class MainActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val leaderViewModel: LeaderSurveyViewModel by viewModels()
    private val memberViewModel: MemberSurveyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FieldTrackTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    NetworkStatusBar()
                    Box(modifier = Modifier.weight(1f)) {
                        AppNavigation(
                            loginViewModel = loginViewModel,
                            dashboardViewModel = dashboardViewModel,
                            leaderViewModel = leaderViewModel,
                            memberViewModel = memberViewModel
                        )
                    }
                }
            }
        }
    }
}

/**
 * Pengelola graf navigasi Jetpack Compose antar layar Login, Dashboard, Leader Survey, dan Member Survey.
 *
 * @param loginViewModel ViewModel untuk otentikasi sesi
 * @param dashboardViewModel ViewModel untuk daftar rute dan sinkronisasi
 * @param leaderViewModel ViewModel untuk formulir kuesioner Leader
 * @param memberViewModel ViewModel untuk formulir audit fisik Member
 */
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    dashboardViewModel: DashboardViewModel,
    leaderViewModel: LeaderSurveyViewModel,
    memberViewModel: MemberSurveyViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 1. Layar Login
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { role, userName ->
                    val encodedRole = Uri.encode(role)
                    val encodedUserName = Uri.encode(userName)
                    navController.navigate("dashboard/$encodedRole/$encodedUserName") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 2. Layar Dashboard Utama
        composable(
            route = "dashboard/{role}/{userName}",
            arguments = listOf(
                navArgument("role") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val role = Uri.decode(backStackEntry.arguments?.getString("role") ?: "member")
            val userName = Uri.decode(backStackEntry.arguments?.getString("userName") ?: "Surveyor")

            DashboardScreen(
                userName = userName,
                role = role,
                viewModel = dashboardViewModel,
                onStartSurvey = { outletId ->
                    val targetRoute = if (role.lowercase() == "leader") {
                        "leader_survey/$outletId"
                    } else {
                        "member_survey/$outletId"
                    }
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    loginViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 3. Layar Form Survey Leader (L1 - L12)
        composable(
            route = "leader_survey/{outletId}",
            arguments = listOf(
                navArgument("outletId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val outletId = backStackEntry.arguments?.getLong("outletId") ?: 0L

            LeaderSurveyScreen(
                outletId = outletId,
                viewModel = leaderViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSurveySaved = {
                    navController.popBackStack()
                }
            )
        }

        // 4. Layar Form Audit Member (Kriteria 1 - 4)
        composable(
            route = "member_survey/{outletId}",
            arguments = listOf(
                navArgument("outletId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val outletId = backStackEntry.arguments?.getLong("outletId") ?: 0L

            MemberSurveyScreen(
                outletId = outletId,
                viewModel = memberViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onSurveySaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}
