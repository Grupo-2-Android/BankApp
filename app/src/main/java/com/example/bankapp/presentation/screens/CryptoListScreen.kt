package com.example.bankapp.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.data.models.CryptoModel
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.CryptoUiState

@Composable
fun CryptoListScreen(
    viewModel: CryptoViewModel,
    onSelect: (CryptoModel) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Mercado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.height(16.dp))
            
            when (val state = uiState) {
                is CryptoUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                }
                is CryptoUiState.Success -> {
                    LazyColumn {
                        items(state.cryptos) { crypto ->
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { 
                                        onSelect(CryptoModel(crypto.symbol, crypto.name, crypto.last)) 
                                    },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                                border = BorderStroke(0.5.dp, Color.DarkGray)
                            ) {
                                Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween) {
                                    Column {
                                        Text(crypto.name ?: crypto.symbol, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(crypto.symbol, color = Color.Gray, fontSize = 12.sp)
                                    }
                                    Text("$ ${crypto.last ?: "0.0"}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is CryptoUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = state.message, color = Color.Red)
                            Button(onClick = { viewModel.fetchCryptos() }) {
                                Text("Tentar novamente")
                            }
                        }
                    }
                }
            }
        }
    }
}
