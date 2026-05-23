package com.example.bankapp.presentation.screens.cards

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankapp.presentation.utils.components.CardItem
import com.example.bankapp.presentation.viewmodels.cards.CardManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardManagementScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: CardManagementViewModel,
    onBack: () -> Unit,
    onNavigateToAddCard: (String) -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Cartões") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Cartão")
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
                    Text("Você ainda não possui cartões.")
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

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Adicionar Cartão") },
            text = { Text("Escolha como deseja adicionar o cartão.") },
            confirmButton = {
                TextButton(onClick = {
                    val canAddType = viewModel.canAddType(CardManagementViewModel.TYPE_VIRTUAL)
                    if (canAddType) {
                        showAddDialog = false
                        onNavigateToAddCard(CardManagementViewModel.TYPE_VIRTUAL)
                    } else {
                        showAddDialog = false
                    }
                }) {
                    Text("Virtual")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val canAddType = viewModel.canAddType(CardManagementViewModel.TYPE_PHYSICAL)
                    if (canAddType) {
                        showAddDialog = false
                        onNavigateToAddCard(CardManagementViewModel.TYPE_PHYSICAL)
                    } else {
                        showAddDialog = false
                    }
                }) {
                    Text("Físico")
                }
            }
        )
    }
}
