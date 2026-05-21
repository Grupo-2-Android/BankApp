package com.example.bankapp.presentation.screens.portfolio

import androidx.compose.foundation.layout.*
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
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.SaleUiState
import java.util.Locale

@Composable
fun MyCryptoDetailScreen(viewModel: MyPortfolioViewModel, onBack: () -> Unit, onNavigateToSellQuantity: () -> Unit) {
    val saleState by viewModel.saleState.collectAsState()
    if (saleState !is SaleUiState.CryptoSelected) { onBack(); return }
    val owned = (saleState as SaleUiState.CryptoSelected).ownedCrypto

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) { Text("Voltar") }
                Button(onClick = onNavigateToSellQuantity, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) { Text("Vender") }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Sua posição: ${owned.cryptoInfo.symbol}", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            DetailItemPortfolio(label = "Quantidade em Carteira", value = String.format(Locale.US, "%.3f", owned.quantity))
            val totalValueBRL = owned.quantity * owned.currentPrice
            DetailItemPortfolio(label = "Valor Atual Estimado (BRL)", value = "R$ ${String.format(Locale.GERMANY, "%,.2f", totalValueBRL)}")
        }
    }
}

@Composable
fun DetailItemPortfolio(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}