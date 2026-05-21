package com.example.bankapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.BankButton
import com.example.bankapp.data.models.CryptoModel
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import kotlinx.coroutines.launch

@Composable
fun CheckoutScreen(
    crypto: CryptoModel?, 
    quantity: Double, 
    viewModel: CryptoViewModel, 
    onSuccess: () -> Unit, 
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val unitPrice = crypto?.last?.toDoubleOrNull() ?: 0.0
    val total = unitPrice * quantity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp), 
            Arrangement.Center, 
            Alignment.CenterHorizontally
        ) {
            Text(
                "Resumo da Compra", 
                color = Color.White, 
                fontWeight = FontWeight.Bold, 
                fontSize = 24.sp
            )
            
            Spacer(Modifier.height(32.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Cripto: ${crypto?.name} (${crypto?.symbol})", color = Color.White)
                    Text("Quantidade: $quantity", color = Color.White)
                    Text("Preço Unitário: $ $unitPrice", color = Color.Gray, fontSize = 14.sp)
                    HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.DarkGray)
                    Text(
                        "Total: $ $total", 
                        color = Color(0xFF4CAF50), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 22.sp
                    )
                }
            }

            Spacer(Modifier.height(48.dp))
            
            BankButton("Confirmar Pagamento", {
                if (crypto != null) {
                    scope.launch {
                        val success = viewModel.buyCrypto(
                            cryptoId = crypto.symbol,
                            symbol = crypto.symbol,
                            amount = quantity,
                            price = unitPrice
                        )
                        
                        if (success) {
                            Toast.makeText(context, "Compra realizada com sucesso!", Toast.LENGTH_LONG).show()
                            onSuccess()
                        } else {
                            Toast.makeText(context, "Saldo Insuficiente!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            })
            
            TextButton(onClick = onCancel) { 
                Text("Cancelar", color = Color.Gray) 
            }
        }
    }
}
