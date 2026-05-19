package com.finora.ai.ui.screens.simulator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(onStartBattle: () -> Unit, onBack: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var showContent by remember { mutableStateOf(false) }
    var showChips by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); showContent = true }

    data class StressTest(val icon: ImageVector, val label: String)
    val stressTests = listOf(
        StressTest(Icons.AutoMirrored.Filled.TrendingDown, "Revenue drops 20% for 3 months"),
        StressTest(Icons.Filled.Inventory, "Supplier raises prices 15%"),
        StressTest(Icons.Filled.CurrencyExchange, "PKR/USD increases 10%"),
        StressTest(Icons.Filled.PersonOff, "Largest customer churns"),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What-If Simulator", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400)) + expandVertically()) {
                    Column {
                        Text("Ask Finora Anything", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onBackground)
                        Text("Describe a scenario in natural language", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // NL Query Input
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 100)) + slideInVertically(initialOffsetY = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                    OutlinedTextField(
                        value = query, onValueChange = { query = it; showChips = it.length > 10 },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("What if I hire 5 engineers at PKR 90k each?", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        trailingIcon = {
                            IconButton(onClick = { if (query.isNotEmpty()) onStartBattle() }) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = if (query.isNotEmpty()) MintLeaf else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintLeaf, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), cursorColor = MintLeaf),
                        minLines = 2, maxLines = 4,
                    )
                }
            }

            // Extracted params chips
            if (showChips) item {
                AnimatedVisibility(true, enter = fadeIn(tween(300)) + expandVertically()) {
                    Column {
                        Text("Extracted Parameters", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("hire_count: 5", "salary: 90k", "monthly_cost: 450k").forEach { chip ->
                                AssistChip(onClick = {}, label = { Text(chip, style = MaterialTheme.typography.labelSmall) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = MintLeaf.copy(alpha = 0.12f), labelColor = MintLeaf), shape = RoundedCornerShape(8.dp))
                            }
                        }
                    }
                }
            }

            // Stress Tests
            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 200))) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Text("One-Tap Stress Tests", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onBackground)
                        Spacer(Modifier.height(4.dp))
                        Text("Pre-configured scenarios to battle-test your finances", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            itemsIndexed(stressTests) { index, test ->
                AnimatedVisibility(showContent, enter = fadeIn(tween(300, delayMillis = 300 + index * 80)) + slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))) {
                    Card(
                        onClick = onStartBattle,
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(test.icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = WarningAmber)
                            Spacer(Modifier.width(12.dp))
                            Text(test.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Run", modifier = Modifier.size(20.dp), tint = MintLeaf)
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(showContent, enter = fadeIn(tween(400, delayMillis = 500))) {
                    Button(onClick = onStartBattle, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MintLeaf, contentColor = InkBlack)) {
                        Icon(Icons.Filled.Compare, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Run Scenario Battle", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp))
                    }
                }
            }
        }
    }
}
