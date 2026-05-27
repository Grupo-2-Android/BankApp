package com.example.bankapp.presentation.screens.portfolio

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.R
import com.example.bankapp.presentation.theme.GreenPrimary
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.SaleUiState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCryptoDetailScreen(viewModel: MyPortfolioViewModel, onBack: () -> Unit, onNavigateToSellQuantity: () -> Unit) {
    val saleState by viewModel.saleState.collectAsState()
    if (saleState !is SaleUiState.CryptoSelected) { onBack(); return }
    val owned = (saleState as SaleUiState.CryptoSelected).ownedCrypto
    val totalValueBRL = owned.quantity * owned.currentPrice
    val formattedTotalValue = String.format(Locale.GERMANY, "%,.2f", totalValueBRL)
    val roundedButtonShape = RoundedCornerShape(12.dp)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.common_my_cryptos)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onNavigateToSellQuantity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = roundedButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.sell_action))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                stringResource(R.string.portfolio_position, owned.cryptoInfo.symbol),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailItemPortfolio(
                label = stringResource(R.string.portfolio_wallet_quantity),
                value = String.format(Locale.US, "%.3f", owned.quantity)
            )
            DetailItemPortfolio(
                label = stringResource(R.string.portfolio_estimated_value_brl),
                value = stringResource(R.string.common_currency_prefix, formattedTotalValue)
            )
        }
    }
}

@Composable
fun DetailItemPortfolio(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}