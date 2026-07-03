package com.bmstally.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.bmstally.app.data.MockDataService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    tenantId: String,
    onBack: () -> Unit
) {
    val parties = remember { MockDataService.getParties(tenantId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Parties / Contacts", color = Color.White) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) } },
            actions = { IconButton({}) { Icon(Icons.Default.PersonAdd, "Add", tint = Color.White) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F51B5))
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5).copy(alpha = 0.1f))
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${parties.size}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF3F51B5))
                    Text("Parties", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Active", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF4CAF50))
                    Text("Status", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(parties) { party ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF3F51B5).copy(alpha = 0.1f)) {
                            Icon(Icons.Default.Person, null, Modifier.padding(8.dp).size(24.dp), tint = Color(0xFF3F51B5))
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(party, fontWeight = FontWeight.W500, fontSize = 15.sp)
                        Spacer(Modifier.weight(1f))
                        IconButton({}) { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) }
                    }
                }
            }
        }
    }
}
