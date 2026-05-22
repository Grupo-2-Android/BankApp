package com.example.bankapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.presentation.viewmodels.CryptoDetailUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoDetailScreen(
    cryptoId: String,
    viewModel: CryptoViewModel,
    onBack: () -> Unit
) {
    val crypto = viewModel.getCryptoById(cryptoId)
    val detailState by viewModel.detailState.collectAsState()
    val userBalance by viewModel.userBalance.collectAsState()
    val brlAmount by viewModel.brlAmount.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(cryptoId) {
        crypto?.let {
            viewModel.fetchCryptoDetail(it.symbol)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(crypto?.symbol ?: "Detalhes", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (crypto == null) {
                Text(text = "Crypto não encontrada na lista local", color = Color.Red)
            } else {
                when (val state = detailState) {
                    is CryptoDetailUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF4CAF50))
                        }
                    }
                    is CryptoDetailUiState.Success -> {
                        val detail = state.detail
                        val lastPrice = detail.last.toDoubleOrNull() ?: 0.0
                        val amountDouble = brlAmount.toDoubleOrNull() ?: 0.0
                        val cryptoFraction = if (lastPrice > 0) amountDouble / lastPrice else 0.0
                        val isOverBalance = amountDouble > userBalance

                        Text(
                            text = "Preço Atual: R$ ${detail.last}",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Saldo disponível: R$ ${String.format(Locale("pt", "BR"), "%,.2f", userBalance)}",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = brlAmount,
                            onValueChange = { viewModel.onBrlAmountChange(it) },
                            label = { Text("Valor em BRL") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text("R$ ", color = Color.White) },
                            isError = isOverBalance,
                            supportingText = {
                                if (isOverBalance) {
                                    Text("Saldo insuficiente", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = Color.Gray,
                                cursorColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (amountDouble > 0) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Você receberá aproximadamente:",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${String.format("%.8f", cryptoFraction)} ${detail.symbol}",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = amountDouble > 0 && !isOverBalance,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = Color.Gray
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Comprar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                containerColor = Color(0xFF1E1E1E),
                                title = { Text("Confirmar Compra", color = Color.White) },
                                text = {
                                    Column {
                                        ConfirmItem("Valor", "R$ ${String.format("%.2f", amountDouble)}")
                                        ConfirmItem("Fração", "${String.format("%.8f", cryptoFraction)} ${detail.symbol}")
                                        ConfirmItem("Taxa de rede", "R$ 0,00")
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        viewModel.buyCrypto(crypto.id, detail.symbol, cryptoFraction, amountDouble)
                                        showDialog = false
                                    }) {
                                        Text("CONFIRMAR", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("CANCELAR", color = Color.Gray)
                                    }
                                }
                            )
                        }
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
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ConfirmItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
