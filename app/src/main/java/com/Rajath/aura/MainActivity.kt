package com.Rajath.aura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.Rajath.aura.auth.AuthViewModel
import com.Rajath.aura.auth.AuthStateViewModel
import com.Rajath.aura.nav.NavRoutes
import com.Rajath.aura.ui.HomeScreen
import com.Rajath.aura.ui.JournalComposeScreen
import com.Rajath.aura.ui.JournalListScreen
import com.Rajath.aura.ui.AnalyticsScreen
import com.Rajath.aura.ui.MeditateScreen
import com.Rajath.aura.ui.FullscreenPlayerScreen
import com.Rajath.aura.ui.LoginScreen
import com.Rajath.aura.ui.NameEntryScreen
import com.Rajath.aura.ui.theme.AURATheme
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppEntry()
        }
    }
}

@Composable
fun AppEntry() {
    AURATheme {
        Surface(modifier = Modifier) {
            val navController = rememberNavController()
            val authViewModel: AuthViewModel = viewModel()
            val authStateVm: AuthStateViewModel = viewModel()
            val currentUid by authStateVm.uid.collectAsState()

            // Always start on Auth to avoid premature Home flash.
            val startDestination = NavRoutes.Auth.route

            // React to auth changes and decide destination AFTER reloading profile.
            LaunchedEffect(currentUid) {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()

                // If no user, ensure we're on Auth
                if (currentUid == null) {
                    if (navController.currentDestination?.route != NavRoutes.Auth.route) {
                        navController.navigate(NavRoutes.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                    return@LaunchedEffect
                }

                // We have a uid — reload user so displayName is up-to-date
                val user = auth.currentUser
                if (user != null) {
                    user.reload().addOnCompleteListener { _ ->
                        val displayName = auth.currentUser?.displayName
                        val targetRoute = if (displayName.isNullOrBlank()) NavRoutes.NameEntry.route else NavRoutes.Home.route

                        // Only navigate if needed
                        if (navController.currentDestination?.route != targetRoute) {
                            navController.navigate(targetRoute) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        }
                    }
                } else {
                    // defensive fallback to auth route
                    if (navController.currentDestination?.route != NavRoutes.Auth.route) {
                        navController.navigate(NavRoutes.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            }

            NavHost(navController = navController, startDestination = startDestination) {
                // Login
                composable(NavRoutes.Auth.route) {
                    LoginScreen(
                        authViewModel = authViewModel,
                        onNavigateToNameEntry = {
                            navController.navigate(NavRoutes.NameEntry.route) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        },
                        onNavigateToHome = {
                            navController.navigate(NavRoutes.Home.route) {
                                popUpTo(NavRoutes.Auth.route) { inclusive = true }
                            }
                        }
                    )
                }

                // Name entry for new users
                composable(NavRoutes.NameEntry.route) {
                    NameEntryScreen(authViewModel = authViewModel) {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                // Home
                composable(NavRoutes.Home.route) {
                    val uid = currentUid
                    if (uid == null) {
                        // If user signed out / no auth yet, redirect to login
                        LaunchedEffect(Unit) {
                            navController.navigate(NavRoutes.Auth.route) {
                                popUpTo(NavRoutes.Home.route) { inclusive = true }
                            }
                        }
                    } else {
                        HomeScreen(
                            uid = uid,
                            onOpenJournal = { navController.navigate(NavRoutes.JournalCompose.route) },
                            onOpenHistory = { navController.navigate(NavRoutes.JournalList.route) },
                            onOpenAnalytics = { navController.navigate(NavRoutes.Analytics.route) },
                            onOpenMeditate = { navController.navigate(NavRoutes.Meditate.route) },
                            modifier = Modifier
                        )
                    }
                }

                // Journal Compose
                composable(NavRoutes.JournalCompose.route) {
                    val uid = currentUid
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.navigate(NavRoutes.Auth.route) }
                    } else {
                        JournalComposeScreen(uid = uid, onBack = { navController.popBackStack() })
                    }
                }

                // Journal list / history
                composable(NavRoutes.JournalList.route) {
                    val uid = currentUid
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.navigate(NavRoutes.Auth.route) }
                    } else {
                        JournalListScreen(uid = uid, onBack = { navController.popBackStack() })
                    }
                }

                // Analytics
                composable(NavRoutes.Analytics.route) {
                    val uid = currentUid
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.navigate(NavRoutes.Auth.route) }
                    } else {
                        AnalyticsScreen(uid = uid, onBack = { navController.popBackStack() })
                    }
                }

                // Meditate
                composable(NavRoutes.Meditate.route) {
                    val uid = currentUid
                    if (uid == null) {
                        LaunchedEffect(Unit) { navController.navigate(NavRoutes.Auth.route) }
                    } else {
                        MeditateScreen(
                            navController = navController,
                            uid = uid,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                // fullscreen player (no uid required)
                composable("fullscreen_player") {
                    FullscreenPlayerScreen(navController = navController)
                }
            }
        }
    }
}