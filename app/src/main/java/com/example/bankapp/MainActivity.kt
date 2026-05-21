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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.data.models.CryptoModel
import com.example.bankapp.data.models.UserLoginResponse
import com.example.bankapp.presentation.screens.*
import com.example.bankapp.presentation.screens.portfolio.*
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.LoginViewModel
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.SaleEvent
import com.example.bankapp.presentation.viewmodels.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BankAppTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                
                var currentUser by remember { mutableStateOf<UserLoginResponse?>(null) }
                var selectedCrypto by remember { mutableStateOf<CryptoModel?>(null) }
                var quantityInput by remember { mutableDoubleStateOf(0.0) }

                val userPreferences = remember { UserPreferences(context) }
                val database = remember { AppDatabase.getDatabase(context) }
                val factory = remember { ViewModelFactory(userPreferences, database) }

                val loginViewModel: LoginViewModel = viewModel(factory = factory)
                val cryptoViewModel: CryptoViewModel = viewModel(factory = factory)
                val portfolioViewModel: MyPortfolioViewModel = viewModel(factory = factory)
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(Unit) {
                    portfolioViewModel.saleEvents.collect { event ->
                        if (event is SaleEvent.Success) {
                            navController.navigate("my_cryptos") {
                                popUpTo("my_cryptos") { inclusive = true }
                            }
                            snackbarHostState.showSnackbar(message = "Venda realizada com sucesso!")
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavHost(navController = navController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(
                                    viewModel = loginViewModel,
                                    onLoginSuccess = { response ->
                                        currentUser = response
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
                                    onNavigateToMyCryptos = { navController.navigate("my_cryptos") }
                                )
                            }

                            composable("crypto_list") {
                                // Corrigido: Agora passa o cryptoViewModel
                                CryptoListScreen(
                                    viewModel = cryptoViewModel,
                                    onSelect = { crypto ->
                                        selectedCrypto = crypto
                                        navController.navigate("crypto_detail/${crypto.symbol}")
                                    }
                                )
                            }

                            composable("crypto_detail/{symbol}") { backStackEntry ->
                                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                                CryptoDetailScreen(
                                    cryptoId = symbol,
                                    viewModel = cryptoViewModel,
                                    onBack = { navController.popBackStack() },
                                    onBuy = { crypto ->
                                        selectedCrypto = crypto
                                        navController.navigate("buy_quantity/${crypto.symbol}")
                                    }
                                )
                            }

                            composable("buy_quantity/{symbol}") { backStackEntry ->
                                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                                QuantityScreen(
                                    symbol = symbol,
                                    onConfirm = { qty ->
                                        quantityInput = qty
                                        navController.navigate("buy_checkout")
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("buy_checkout") {
                                CheckoutScreen(
                                    crypto = selectedCrypto,
                                    quantity = quantityInput,
                                    viewModel = cryptoViewModel,
                                    onSuccess = { 
                                        navController.navigate("dashboard") {
                                            popUpTo("dashboard") { inclusive = true }
                                        }
                                    },
                                    onCancel = { navController.popBackStack() }
                                )
                            }

                            composable("my_cryptos") {
                                MyCryptosListScreen(
                                    viewModel = portfolioViewModel,
                                    onBack = { navController.navigate("dashboard") },
                                    onCryptoClick = { cryptoId ->
                                        portfolioViewModel.selectCryptoForSale(cryptoId)
                                        navController.navigate("my_crypto_detail")
                                    }
                                )
                            }
                            composable("my_crypto_detail") {
                                MyCryptoDetailScreen(
                                    viewModel = portfolioViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSellQuantity = { navController.navigate("sell_quantity") }
                                )
                            }
                            composable("sell_quantity") {
                                SellQuantityScreen(
                                    viewModel = portfolioViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToCheckout = { navController.navigate("sell_checkout") }
                                )
                            }
                            composable("sell_checkout") {
                                SellCheckoutScreen(
                                    viewModel = portfolioViewModel,
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
