package com.example.bankapp.presentation.screens.cards

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bankapp.R
import com.example.bankapp.presentation.theme.GreenPrimary
import com.example.bankapp.presentation.theme.PhysicalCardColor
import com.example.bankapp.presentation.theme.RedDelete
import com.example.bankapp.presentation.theme.VirtualCardColor
import com.example.bankapp.presentation.utils.components.CardItem
import com.example.bankapp.presentation.viewmodels.CardManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardManagementScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: CardManagementViewModel,
    onBack: () -> Unit,
    onNavigateToAddCard: (String) -> Unit
) {
    val context = LocalContext.current
    val cards by viewModel.cards.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddCardBottomSheet by remember { mutableStateOf(false) }
    var showDeleteCardBottomSheet by remember { mutableStateOf(false) }
    var pendingDeleteCard by remember {
        mutableStateOf<com.example.bankapp.data.local.room.entities.Card?>(null)
    }
    var pendingNavigationType by remember { mutableStateOf<String?>(null) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val roundedButtonShape = RoundedCornerShape(12.dp)

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingNavigationType?.let { onNavigateToAddCard(it) }
        }
        pendingNavigationType = null
    }

    fun navigateToAddCardWithPermission(type: String) {
        if (type == CardManagementViewModel.TYPE_PHYSICAL) {
            val hasCameraPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            if (hasCameraPermission) {
                onNavigateToAddCard(type)
            } else {
                pendingNavigationType = type
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } else {
            onNavigateToAddCard(type)
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cards_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cards.size < 2) {
                    Button(
                        onClick = { showAddCardBottomSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = roundedButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.cards_add))
                    }
                }

                if (cards.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showDeleteCardBottomSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = roundedButtonShape,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedDelete),
                        border = BorderStroke(1.dp, RedDelete)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = RedDelete)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.cards_delete), color = RedDelete)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (cards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.cards_empty))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cards) { card ->
                        CardItem(card)
                    }
                }
            }
        }
    }

    if (showAddCardBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCardBottomSheet = false },
            sheetState = addSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 2.dp,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.cards_choose_type),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                Button(
                    onClick = {
                        val canAddType = viewModel.canAddType(CardManagementViewModel.TYPE_VIRTUAL)
                        showAddCardBottomSheet = false
                        if (canAddType) {
                            navigateToAddCardWithPermission(CardManagementViewModel.TYPE_VIRTUAL)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VirtualCardColor,
                        contentColor = Color.White
                    ),
                    shape = roundedButtonShape
                ) {
                    Text(stringResource(R.string.common_virtual))
                }

                Button(
                    onClick = {
                        val canAddType = viewModel.canAddType(CardManagementViewModel.TYPE_PHYSICAL)
                        showAddCardBottomSheet = false
                        if (canAddType) {
                            navigateToAddCardWithPermission(CardManagementViewModel.TYPE_PHYSICAL)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhysicalCardColor,
                        contentColor = Color.White
                    ),
                    shape = roundedButtonShape
                ) {
                    Text(stringResource(R.string.common_physical))
                }
            }
        }
    }

    if (showDeleteCardBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteCardBottomSheet = false },
            sheetState = deleteSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 2.dp,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.cards_choose_to_delete),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                cards.forEach { card ->
                    val backgroundColor = if (card.type == CardManagementViewModel.TYPE_VIRTUAL) {
                        VirtualCardColor
                    } else {
                        PhysicalCardColor
                    }

                    Button(
                        onClick = {
                            pendingDeleteCard = card
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = roundedButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = backgroundColor,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = stringResource(R.string.cards_ending, card.number.takeLast(4)))
                            Text(
                                text = stringResource(
                                    if (card.type == CardManagementViewModel.TYPE_PHYSICAL) {
                                        R.string.common_physical
                                    } else {
                                        R.string.common_virtual
                                    }
                                ),
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteCard != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteCard = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = {
                Text(stringResource(R.string.cards_delete_confirm_title))
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.cards_delete_confirm_message,
                        pendingDeleteCard?.number?.takeLast(4).orEmpty()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteCard?.let { viewModel.deleteCard(it) }
                        pendingDeleteCard = null
                        showDeleteCardBottomSheet = false
                    }
                ) {
                    Text(stringResource(R.string.cards_delete), color = RedDelete)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteCard = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
