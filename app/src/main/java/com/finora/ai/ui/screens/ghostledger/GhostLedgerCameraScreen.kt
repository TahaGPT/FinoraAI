package com.finora.ai.ui.screens.ghostledger

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.finora.ai.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun GhostLedgerCameraScreen(onPhotoCaptured: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isCapturing by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "cam")
    val scanLine by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "scan")
    val borderPulse by infiniteTransition.animateFloat(0.4f, 0.9f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse")

    Box(Modifier.fillMaxSize().background(InkBlack)) {
        if (hasCameraPermission) {
            CameraPreview(modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize().background(JetBlack), contentAlignment = Alignment.Center) {
                Text("Camera permission required", color = Color.White)
            }
        }

        // Edge detection overlay
        Canvas(Modifier.fillMaxSize().padding(40.dp)) {
            val w = size.width; val h = size.height
            val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            drawRoundRect(color = MintLeaf.copy(alpha = borderPulse), cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f), style = Stroke(width = 2f, pathEffect = dash))
            
            val lineY = h * scanLine
            drawLine(NeonMint.copy(alpha = 0.6f), Offset(0f, lineY), Offset(w, lineY), strokeWidth = 2f)
            
            val cornerLen = 30f
            listOf(Offset(0f,0f), Offset(w,0f), Offset(0f,h), Offset(w,h)).forEach { corner ->
                val dx = if (corner.x == 0f) 1f else -1f
                val dy = if (corner.y == 0f) 1f else -1f
                drawLine(MintLeaf, corner, Offset(corner.x + cornerLen*dx, corner.y), strokeWidth = 3f)
                drawLine(MintLeaf, corner, Offset(corner.x, corner.y + cornerLen*dy), strokeWidth = 3f)
            }
        }

        // Top bar
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.Close, "Close", tint = Color.White) }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ghost Ledger 📷", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text("Align handwritten ledger within frame", style = MaterialTheme.typography.labelSmall, color = PearlAqua)
            }
            IconButton(onClick = {}) { Icon(Icons.Filled.FlashOn, "Flash", tint = Color.White) }
        }

        // Processing overlay
        AnimatedVisibility(isProcessing, Modifier.fillMaxSize(), enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(InkBlack.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MintLeaf, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Gemini is reading your ledger...", style = MaterialTheme.typography.bodyMedium, color = PearlAqua)
                    Spacer(Modifier.height(4.dp))
                    Text("Detecting rows, columns & amounts", style = MaterialTheme.typography.labelSmall, color = DarkTextTertiary)
                }
            }
        }

        // Capture button
        Box(Modifier.fillMaxWidth().align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 32.dp), contentAlignment = Alignment.Center) {
            FloatingActionButton(
                onClick = {
                    isCapturing = true; isProcessing = true
                },
                containerColor = MintLeaf, contentColor = InkBlack,
                shape = CircleShape, modifier = Modifier.size(72.dp),
            ) { Icon(Icons.Filled.CameraAlt, "Capture", modifier = Modifier.size(32.dp)) }
        }
    }

    // Navigate after simulated processing
    LaunchedEffect(isProcessing) {
        if (isProcessing) { delay(2500); onPhotoCaptured("ext_001") }
    }
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = modifier
    )
}
