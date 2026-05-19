package com.finora.ai.ui.screens.simulator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioBattleScreen(onBack: () -> Unit) {
    var raceProgress by remember { mutableFloatStateOf(0f) }
    var showWinner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (raceProgress < 1f) { delay(30); raceProgress = (raceProgress + 0.008f).coerceAtMost(1f) }
        delay(500); showWinner = true
    }

    val animatedProgress by animateFloatAsState(raceProgress, tween(100), label = "race")

    data class Scenario(val name: String, val color: Color, val riskScore: Float, val runway: Int, val balance: String, val isWinner: Boolean, val seed: Float)
    val scenarios = listOf(
        Scenario("Base Case", Turquoise, 0.35f, 142, "₨4.3M", false, 0f),
        Scenario("Hire 5 Engineers", MintLeaf, 0.52f, 94, "₨2.8M", false, 1.5f),
        Scenario("Cut Costs 15%", PearlAqua, 0.22f, 186, "₨5.1M", true, 3f),
    )

    Box(Modifier.fillMaxSize().background(InkBlack)) {
        // Animated racing curves
        Canvas(Modifier.fillMaxSize().padding(top = 200.dp, bottom = 220.dp, start = 20.dp, end = 20.dp)) {
            val w = size.width; val h = size.height
            // Danger zone
            drawRect(DangerRed.copy(alpha = 0.06f), topLeft = Offset(0f, h * 0.7f), size = androidx.compose.ui.geometry.Size(w, h * 0.3f))
            drawLine(DangerRed.copy(alpha = 0.3f), Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 1f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))

            scenarios.forEach { scenario ->
                val path = Path()
                val maxX = (w * animatedProgress).toInt()
                for (x in 0..maxX step 3) {
                    val t = x / w
                    val baseY = h * (0.3f + scenario.riskScore * 0.5f)
                    val noise = sin(t * 12f + scenario.seed) * h * 0.05f + sin(t * 7f + scenario.seed * 2) * h * 0.03f
                    val y = baseY + noise - t * h * (if (scenario.isWinner) 0.15f else 0.05f)
                    if (x == 0) path.moveTo(x.toFloat(), y.coerceIn(0f, h)) else path.lineTo(x.toFloat(), y.coerceIn(0f, h))
                }
                drawPath(path, color = scenario.color, style = Stroke(width = 3f, cap = StrokeCap.Round))
            }
        }

        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = DarkTextSecondary) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Compare, contentDescription = null, modifier = Modifier.size(20.dp), tint = NeonMint)
                    Spacer(Modifier.width(8.dp))
                    Text("SCENARIO BATTLE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = NeonMint)
                }
                Spacer(Modifier.width(48.dp))
            }

            // Scenario legends
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), Arrangement.spacedBy(12.dp)) {
                scenarios.forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(s.color))
                        Spacer(Modifier.width(4.dp))
                        Text(s.name, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Winner card
            AnimatedVisibility(showWinner, enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(tween(400)), modifier = Modifier.padding(20.dp)) {
                val winner = scenarios.first { it.isWinner }
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(DarkSurfaceVariant)) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = "Winner", modifier = Modifier.size(40.dp), tint = WarningAmber)
                        Spacer(Modifier.height(8.dp))
                        Text("BATTLE WINNER", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp), color = NeonMint)
                        Text(winner.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = DarkTextPrimary)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Runway", style = MaterialTheme.typography.labelSmall, color = DarkTextTertiary); Text("${winner.runway} days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = SuccessGreen) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Balance", style = MaterialTheme.typography.labelSmall, color = DarkTextTertiary); Text(winner.balance, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = PearlAqua) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Confidence", style = MaterialTheme.typography.labelSmall, color = DarkTextTertiary); Text("87%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MintLeaf) }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = {}, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MintLeaf, contentColor = InkBlack)) {
                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Scenario", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
