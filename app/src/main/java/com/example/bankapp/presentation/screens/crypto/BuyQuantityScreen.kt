package com.example.bankapp.presentation.screens.crypto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.R
import com.example.bankapp.presentation.theme.GreenPrimary
import com.example.bankapp.presentation.viewmodels.BuyUiState
import com.example.bankapp.presentation.viewmodels.CryptoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyQuantityScreen(
    viewModel: CryptoViewModel,
    onBack: () -> Unit,
    onNavigateToCheckout: () -> Unit
) {
            val invalidQuantityMessage = stringResource(R.string.buy_invalid_quantity)
            val roundedButtonShape = RoundedCornerShape(12.dp)

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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.buy_quantity_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.buy_how_much, crypto.symbol),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = quantityText,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("""^\d*\.?\d*$"""))) {
                        quantityText = it
                        errorMessage = null
                    }
                },
                label = {
                    Text(
                        stringResource(R.string.buy_quantity_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull()
                    if (qty == null || qty <= 0) {
                        errorMessage = invalidQuantityMessage
                    } else {
                        viewModel.setBuyAmount(qty)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = roundedButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenPrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    stringResource(R.string.buy_continue_to_summary),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
