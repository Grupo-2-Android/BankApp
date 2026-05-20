package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankapp.data.local.datastore.UserPreferences
import com.example.bankapp.data.local.room.AppDatabase
import com.example.bankapp.presentation.screens.CryptoDetailScreen
import com.example.bankapp.presentation.screens.CryptoListScreen
import com.example.bankapp.presentation.screens.LoginScreen
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.CryptoViewModel
import com.example.bankapp.presentation.viewmodels.MyPortfolioViewModel
import com.example.bankapp.presentation.viewmodels.ViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            BankAppTheme {

                val navController = rememberNavController()
                val context = LocalContext.current

                val userPreferences = remember {
                    UserPreferences(context)
                }

                val database = remember {
                    AppDatabase.getDatabase(context)
                }

                val factory = remember {
                    ViewModelFactory(userPreferences, database)
                }

                val cryptoViewModel: CryptoViewModel =
                    viewModel(factory = factory)

                val portfolioViewModel: MyPortfolioViewModel =
                    viewModel()

                val snackbarHostState = remember {
                    SnackbarHostState()
                }

                var userName by remember {
                    mutableStateOf("Usuário")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    }
                ) { innerPadding ->

                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {

                        NavHost(
                            navController = navController,
                            startDestination = "login"
                        ) {

                            composable("login") {

                                LoginScreen(
                                    viewModel = viewModel(factory = factory),

                                    onLoginSuccess = { name ->

                                        userName = name

                                        navController.navigate("dashboard") {
                                            popUpTo("login") {
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }

                            composable("dashboard") {

                                DashboardScreen(
                                    userName = userName,

                                    onNavigateToCryptos = {
                                        navController.navigate("crypto_list")
                                    },

                                    onNavigateToMyCryptos = {
                                        navController.navigate("my_cryptos")
                                    },

                                    onLogout = {

                                        navController.navigate("login") {
                                            popUpTo(0)
                                        }
                                    },

                                    snackbarHostState = snackbarHostState
                                )
                            }

                            composable("crypto_list") {

                                CryptoListScreen(
                                    viewModel = cryptoViewModel,

                                    onCryptoClick = { cryptoId ->
                                        navController.navigate(
                                            "crypto_detail/$cryptoId"
                                        )
                                    }
                                )
                            }

                            composable("crypto_detail/{cryptoId}") { backStackEntry ->

                                val cryptoId =
                                    backStackEntry.arguments?.getString("cryptoId")
                                        ?: ""

                                CryptoDetailScreen(
                                    cryptoId = cryptoId,
                                    viewModel = cryptoViewModel,

                                    onBack = {
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable("my_cryptos") {

                                Text(
                                    text = "Tela Minhas Cryptos"
                                )

                                // Você pode substituir depois
                                // pela sua tela real de portfolio
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    onNavigateToCryptos: () -> Unit,
    onNavigateToMyCryptos: () -> Unit,
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {

    val drawerState =
        rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(

        drawerState = drawerState,

        drawerContent = {

            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight(),
                drawerContainerColor = Color(0xFF1E1E1E)
            ) {

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Olá, $userName",
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

                        onNavigateToCryptos()
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

                        onNavigateToMyCryptos()
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
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFF4CAF50)
                        )
                    },

                    onClick = {

                        scope.launch {
                            drawerState.close()
                        }

                        onLogout()

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Logout realizado com sucesso!"
                            )
                        }
                    }
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
                            "Dashboard",
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

                Text(
                    text = "Olá, $userName!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                Button(
                    onClick = onNavigateToCryptos,

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
                        "Cryptos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToMyCryptos,

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
                        "Minhas Cryptos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {

    DashboardScreen(
        userName = "Usuário Teste",
        onNavigateToCryptos = {},
        onNavigateToMyCryptos = {},
        onLogout = {},
        snackbarHostState = SnackbarHostState()
    )
}