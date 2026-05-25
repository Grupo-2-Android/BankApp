package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.presentation.AppNavigation
import com.example.bankapp.presentation.screens.CryptoDetailScreen
import com.example.bankapp.presentation.screens.CryptoListScreen
import com.example.bankapp.presentation.screens.DashboardScreen
import com.example.bankapp.presentation.screens.LoginScreen
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val snackbarHostState = remember { SnackbarHostState() }

            BankAppTheme {

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { innerPadding ->

                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {

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

                        val userName by portfolioViewModel.userName
                            .collectAsState()

                        // Estado de login persistente
                        val isLoggedIn by userPreferences.isLoggedIn
                            .collectAsState(initial = false)

                        val scope = rememberCoroutineScope()

                        val startDestination =
                            if (isLoggedIn) "dashboard" else "login"

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {

                            // LOGIN
                            composable(
                                route = "login?logoutMessage={logoutMessage}"
                            ) { backStackEntry ->

                                val logoutMessage =
                                    backStackEntry.arguments
                                        ?.getString("logoutMessage")

                                LoginScreen(
                                    viewModel = viewModel(factory = factory),
                                    logoutMessage = logoutMessage,

                                    onLoginSuccess = {

                                        navController.navigate("dashboard") {

                                            popUpTo("login") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }

                            // DASHBOARD
                            composable("dashboard") {

                                DashboardScreen(
                                    viewModel = viewModel(factory = factory),

                                    onNavigateToCryptos = {
                                        navController.navigate("crypto_list")
                                    },

                                    onNavigateToMyCryptos = {
                                        navController.navigate("my_cryptos")
                                    },

                                    onLogout = {

                                        scope.launch {
                                            userPreferences.logout()
                                        }

                                        navController.navigate(
                                            "login?logoutMessage=Logout realizado com sucesso"
                                        ) {

                                            popUpTo("dashboard") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }

                            // CRYPTOS
                            composable("crypto_list") {

                                CryptoListScreen(
                                    viewModel = cryptoViewModel,

                                    onCryptoClick = { cryptoId ->
                                        navController.navigate(
                                            "crypto_detail/$cryptoId"
                                        )
                                    }
                                )
                            }

                            // DETALHE
                            composable(
                                "crypto_detail/{cryptoId}"
                            ) { backStackEntry ->

                                val cryptoId =
                                    backStackEntry.arguments
                                        ?.getString("cryptoId") ?: ""

                                CryptoDetailScreen(
                                    cryptoId = cryptoId,
                                    viewModel = cryptoViewModel,

                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            // PORTFOLIO
                            composable("my_cryptos") {

                                Text(text = "Tela Minhas Cryptos")
                            }
                        }

                        // Mantido do merge
                        AppNavigation(
                            snackbarHostState = snackbarHostState
                        )
                    }
                }
            }
        }
    }
}