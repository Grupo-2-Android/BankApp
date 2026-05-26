package com.example.bankapp.presentation.screens.crypto

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.presentation.viewmodels.BuyUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import java.util.Locale

@Composable
fun BuyQuantityScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
    val buyState by viewModel.buyState.collectAsState()
    var quantityText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(buyState) {
        if (buyState is BuyUiState.QuantityInputed) {
            onNavigateToCheckout()
        }
    }

    val crypto = when (val state = buyState) {
        is BuyUiState.CryptoSelected -> state.crypto
        is BuyUiState.QuantityInputed -> state.crypto
        else -> {
            onBack()
            return
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Voltar")
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Quanto deseja comprar de ${crypto.symbol}?",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Input de quantidade
            OutlinedTextField(
                value = quantityText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d*$"""))) {
                        quantityText = it
                        errorMessage = null
                    }
                },
                label = { Text("Quantidade", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red
                )
            )

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull()
                    if (qty == null || qty <= 0) {
                        errorMessage = "Digite uma quantidade válida"
                    } else {
                        viewModel.setBuyAmount(qty)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continuar para Resumo", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
