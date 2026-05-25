package com.example.bankapp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bankapp.presentation.viewmodels.DashboardViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navController: NavController,
    onLogout: () -> Unit
) {
    val userAccount by viewModel.userAccount.collectAsState()

    val drawerState =
        rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(),
                drawerContainerColor = Color(0xFF1E1E1E)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Olá, ${userAccount?.name ?: "Usuário"}",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Menu",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = Color.Gray)

                NavigationDrawerItem(

                    label = {
                        Text("Cryptos", color = Color.White)
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onNavigateToCryptos(navController = navController)
                    },

                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color(0xFF4CAF50),
                        unselectedTextColor = Color.White,
                        selectedTextColor = Color.White
                    )
                )

                NavigationDrawerItem(

                    label = {
                        Text("Minhas Cryptos", color = Color.White)
                    },

                    selected = false,

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onNavigateToMyCryptos(navController = navController)
                    },

                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color(0xFF4CAF50),
                        unselectedTextColor = Color.White,
                        selectedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color.Gray)

                Spacer(modifier = Modifier.weight(1f))

                NavigationDrawerItem(

                    label = {
                        Text("Sair", color = Color.White)
                    },

                    selected = false,

                    icon = {

                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFF4CAF50)
                        )
                    },

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onLogout()
                    },

                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = Color(0xFF4CAF50),
                        unselectedTextColor = Color.White,
                        selectedTextColor = Color.White
                    )
                )
            }
        }
    ) {

        Scaffold(

            containerColor = Color.Black,

            snackbarHost = {
                SnackbarHost(snackbarHostState)
            },

            topBar = {

                TopAppBar(

                    title = {
                        Text(
                            text = "Dashboard",
                            color = Color.White
                        )
                    },

                    navigationIcon = {

                        IconButton(
                            onClick = {

                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },

                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->

            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),

                horizontalAlignment = Alignment.CenterHorizontally,

                verticalArrangement = Arrangement.Center

            ) {

                userAccount?.let { account ->

                    Text(
                        text = "Olá, ${account.name}!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Saldo: R$ ${
                            String.format(
                                Locale("pt", "BR"),
                                "%,.2f",
                                account.balance
                            )
                        }",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = { onNavigateToCryptos(navController = navController) },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),

                    shape = RoundedCornerShape(8.dp)

                ) {

                    Text(
                        text = "Cryptos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(

                    onClick = { onNavigateToMyCryptos(navController = navController) },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),

                    shape = RoundedCornerShape(8.dp)

                ) {

                    Text(
                        text = "Minhas Cryptos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("cards") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Cartões",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

private fun onNavigateToCryptos(navController: NavController) {
    navController.navigate("crypto_list")
}

private fun onNavigateToMyCryptos(navController: NavController) {
    navController.navigate("my_cryptos")
}
