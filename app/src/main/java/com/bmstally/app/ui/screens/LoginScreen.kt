package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bmstally.app.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    loginState: LoginUiState,
    onLogin: (tenantCode: String, username: String, password: String) -> Unit,
    onResetState: () -> Unit
) {
    var tenantCode by remember { mutableStateOf("tally") }
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("") }
    var obscure by remember { mutableStateOf(true) }

    val isLoading = loginState is LoginUiState.Loading
    val errorMessage = (loginState as? LoginUiState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Calculate, contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary)

        Spacer(Modifier.height(16.dp))
        Text("BMS Tally", style = MaterialTheme.typography.headlineLarge)
        Text("Sign in to your account", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(tenantCode, {
            tenantCode = it
            onResetState()
        },
            label = { Text("Tenant Code") },
            leadingIcon = { Icon(Icons.Default.Business, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(username, {
            username = it
            onResetState()
        },
            label = { Text("Username") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading)

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(password, {
            password = it
            onResetState()
        },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton({ obscure = !obscure }) {
                    Icon(if (obscure) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            visualTransformation = if (obscure) PasswordVisualTransformation() else VisualTransformation.None,
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading)

        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(errorMessage, color = Color(0xFFD32F2F), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = { onLogin(tenantCode, username, password) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(48.dp)) {
            if (isLoading) CircularProgressIndicator(strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            else Text("Login")
        }

        Spacer(Modifier.height(12.dp))
        Text("Demo: tally/bmscorp/techmart · admin/admin...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}
