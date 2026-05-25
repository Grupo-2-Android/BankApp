package com.example.bankapp.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.presentation.screens.cards.AddCardScreen
import com.example.bankapp.presentation.screens.cards.CardManagementScreen
import com.example.bankapp.presentation.screens.crypto.CryptoDetailScreen
import com.example.bankapp.presentation.screens.crypto.CryptoListScreen
import com.example.bankapp.presentation.screens.DashboardScreen
import com.example.bankapp.presentation.screens.LoginScreen
import com.example.bankapp.presentation.screens.portfolio.MyCryptoDetailScreen
import com.example.bankapp.presentation.screens.portfolio.MyCryptosListScreen
import com.example.bankapp.presentation.screens.portfolio.SellCheckoutScreen
import com.example.bankapp.presentation.screens.portfolio.SellQuantityScreen
import com.example.bankapp.presentation.viewmodels.DashboardViewModel
import com.example.bankapp.presentation.viewmodels.cards.CardManagementViewModel
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.SaleEvent
import com.example.bankapp.presentation.viewmodels.ViewModelFactory

@Composable
fun AppNavigation(snackbarHostState: SnackbarHostState) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    val factory = remember { ViewModelFactory(userPreferences, database) }

    val cryptoViewModel: CryptoViewModel = viewModel(factory = factory)
    val portfolioViewModel: MyPortfolioViewModel = viewModel()
    val cardManagementViewModel: CardManagementViewModel = viewModel(factory = factory)

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

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = viewModel(factory = factory),
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("dashboard") {
            val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
            DashboardScreen(
                viewModel = dashboardViewModel,
                navController = navController,
                onLogout = {
                    dashboardViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("crypto_list") {
            CryptoListScreen(
                viewModel = cryptoViewModel,
                onCryptoClick = { cryptoId -> navController.navigate("crypto_detail/$cryptoId") }
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

        // Minhas Cryptos Flow
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

        composable("cards") {
            CardManagementScreen(
                snackbarHostState = snackbarHostState,
                viewModel = cardManagementViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToAddCard = { type ->
                    navController.navigate("cards/add/$type")
                }
            )
        }

        composable("cards/add/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "VIRTUAL"
            AddCardScreen(
                viewModel = cardManagementViewModel,
                cardType = type,
                onBack = { navController.popBackStack() },
                onConfirmSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}