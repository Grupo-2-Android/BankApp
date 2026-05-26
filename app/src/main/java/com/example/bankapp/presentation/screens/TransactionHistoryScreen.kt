package com.example.bankapp.presentation.screens

import androidx.compose.foundation.background
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
        topBar = {
            TopAppBar(
                title = { Text("Meu Extrato", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma movimentação encontrada", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                    HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val isNegative = transaction.operation == "BUY"
    val color = if (isNegative) Color.Red else Color(0xFF4CAF50)
    val prefix = if (isNegative) "-" else "+"

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
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = formatDate(transaction.date),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (transaction.operation == "BUY") "COMPRA" else "VENDA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Text(
            text = "$prefix R$ ${String.format(Locale("pt", "BR"), "%,.2f", transaction.amount)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
