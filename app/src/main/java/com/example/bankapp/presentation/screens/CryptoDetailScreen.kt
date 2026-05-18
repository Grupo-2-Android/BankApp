package com.example.bankapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.presentation.viewmodels.CryptoDetailUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel

@Composable
fun CryptoDetailScreen(
    cryptoId: String,
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val crypto = viewModel.getCryptoById(cryptoId)
    val detailState by viewModel.detailState.collectAsState()

    LaunchedEffect(cryptoId) {
        crypto?.let {
            viewModel.fetchCryptoDetail(it.symbol)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Voltar")
                }

                Button(
                    onClick = { /* Sem operação conforme solicitado */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Comprar")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (crypto == null) {
                Text(text = "Cripto não encontrada na lista local", color = Color.Red)
            } else {
                when (val state = detailState) {
                    is CryptoDetailUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                    }
                    is CryptoDetailUiState.Success -> {
                        val detail = state.detail
                        Text(
                            text = "Detalhes: ${detail.symbol}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        DetailItem(label = "Símbolo", value = detail.symbol)
                        DetailItem(label = "Último Preço", value = detail.last)
                        DetailItem(label = "Último em BTC", value = detail.last_btc)
                        DetailItem(label = "Mínima (24h)", value = detail.lowest)
                        DetailItem(label = "Máxima (24h)", value = detail.highest)
                        DetailItem(label = "Data/Hora", value = detail.date)
                    }
                    is CryptoDetailUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = state.message, color = Color.Red)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { viewModel.fetchCryptoDetail(crypto.symbol) }) {
                                    Text("Tentar novamente")
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
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}
