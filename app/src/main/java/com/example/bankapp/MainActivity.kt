package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.presentation.screens.CryptoDetailScreen
import com.example.bankapp.presentation.screens.CryptoListScreen
import com.example.bankapp.presentation.screens.DashboardScreen
import com.example.bankapp.presentation.screens.LoginScreen
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import androidx.compose.material3.*
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.ViewModelFactory

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
                    viewModel()

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {

                        // LOGIN com mensagem de logout
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
                                    navController.navigate("crypto_detail/$cryptoId")
                                }
                            )
                        }

                        // DETALHE
                        composable("crypto_detail/{cryptoId}") { backStackEntry ->

                            val cryptoId =
                                backStackEntry.arguments?.getString("cryptoId") ?: ""

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
                }
            }
        }
    }
}