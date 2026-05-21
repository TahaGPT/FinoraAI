package com.finora.ai.ui.screens.analysis

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finora.ai.data.model.SourceType
import com.finora.ai.ui.theme.*
import com.finora.ai.viewmodel.FinoraViewModel
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════════════
// Source Selection Screen — Multi-source analysis setup
// Now with functional file picking & simulated upload
// ═══════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceSelectionScreen(
    viewModel: FinoraViewModel,
    onStartAnalysis: (String) -> Unit,
    onBack: () -> Unit,
) {
    val selectedSources = remember { mutableStateListOf<SourceType>() }
    val uploadedFiles = remember { mutableStateMapOf<SourceType, String>() }
    var showContent by remember { mutableStateOf(false) }
    var isStarting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    // When the ViewModel gets a session ID from the backend, navigate
    LaunchedEffect(viewModel.currentSessionId) {
        val sessionId = viewModel.currentSessionId
        if (sessionId != null && isStarting) {
            onStartAnalysis(sessionId)
        }
    }

    // File Pickers
    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadedFiles[SourceType.PDF] = "Selected: ${it.path?.substringAfterLast("/")}" }
    }
    val csvPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploadedFiles[SourceType.CSV] = "Selected: ${it.path?.substringAfterLast("/")}" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Analysis Session",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedSources.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding(),
                    ) {
                        Text(
                            text = "${selectedSources.size} source${if (selectedSources.size > 1) "s" else ""} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isStarting = true
                                // Trigger the real backend analysis
                                viewModel.startAnalysis(
                                    selectedSources.map { it.name }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MintLeaf,
                                contentColor = InkBlack,
                            ),
                            enabled = !isStarting,
                        ) {
                            if (isStarting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = InkBlack,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                "Start Analysis",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400)) + expandVertically(),
                ) {
                    Column {
                        Text(
                            text = "Select Data Sources",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose one or more sources for Finora to analyze. The more data, the smarter the insights.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        // Backend connection indicator
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (viewModel.isBackendReachable) SuccessGreen else WarningAmber)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (viewModel.isBackendReachable) "Backend connected" else "Backend connecting...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            itemsIndexed(SourceType.values().toList()) { index, sourceType ->
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(300, delayMillis = 100 + index * 70)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 3 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            )
                        ),
                ) {
                    SourceToggleCard(
                        sourceType = sourceType,
                        isSelected = sourceType in selectedSources,
                        uploadedInfo = uploadedFiles[sourceType],
                        onToggle = {
                            if (sourceType in selectedSources) {
                                selectedSources.remove(sourceType)
                                uploadedFiles.remove(sourceType)
                            } else {
                                selectedSources.add(sourceType)
                            }
                        },
                        onUpload = {
                            when (sourceType) {
                                SourceType.PDF -> pdfPicker.launch("application/pdf")
                                SourceType.CSV -> csvPicker.launch("*/*")
                                SourceType.URL -> uploadedFiles[SourceType.URL] = "URL Connected"
                                SourceType.REAL_TIME -> uploadedFiles[SourceType.REAL_TIME] = "Feed Connected"
                                SourceType.GHOST_LEDGER -> uploadedFiles[SourceType.GHOST_LEDGER] = "Camera Ready"
                                SourceType.GOOGLE_SHEETS -> uploadedFiles[SourceType.GOOGLE_SHEETS] = "Sheet Linked"
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SourceToggleCard(
    sourceType: SourceType,
    isSelected: Boolean,
    uploadedInfo: String?,
    onToggle: () -> Unit,
    onUpload: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MintLeaf else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "borderColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MintLeaf.copy(alpha = 0.08f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "bgColor"
    )
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        ),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Icon
                Icon(
                    imageVector = sourceType.icon,
                    contentDescription = sourceType.label,
                    modifier = Modifier.size(32.dp),
                    tint = if (isSelected) MintLeaf else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(16.dp))

                // Label & description
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sourceType.label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = when (sourceType) {
                            SourceType.PDF -> "Upload bank statements, audit reports"
                            SourceType.URL -> "Paste news article or data page URL"
                            SourceType.CSV -> "Import structured data from POS exports"
                            SourceType.REAL_TIME -> "Connect simulated market data feed"
                            SourceType.GHOST_LEDGER -> "Photograph handwritten Urdu/English ledger"
                            SourceType.GOOGLE_SHEETS -> "Connect your Google Sheets in real-time"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Selection indicator
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MintLeaf),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = InkBlack,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                if (!isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                    )
                }
            }

            // Uploaded Status Info
            if (isSelected && uploadedInfo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(SuccessGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Description, null, Modifier.size(16.dp), tint = SuccessGreen)
                    Spacer(Modifier.width(8.dp))
                    Text(uploadedInfo, style = MaterialTheme.typography.labelSmall, color = SuccessGreen)
                }
            }
        }
    }

    // Upload area (shown when selected)
    AnimatedVisibility(
        visible = isSelected,
        enter = expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)) + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUpload() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Filled.Upload,
                    contentDescription = "Upload",
                    tint = MintLeaf,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (sourceType) {
                        SourceType.PDF -> "Tap to upload PDF"
                        SourceType.URL -> "Paste URL here"
                        SourceType.CSV -> "Tap to upload CSV/JSON"
                        SourceType.REAL_TIME -> "Connect to feed"
                        SourceType.GHOST_LEDGER -> "Open camera to scan"
                        SourceType.GOOGLE_SHEETS -> "Connect Google account"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MintLeaf,
                )
            }
        }
    }
}
