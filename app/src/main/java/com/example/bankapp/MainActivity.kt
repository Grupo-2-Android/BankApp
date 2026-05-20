package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.presentation.screens.*
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankAppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val userPreferences = remember { UserPreferences(context) }
                val database = remember { AppDatabase.getDatabase(context) }
                val factory = remember { ViewModelFactory(userPreferences, database) }
                
                val cryptoViewModel: CryptoViewModel = viewModel(factory = factory)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(
                                    viewModel = viewModel(factory = factory),
                                    onLoginSuccess = { name ->
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel(factory = factory),
                                    onNavigateToCryptos = {
                                        navController.navigate("crypto_list")
                                    }
                                )
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