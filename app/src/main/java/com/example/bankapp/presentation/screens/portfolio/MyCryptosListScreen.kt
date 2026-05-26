package com.example.bankapp.presentation.screens.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.bankapp.data.models.OwnedCrypto
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import java.util.Locale

@Composable
fun MyCryptosListScreen(viewModel: MyPortfolioViewModel, onBack: () -> Unit, onCryptoClick: (String) -> Unit) {
    val ownedCryptos by viewModel.ownedCryptosState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Text("<", color = Color.Gray, fontSize = 24.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Minhas Cryptos", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(ownedCryptos) { owned ->
                    MyCryptoItemRow(owned = owned, onClick = { onCryptoClick(owned.cryptoInfo.id) })
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun MyCryptoItemRow(owned: OwnedCrypto, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(owned.cryptoInfo.symbol, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Qtde: ${owned.quantity.toInt()}", color = Color.Gray, fontSize = 14.sp)
        }
        val totalValueBRL = owned.quantity * owned.currentPrice
        Text("R$ ${String.format(Locale.GERMANY, "%,.2f", totalValueBRL)}", color = Color.White, fontWeight = FontWeight.Medium)
    }
}