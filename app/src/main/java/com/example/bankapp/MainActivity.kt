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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankapp.presentation.screens.*
import com.example.bankapp.presentation.screens.portfolio.*
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.SaleEvent
import com.example.bankapp.presentation.theme.BankAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BankAppTheme {

                val navController = rememberNavController()
                val cryptoViewModel: CryptoViewModel = viewModel()
                val portfolioViewModel: MyPortfolioViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope() // ✅ importante

                // ✅ Eventos de venda
                LaunchedEffect(Unit) {
                    portfolioViewModel.saleEvents.collect { event ->
                        when (event) {
                            is SaleEvent.Success -> {
                                navController.navigate("my_cryptos") {
                                    popUpTo("my_cryptos") { inclusive = true }
                                }

                                snackbarHostState.showSnackbar(
                                    message = "Venda realizada com sucesso!"
                                )
                            }
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->

                    Box(modifier = Modifier.padding(innerPadding)) {

                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {

                            // ✅ LOGIN
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = { name ->
                                        navController.navigate("dashboard/$name") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            // ✅ DASHBOARD COM LOGOUT + SNACKBAR
                            composable("dashboard/{userName}") { backStackEntry ->
                                val userName =
                                    backStackEntry.arguments?.getString("userName") ?: ""

                                DashboardScreen(
                                    userName = userName,
                                    onNavigateToCryptos = {
                                        navController.navigate("crypto_list")
                                    },
                                    onNavigateToMyCryptos = {
                                        navController.navigate("my_cryptos")
                                    },
                                    onLogout = {
                                        // ✅ navegação limpa
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }

                                        // ✅ snackbar de logout
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Logout realizado com sucesso!"
                                            )
                                        }
                                    }
                                )
                            }

                            // ✅ LISTA DE CRYPTOS
                            composable("crypto_list") {
                                CryptoListScreen(
                                    viewModel = cryptoViewModel,
                                    onCryptoClick = { cryptoId ->
                                        navController.navigate("crypto_detail/$cryptoId")
                                    }
                                )
                            }

                            // ✅ DETALHE DA CRYPTO
                            composable("crypto_detail/{cryptoId}") { backStackEntry ->
                                val cryptoId =
                                    backStackEntry.arguments?.getString("cryptoId") ?: ""

                                CryptoDetailScreen(
                                    cryptoId = cryptoId,
                                    viewModel = cryptoViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            // ✅ MINHAS CRYPTOS
                            composable("my_cryptos") {
                                MyCryptosListScreen(
                                    viewModel = portfolioViewModel,
                                    onBack = { navController.popBackStack() },
                                    onCryptoClick = { cryptoId ->
                                        portfolioViewModel.selectCryptoForSale(cryptoId)
                                        navController.navigate("my_crypto_detail")
                                    }
                                )
                            }

                            // ✅ DETALHE MINHA CRYPTO
                            composable("my_crypto_detail") {
                                MyCryptoDetailScreen(
                                    viewModel = portfolioViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToSellQuantity = {
                                        navController.navigate("sell_quantity")
                                    }
                                )
                            }

                            // ✅ TELA DE QUANTIDADE
                            composable("sell_quantity") {
                                SellQuantityScreen(
                                    viewModel = portfolioViewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToCheckout = {
                                        navController.navigate("sell_checkout")
                                    }
                                )
                            }

                            // ✅ CHECKOUT
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