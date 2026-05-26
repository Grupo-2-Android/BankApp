package com.example.bankapp.presentation.screens.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.R
import com.example.bankapp.data.models.OwnedCrypto
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import java.util.Locale

@Composable
fun MyCryptosListScreen(viewModel: MyPortfolioViewModel, onBack: () -> Unit, onCryptoClick: (String) -> Unit) {
    val ownedCryptos by viewModel.ownedCryptosState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.common_my_cryptos), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
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
            Text(
                stringResource(R.string.portfolio_quantity_short, String.format(Locale.US, "%.3f", owned.quantity)),
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        val totalValueBRL = owned.quantity * owned.currentPrice
        Text(
            stringResource(R.string.common_currency_prefix, String.format(Locale.GERMANY, "%,.2f", totalValueBRL)),
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}