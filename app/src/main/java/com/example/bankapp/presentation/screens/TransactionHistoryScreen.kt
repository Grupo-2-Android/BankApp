package com.example.bankapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.bankapp.data.local.room.entities.Transaction
import com.example.bankapp.presentation.viewmodels.TransactionHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionHistoryViewModel,
    onBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Histórico de Transações", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma transação encontrada", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val ptBr = Locale("pt", "BR")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDate(transaction.date),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "Operação: ${translateOperation(transaction.operation)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text(
            text = if (transaction.operation == "BUY") "-R$ ${String.format(ptBr, "%,.2f", transaction.amount)}" 
                   else "R$ ${String.format(ptBr, "%,.2f", transaction.amount)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (transaction.operation == "BUY") Color(0xFFF44336) else Color(0xFF4CAF50)
        )
    }
}

private fun translateOperation(operation: String): String {
    return when (operation.uppercase()) {
        "BUY" -> "COMPRA"
        "SELL" -> "VENDA"
        "INITIAL" -> "SALDO INICIAL"
        else -> operation
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("pt", "BR"))
    return sdf.format(Date(timestamp))
}
