package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankapp.presentation.screens.*
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.CryptoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankAppTheme {
                val navController = rememberNavController()
                val cryptoViewModel: CryptoViewModel = viewModel()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(onLoginSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                })
                            }
                            composable("dashboard") {
                                DashboardScreen(onNavigateToCryptos = {
                                    navController.navigate("crypto_list")
                                })
                            }
                            composable("crypto_list") {
                                CryptoListScreen(
                                    viewModel = cryptoViewModel,
                                    onCryptoClick = { cryptoId ->
                                        navController.navigate("crypto_detail/$cryptoId")
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
                        }
                    }
                }
            }
        }
    }
}