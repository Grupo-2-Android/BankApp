package com.example.bankapp.presentation.screens.crypto

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.presentation.viewmodels.CryptoUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel

@Composable
fun CryptoListScreen(
    onCryptoClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CryptoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text("Voltar")
                }

                Text(
                    text = "Cryptos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is CryptoUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
                is CryptoUiState.Success -> {
                    LazyColumn {
                        items(state.cryptos) { crypto ->
                            CryptoItemRow(
                                id = crypto.id,
                                symbol = crypto.symbol,
                                onClick = { onCryptoClick(crypto.id) }
                            )
                            HorizontalDivider(color = Color.DarkGray)
                        }
                    }
                }
                is CryptoUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
fun CryptoItemRow(id: String, symbol: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = "ID: $id", color = Color.Gray, fontSize = 14.sp)
        }
        Text(text = ">", color = Color.Gray)
    }
}
