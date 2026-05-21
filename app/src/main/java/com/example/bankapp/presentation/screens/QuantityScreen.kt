package com.example.bankapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.BankButton

@Composable
fun QuantityScreen(symbol: String, onConfirm: (Double) -> Unit, onBack: () -> Unit) {
    var qtyText by remember { mutableStateOf("") }
    val context = LocalContext.current

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
                text = "Quanto de $symbol deseja comprar?", 
                color = Color.White, 
                fontWeight = FontWeight.Bold, 
                fontSize = 20.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = qtyText,
                onValueChange = { 
                    // Permite apenas números e um ponto/vírgula
                    if (it.all { char -> char.isDigit() || char == '.' || char == ',' }) {
                        qtyText = it 
                    }
                },
                label = { Text("Quantidade", color = Color.Gray) },
                placeholder = { Text("Ex: 0.5", color = Color.DarkGray) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            BankButton("Continuar", { 
                // Converte vírgula para ponto antes de transformar em Double
                val normalizedQty = qtyText.replace(",", ".").toDoubleOrNull()
                
                if (normalizedQty != null && normalizedQty > 0) {
                    onConfirm(normalizedQty)
                } else {
                    Toast.makeText(context, "Digite uma quantidade válida", Toast.LENGTH_SHORT).show()
                }
            })
            
            TextButton(onClick = onBack) { 
                Text("Cancelar", color = Color.Gray) 
            }
        }
    }
}
