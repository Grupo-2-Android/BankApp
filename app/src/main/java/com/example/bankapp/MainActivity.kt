package com.example.bankapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.api.RetrofitClient
import kotlinx.coroutines.launch

// --- Identidade Visual ---
val BankGreen = Color(0xFF4CAF50) // Verde estilo Bank
val DarkCard = Color(0xFF121212) // Fundo dos cards

// --- Modelos de Dados (Devem bater com os DTOs do Java) ---
data class UserLoginRequest(val login: String, val password: String)
data class UserLoginResponse(val id: String?, val name: String?, val message: String)
data class CryptoPurchaseRequest(val userId: String, val symbol: String, val quantity: Double)
data class CryptoPurchaseResponse(val transactionId: String, val message: String)
data class CryptoListResponse(val symbols: List<CryptoModel>?)
data class CryptoModel(val symbol: String, val name: String?, val last: String?)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color.Black,
                    surface = Color.Black,
                    primary = BankGreen
                )
            ) {
                BankAppNavigation()
            }
        }
    }
}

@Composable
fun BankAppNavigation() {
    var currentScreen by remember { mutableStateOf("login") }
    var currentUser by remember { mutableStateOf<UserLoginResponse?>(null) }
    var selectedCrypto by remember { mutableStateOf<CryptoModel?>(null) }
    var quantityInput by remember { mutableStateOf(0.0) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        when (currentScreen) {
            "login" -> LoginScreen { user ->
                currentUser = user
                currentScreen = "dashboard"
            }
            "dashboard" -> DashboardScreen(currentUser?.name ?: "Usuário") {
                currentScreen = "list"
            }
            "list" -> CryptoListScreen(
                onBack = { currentScreen = "dashboard" },
                onSelect = { crypto ->
                    selectedCrypto = crypto
                    currentScreen = "quantidade"
                }
            )
            "quantidade" -> QuantityScreen(
                symbol = selectedCrypto?.symbol ?: "",
                onConfirm = { qty ->
                    quantityInput = qty
                    currentScreen = "checkout"
                }
            )
            "checkout" -> CheckoutScreen(
                crypto = selectedCrypto,
                quantity = quantityInput,
                userId = currentUser?.id ?: "",
                onSuccess = { currentScreen = "dashboard" },
                onCancel = { currentScreen = "list" }
            )
        }
    }
}

// --- Componente de Botão Padrão Green ---
@Composable
fun BankButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BankGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// --- Telas ---

@Composable
fun LoginScreen(onLoginSuccess: (UserLoginResponse) -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("BankApp", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(48.dp))
        OutlinedTextField(user, { user = it }, label = { Text("Login") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(32.dp))
        BankButton("Entrar", {
            scope.launch {
                try {
                    val res = RetrofitClient.instance.login(UserLoginRequest(user, pass))
                    if (res.id != null) onLoginSuccess(res)
                    else Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(context, "Erro de Conexão", Toast.LENGTH_SHORT).show() }
            }
        })
    }
}

@Composable
fun DashboardScreen(userName: String, onGoToCryptos: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Olá, $userName!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Text("Dashboard", color = Color.Gray, fontSize = 18.sp)
        Spacer(Modifier.height(64.dp))
        BankButton("Cryptos", onGoToCryptos)
        Spacer(Modifier.height(16.dp))
        BankButton("Minhas Cryptos", { /* Reservado para outra squad */ })
    }
}

@Composable
fun CryptoListScreen(onBack: () -> Unit, onSelect: (CryptoModel) -> Unit) {
    var cryptos by remember { mutableStateOf<List<CryptoModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try { cryptos = RetrofitClient.instance.getCryptoList().symbols ?: emptyList() } catch (e: Exception) {}
        finally { loading = false }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mercado", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))
        if (loading) CircularProgressIndicator(color = BankGreen, modifier = Modifier.align(Alignment.CenterHorizontally))
        else {
            LazyColumn {
                items(cryptos) { crypto ->
                    Card(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelect(crypto) },
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(0.5.dp, Color.DarkGray)
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), Arrangement.SpaceBetween) {
                            Column {
                                Text(crypto.name ?: "", color = Color.White, fontWeight = FontWeight.Bold)
                                Text(crypto.symbol, color = Color.Gray, fontSize = 12.sp)
                            }
                            Text("$ ${crypto.last ?: "0.0"}", color = BankGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuantityScreen(symbol: String, onConfirm: (Double) -> Unit) {
    var qty by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Quanto de $symbol deseja comprar?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            qty, { qty = it }, label = { Text("Quantidade") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(32.dp))
        BankButton("Continuar", { if (qty.isNotEmpty()) onConfirm(qty.toDouble()) })
    }
}

@Composable
fun CheckoutScreen(crypto: CryptoModel?, quantity: Double, userId: String, onSuccess: () -> Unit, onCancel: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val unitPrice = crypto?.last?.toDoubleOrNull() ?: 0.0
    val total = unitPrice * quantity

    Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Resumo da Compra", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(32.dp))

        Box(Modifier.fillMaxWidth().background(DarkCard, RoundedCornerShape(16.dp)).padding(24.dp)) {
            Column {
                Text("Cripto: ${crypto?.name} (${crypto?.symbol})", color = Color.White)
                Text("Quantidade: $quantity", color = Color.White)
                Text("Preço Unitário: $ $unitPrice", color = Color.Gray, fontSize = 14.sp)
                HorizontalDivider(Modifier.padding(vertical = 16.dp), color = Color.DarkGray)
                Text("Total: $ $total", color = BankGreen, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            }
        }

        Spacer(Modifier.height(48.dp))
        BankButton("Confirmar Pagamento", {
            scope.launch {
                try {
                    val res = RetrofitClient.instance.purchaseCrypto(CryptoPurchaseRequest(userId, crypto?.symbol ?: "", quantity))
                    Toast.makeText(context, res.message, Toast.LENGTH_LONG).show() // Sucesso!
                    onSuccess() // Retorna ao Dashboard
                } catch (e: Exception) { Toast.makeText(context, "Falha na Transação", Toast.LENGTH_SHORT).show() }
            }
        })
        TextButton(onClick = onCancel) { Text("Cancelar", color = Color.Gray) }
    }
}
