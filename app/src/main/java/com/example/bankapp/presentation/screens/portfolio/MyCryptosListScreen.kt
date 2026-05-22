package com.example.bankapp.presentation.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.data.models.OwnedCrypto
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCryptosListScreen(
    viewModel: MyPortfolioViewModel,
    onNavigateToCryptos: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit
) {
    val ownedCryptos by viewModel.ownedCryptosState.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1E1E1E),
                modifier = Modifier.fillMaxHeight()
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Olá, $userName",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(color = Color.Gray)
                
                NavigationDrawerItem(
                    label = { Text("Histórico", color = Color.White) },
                    selected = false,
                    icon = {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToHistory()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )

                NavigationDrawerItem(
                    label = { Text("Sair", color = Color.White) },
                    selected = false,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    },
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent
                    )
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        // Título removido daqui para evitar duplicidade com o headline abaixo
                    },
                    actions = {
                        Text(
                            text = "Olá, $userName",
                            color = Color.White,
                            modifier = Modifier.padding(end = 16.dp),
                            fontSize = 14.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Black,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onNavigateToCryptos,
                        icon = { Icon(Icons.Default.Home, contentDescription = "Cryptos") },
                        label = { Text("Cryptos") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Já na tela */ },
                        icon = { Icon(Icons.Default.Wallet, contentDescription = "Minhas Cryptos") },
                        label = { Text("Minhas Cryptos") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4CAF50),
                            selectedTextColor = Color(0xFF4CAF50),
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.open() }
                        },
                        icon = { Icon(Icons.Default.Menu, contentDescription = "Menu") },
                        label = { Text("Menu") },
                        colors = NavigationBarItemDefaults.colors(
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            },
            containerColor = Color.Black
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Minhas Cryptos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(ownedCryptos) { owned ->
                        MyCryptoCard(owned = owned)
                    }
                }
            }
        }
    }
}

@Composable
fun MyCryptoCard(owned: OwnedCrypto) {
    val variation = if (owned.cryptoInfo.symbol == "LTC") -0.52 else 2.31
    val variationColor = if (variation >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)

    val logoColor = when (owned.cryptoInfo.symbol) {
        "BTC" -> Color(0xFFF7931A)
        "ETH" -> Color(0xFF627EEA)
        "LTC" -> Color(0xFF345D9D)
        else -> Color.DarkGray
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF121212)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(logoColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = owned.cryptoInfo.symbol,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = owned.cryptoInfo.symbol,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = owned.cryptoInfo.id.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            },
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Text(text = "Ultim 24h", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${String.format(Locale.US, "%.8f", owned.quantity)} ${owned.cryptoInfo.symbol}",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalValueBRL = owned.quantity * owned.currentPrice
                Text(
                    text = "R$ ${String.format(Locale("pt", "BR"), "%,.2f", totalValueBRL)} BRL",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFBBBBBB)
                )

                Surface(
                    color = variationColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${if (variation >= 0) "+" else ""}${String.format("%.2f", variation)}%",
                        color = variationColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
