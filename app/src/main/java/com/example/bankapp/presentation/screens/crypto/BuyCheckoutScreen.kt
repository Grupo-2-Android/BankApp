package com.example.bankapp.presentation.screens.crypto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.bankapp.presentation.screens.portfolio.SummaryRow
import com.example.bankapp.presentation.viewmodels.BuyUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import java.util.Locale

@Composable
fun BuyCheckoutScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val buyState by viewModel.buyState.collectAsState()

    if (buyState !is BuyUiState.QuantityInputed) {
        return
    }

    val state = buyState as BuyUiState.QuantityInputed
    val totalCostBRL = state.amount * state.price

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Cancelar")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Resumo da Compra",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SummaryRow(label = "Ativo", value = state.crypto.symbol)
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                    SummaryRow(label = "Quantidade a Comprar", value = String.format(Locale.US, "%.6f", state.amount))
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                    SummaryRow(label = "Cotação Atual", value = "R$ ${String.format(Locale.GERMANY, "%,.2f", state.price)}")
                    HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total a Pagar", color = Color.Gray, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Text("R$ ${String.format(Locale.GERMANY, "%,.2f", totalCostBRL)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.confirmBuy()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Confirmar Compra", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}
