package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.presentation.screens.*
import com.example.bankapp.presentation.screens.portfolio.MyCryptosListScreen
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.*

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            BankAppTheme {

                val navController = rememberNavController()
                val context = LocalContext.current

                val userPreferences = remember {
                    UserPreferences(context)
                }

                val database = remember {
                    AppDatabase.getDatabase(context)
                }

                val factory = remember {
                    ViewModelFactory(userPreferences, database)
                }

                val cryptoViewModel: CryptoViewModel =
                    viewModel(factory = factory)

                val portfolioViewModel: MyPortfolioViewModel =
                    viewModel(factory = factory)

                // Coleta o nome do usuário para passar para as telas
                val userName by portfolioViewModel.userName.collectAsState()

                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "login",
                            modifier = Modifier.fillMaxSize()
                        ) {

                            composable("login") {
                                LoginScreen(
                                    viewModel = viewModel(factory = factory),
                                    onLoginSuccess = { _ ->
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel(factory = factory),
                                    onNavigateToCryptos = { navController.navigate("crypto_list") },
                                    onNavigateToMyCryptos = { navController.navigate("my_cryptos") },
                                    onNavigateToHistory = { navController.navigate("transaction_history") },
                                    onLogout = {
                                        navController.navigate("login") { popUpTo(0) }
                                    }
                                )
                            }

                            composable("crypto_list") {
                                CryptoListScreen(
                                    viewModel = cryptoViewModel,
                                    userName = userName,
                                    onCryptoClick = { cryptoId ->
                                        navController.navigate("crypto_detail/$cryptoId")
                                    },
                                    onBack = {
                                        navController.popBackStack()
                                    },
                                    onNavigateToMyCryptos = {
                                        navController.navigate("my_cryptos") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToHistory = {
                                        navController.navigate("transaction_history")
                                    },
                                    onLogout = {
                                        navController.navigate("login") { popUpTo(0) }
                                    }
                                )
                            }

                            composable("crypto_detail/{cryptoId}") { backStackEntry ->
                                val cryptoId = backStackEntry.arguments?.getString("cryptoId") ?: ""
                                CryptoDetailScreen(
                                    cryptoId = cryptoId,
                                    viewModel = cryptoViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("my_cryptos") {
                                MyCryptosListScreen(
                                    viewModel = portfolioViewModel,
                                    onNavigateToCryptos = {
                                        navController.navigate("crypto_list") {
                                            popUpTo("dashboard") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    onNavigateToHistory = {
                                        navController.navigate("transaction_history")
                                    },
                                    onLogout = {
                                        navController.navigate("login") { popUpTo(0) }
                                    }
                                )
                            }

                            composable("transaction_history") {
                                TransactionHistoryScreen(
                                    viewModel = viewModel(factory = factory),
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter).padding(bottom = 80.dp)
                        )
                    }
                }
            }
        }
    }
}
