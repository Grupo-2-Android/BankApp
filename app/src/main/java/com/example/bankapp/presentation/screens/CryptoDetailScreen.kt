package com.example.bankapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.presentation.viewmodels.CryptoViewModel

@Composable
fun CryptoDetailScreen(
    cryptoId: String,
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val crypto = viewModel.getCryptoById(cryptoId)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Voltar")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (crypto != null) {
                Text(
                    text = "Detalhes: ${crypto.symbol}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailItem(label = "ID", value = crypto.id)
                DetailItem(label = "Símbolo", value = crypto.symbol)
                DetailItem(label = "Fonte", value = crypto.source)
                DetailItem(label = "OHLC disponível desde", value = crypto.ohlc_available_from)
                DetailItem(label = "Histórico disponível desde", value = crypto.history_available_from)
            } else {
                Text(text = "Crypto não encontrada", color = Color.Red)
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
