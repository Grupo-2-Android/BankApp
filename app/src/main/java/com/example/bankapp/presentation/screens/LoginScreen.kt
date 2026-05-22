package com.example.bankapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bankapp.presentation.theme.BankAppTheme
import com.example.bankapp.presentation.viewmodels.LoginStatus
import com.example.bankapp.presentation.viewmodels.LoginViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    logoutMessage: String? = null,
    viewModel: LoginViewModel = viewModel()
) {

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    fun sanitize(input: String): String {
        val emojiRegex = Regex("[\\p{So}\\p{Cn}\\p{Cs}]")
        return input.replace("\n", "").replace(emojiRegex, "")
    }

    fun validateUsername(value: String): String? {
        return when {
            value.isBlank() -> "Usuário não pode ser vazio"
            value.trim() != value -> "Não use espaços no início ou fim"
            value.contains("  ") -> "Não use espaços duplos"
            else -> null
        }
    }

    fun validatePassword(value: String): String? {
        return when {
            value.length < 6 -> "Senha deve ter no mínimo 6 caracteres"
            else -> null
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginStatus.Success) {
            onLoginSuccess()
            viewModel.resetState()
        }
    }

    LaunchedEffect(logoutMessage) {
        logoutMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = Color.Black
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "BankApp",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Seu banco digital seguro",
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(16.dp)
                ) {

                    Column(modifier = Modifier.padding(20.dp)) {

                        OutlinedTextField(
                            value = username,
                            onValueChange = {

                                val cleaned = sanitize(it)

                                username = cleaned
                                usernameError = validateUsername(cleaned)
                            },
                            label = { Text("Usuário", color = Color.Gray) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            isError = usernameError != null
                        )

                        if (usernameError != null) {
                            Text(
                                text = usernameError!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {

                                val cleaned = sanitize(it)

                                password = cleaned
                                passwordError = validatePassword(cleaned)
                            },
                            label = { Text("Senha", color = Color.Gray) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            isError = passwordError != null
                        )

                        if (passwordError != null) {
                            Text(
                                text = passwordError!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {

                                usernameError = validateUsername(username)
                                passwordError = validatePassword(password)

                                if (usernameError == null && passwordError == null) {
                                    viewModel.login(username, password)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = loginState !is LoginStatus.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {

                            if (loginState is LoginStatus.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Entrar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    BankAppTheme {
        LoginScreen(onLoginSuccess = {})
    }
}