package com.example.bankapp.presentation.screens.cards

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.br.scan_card.CreditCardData
import com.br.scan_card.ScanCardActivity
import com.example.bankapp.R
import com.example.bankapp.data.local.room.entities.Card
import com.example.bankapp.presentation.theme.GreenPrimary
import com.example.bankapp.presentation.theme.GreenSecondary
import com.example.bankapp.presentation.utils.components.CardItem
import com.example.bankapp.presentation.utils.formatCardNumber
import com.example.bankapp.presentation.viewmodels.AddCardUiState
import com.example.bankapp.presentation.viewmodels.CardManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    modifier: Modifier = Modifier,
    viewModel: CardManagementViewModel,
    cardType: String,
    onBack: () -> Unit,
    onConfirmSuccess: () -> Unit
) {
    val isPhysical = cardType == CardManagementViewModel.TYPE_PHYSICAL
    val previewCard by viewModel.previewCard.collectAsState()
    val error by viewModel.error.collectAsState()
    val addCardUiState by viewModel.addCardUiState.collectAsState()
    val context = LocalContext.current

    val scanCardLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cardData = parseCardData(result.data)
        viewModel.setScannedCardPreview(cardData)
    }

    LaunchedEffect(cardType) {
        viewModel.clearError()
        viewModel.clearPreview()

        if (isPhysical) {
            val intent = Intent(context, ScanCardActivity::class.java)
            scanCardLauncher.launch(intent)
        } else {
            viewModel.generateVirtualCard()
        }
    }

    val displayCard = previewCard ?: Card(
        userId = "",
        type = cardType,
        number = stringResource(R.string.add_card_placeholder_number),
        expiration = stringResource(R.string.add_card_placeholder_expiration),
        cvv = stringResource(R.string.add_card_placeholder_cvv),
        brand = stringResource(R.string.add_card_placeholder_brand)
    )

    val typeLabel = stringResource(
        if (isPhysical) R.string.common_physical else R.string.common_virtual
    )
    val canConfirm = !isPhysical || previewCard != null
    val roundedButtonShape = RoundedCornerShape(12.dp)

    if (addCardUiState is AddCardUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.add_card_title, typeLabel)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    color = Color.Transparent,
                    tonalElevation = 4.dp
                ) {
                    Button(
                        onClick = { viewModel.confirmAddCard(onSuccess = onConfirmSuccess) },
                        enabled = canConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(48.dp),
                        shape = roundedButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f),
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.common_confirm),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        CardItem(card = displayCard, isAdding = true)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.add_card_data_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadOnlyField(
                            modifier = Modifier.weight(1f),
                            value = formatCardNumber(displayCard.number),
                            label = stringResource(R.string.add_card_label_number)
                        )
                        ReadOnlyField(
                            modifier = Modifier.weight(0.5f),
                            value = displayCard.cvv,
                            label = stringResource(R.string.add_card_label_cvv)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadOnlyField(
                            modifier = Modifier.weight(1f),
                            value = displayCard.expiration,
                            label = stringResource(R.string.add_card_label_expiration)
                        )
                        ReadOnlyField(
                            modifier = Modifier.weight(1f),
                            value = displayCard.brand,
                            label = stringResource(R.string.add_card_label_brand)
                        )
                    }
                }

                if (isPhysical) {
                    item {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(context, ScanCardActivity::class.java)
                                scanCardLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = roundedButtonShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenSecondary),
                            border = BorderStroke(1.dp, GreenSecondary)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.scan_card),
                                contentDescription = stringResource(R.string.add_card_scan),
                                tint = GreenSecondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_card_scan_again))
                        }
                    }
                }

                if (error != null) {
                    item {
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun ReadOnlyField(
    modifier: Modifier = Modifier,
    value: String,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        enabled = false,
        singleLine = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}

private fun parseCardData(data: Intent?): CreditCardData? {
    if (data == null) return null
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        data.getParcelableExtra(ScanCardActivity.CREDIT_CARD_DATA, CreditCardData::class.java)
    } else {
        @Suppress("DEPRECATION")
        data.getParcelableExtra(ScanCardActivity.CREDIT_CARD_DATA)
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

