package com.example.bankapp.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bankapp.BankButton
import com.example.bankapp.data.models.UserLoginResponse
import com.example.bankapp.presentation.viewmodels.LoginViewModel
import com.example.bankapp.presentation.viewmodels.LoginStatus

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (UserLoginResponse) -> Unit
) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()

    // Monitora o estado de login
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginStatus.Success -> {
                // Ao logar com sucesso (ou via fallback), simulamos a resposta para a MainActivity
                onLoginSuccess(UserLoginResponse(user, user, "Sucesso"))
                viewModel.resetState()
            }
            is LoginStatus.Error -> {
                Toast.makeText(context, (loginState as LoginStatus.Error).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            Text("BankApp", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(48.dp))
            
            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { Text("Login", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Senha", color = Color.Gray) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (loginState is LoginStatus.Loading) {
                CircularProgressIndicator(color = Color(0xFF4CAF50))
            } else {
                BankButton("Entrar", {
                    if (user.isNotBlank() && pass.isNotBlank()) {
                        viewModel.login(user, pass)
                    } else {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}
