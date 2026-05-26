package com.example.bankapp.presentation.screens.crypto

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.R
import com.example.bankapp.presentation.theme.GreenPrimary
import com.example.bankapp.presentation.viewmodels.CryptoDetailUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    cryptoId: String,
    viewModel: CryptoViewModel,
    onBack: () -> Unit,
    onNavigateToBuy: () -> Unit
) {
    val crypto = viewModel.getCryptoById(cryptoId)
    val detailState by viewModel.detailState.collectAsState()

    LaunchedEffect(cryptoId) {
        crypto?.let {
            viewModel.fetchCryptoDetail(it.symbol)
        }
    }

    val roundedButtonShape = RoundedCornerShape(12.dp)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.common_cryptos)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick = {
                        val state = detailState
                        if (state is CryptoDetailUiState.Success && crypto != null) {
                            viewModel.startBuyFlow(crypto, state.detail.last.toDoubleOrNull() ?: 0.0)
                            onNavigateToBuy()
                        }
                    },
                    shape = roundedButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.crypto_buy))
                }
            }

            Spacer(modifier = Modifier.size(width = 0.dp, height = 24.dp))

            if (crypto == null) {
                Text(
                    text = stringResource(R.string.crypto_not_found),
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                when (val state = detailState) {
                    is CryptoDetailUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = GreenPrimary
                            )
                        }
                    }

                    is CryptoDetailUiState.Success -> {
                        val detail = state.detail
                        Text(
                            text = stringResource(R.string.crypto_details_title, detail.symbol),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.size(width = 0.dp, height = 16.dp))

                        DetailItem(label = stringResource(R.string.crypto_label_symbol), value = detail.symbol)
                        DetailItem(label = stringResource(R.string.crypto_label_last_price), value = detail.last)
                        DetailItem(label = stringResource(R.string.crypto_label_last_btc), value = detail.last_btc)
                        DetailItem(label = stringResource(R.string.crypto_label_low_24h), value = detail.lowest)
                        DetailItem(label = stringResource(R.string.crypto_label_high_24h), value = detail.highest)
                        DetailItem(label = stringResource(R.string.crypto_label_date_time), value = detail.date)
                    }

                    is CryptoDetailUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = state.message, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.size(width = 0.dp, height = 8.dp))
                                Button(
                                    onClick = { viewModel.fetchCryptoDetail(crypto.symbol) },
                                    shape = roundedButtonShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GreenPrimary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(stringResource(R.string.crypto_try_again))
                                }
                            }
                        }
                    }

                    is CryptoDetailUiState.Idle -> {
                        // Aguardando início do carregamento
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
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
