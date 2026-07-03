package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bmstally.app.viewmodel.RequestMessageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestMessageScreen(
    onBack: () -> Unit,
    viewModel: RequestMessageViewModel = hiltViewModel()
) {
    val message by viewModel.message.collectAsState()
    val status by viewModel.status.collectAsState()
    val sending by viewModel.sending.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Details", color = Color.White) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text("Send your requirement directly to the administrator", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    Text("Assigned To", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF616161))
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Admin",
                            Modifier.padding(12.dp),
                            fontSize = 14.sp,
                            color = Color(0xFF424242)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Describe your requirement",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161)
                    )
                    Spacer(Modifier.height(4.dp))

                    OutlinedTextField(
                        value = message,
                        onValueChange = { viewModel.setMessage(it) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        placeholder = { Text("Explain what you need... (e.g. access, issue, feature request)") },
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors()
                    )

                    if (status != null) {
                        Spacer(Modifier.height(12.dp))
                        val isError = status!!.startsWith("Please")
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ) {
                            Text(
                                status!!,
                                Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = if (isError) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.sendRequest() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !sending,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                    ) {
                        if (sending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sending...")
                        } else {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Submit Request")
                        }
                    }
                }
            }
        }
    }
}
