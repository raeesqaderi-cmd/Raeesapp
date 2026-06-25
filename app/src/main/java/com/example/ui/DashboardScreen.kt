package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.MessageEntity
import com.example.ui.theme.AccentPink
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.DeepBlackSpace
import com.example.ui.theme.NeonBlueAccent
import com.example.ui.theme.NeonPurpleAccent
import com.example.ui.theme.PremiumCardBg
import com.example.ui.theme.TextPrimaryWhite
import com.example.ui.theme.TextSecondaryGrey
import com.example.viewmodel.AssistantViewModel
import com.example.voice.SpeechManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()

    // Permission Launchers
    val requestAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setRecordingPermission(isGranted)
    }

    val requestSmsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle SMS state
    }

    // Refresh permissions on resume/start
    LaunchedEffect(Unit) {
        val audioGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setRecordingPermission(audioGranted)

        if (!audioGranted) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // Request SMS receive permission if enabled
        val smsGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!smsGranted) {
            requestSmsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_raeestalk_logo),
                            contentDescription = "RAEESTALK LOGO",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, NeonBlueAccent, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PREMIUM ASSISTANT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonBlueAccent,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "د رئيس ږغ RAEESTALK",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimaryWhite,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    shadow = Shadow(
                                        color = NeonBlueAccent,
                                        offset = Offset(0f, 0f),
                                        blurRadius = 6f
                                    )
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBlackSpace,
                    titleContentColor = TextPrimaryWhite
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear History",
                            tint = TextSecondaryGrey
                        )
                    }
                }
            )
        },
        bottomBar = {
            ImmersiveFooter()
        },
        containerColor = DeepBlackSpace
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepBlackSpace, Color(0xFF070514), DeepBlackSpace)
                    )
                )
                .drawBehind {
                    // Draw beautiful glow/blur radial gradients like in Tailwind "filter blur"
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1B00F0FF), Color.Transparent),
                            radius = size.width * 0.7f,
                            center = Offset(size.width * 0.2f, size.height * 0.35f)
                        ),
                        radius = size.width * 0.7f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x19B026FF), Color.Transparent),
                            radius = size.width * 0.7f,
                            center = Offset(size.width * 0.8f, size.height * 0.55f)
                        ),
                        radius = size.width * 0.7f
                    )
                }
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Pulsing Central Orb Visualizer
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GlowVoiceOrb(
                    speechState = uiState.speechState,
                    rmsdB = uiState.rmsdB,
                    onOrbTap = {
                        val audioGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (audioGranted) {
                            if (uiState.speechState == SpeechManager.SpeechState.LISTENING) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        } else {
                            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }

            // Real-Time Transcript Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TranscriptPanel(
                    speechState = uiState.speechState,
                    inputText = uiState.currentInputText,
                    responseText = uiState.currentResponseText,
                    statusMessage = uiState.statusMessage
                )
            }

            // Interactive Dashboard Control Cards (Collapsible settings panel)
            var showSettings by remember { mutableStateOf(false) }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSettings = !showSettings }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = NeonBlueAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تجهیزات او کنټرول / Quick Control Panel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryWhite
                        )
                    }
                    Text(
                        text = if (showSettings) "پټول / Hide" else "ښودل / Show",
                        fontSize = 12.sp,
                        color = NeonPurpleAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(
                    visible = showSettings,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        ControlTogglesCard(
                            isSmsOn = uiState.isSmsReaderOn,
                            onSmsToggle = { viewModel.toggleSmsReader(it) },
                            isNotifOn = uiState.isNotificationReaderOn,
                            onNotifToggle = { viewModel.toggleNotificationReader(it) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        PermissionsWidgetCard(context = context)
                        Spacer(modifier = Modifier.height(8.dp))
                        ApiKeySecurityWarningCard()
                    }
                }
            }

            // History Logs section
            Text(
                text = "د خبرو اترو تاریخچه / Voice Conversations",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryGrey,
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .weight(1.7f)
                    .fillMaxWidth()
            ) {
                if (history.isEmpty()) {
                    EmptyHistoryPlaceholder()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(history) { message ->
                            HistoryCard(
                                message = message,
                                onPlayTap = { viewModel.speakText(message.response, message.detectedLanguage) },
                                onDeleteTap = { viewModel.deleteMessage(message.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlowVoiceOrb(
    speechState: SpeechManager.SpeechState,
    rmsdB: Float,
    onOrbTap: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    // Breathing pulse scale animation
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    // Fast rotation animation
    val rotationDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    // Dynamic scale matching RMS voice input level
    val inputAmplitudeScale = remember(rmsdB) {
        val ampFactor = (rmsdB.coerceAtLeast(0f) / 10f).coerceAtMost(1.5f)
        1f + ampFactor
    }

    val finalScale = when (speechState) {
        SpeechManager.SpeechState.LISTENING -> breathingScale * inputAmplitudeScale
        SpeechManager.SpeechState.PROCESSING -> breathingScale * 0.9f
        SpeechManager.SpeechState.SPEAKING -> breathingScale * (1f + (breathingScale - 1f) * 1.5f)
        SpeechManager.SpeechState.IDLE -> breathingScale
    }

    val glowBrush = Brush.radialGradient(
        colors = when (speechState) {
            SpeechManager.SpeechState.LISTENING -> listOf(NeonBlueAccent, Color(0x3300F0FF), Color.Transparent)
            SpeechManager.SpeechState.PROCESSING -> listOf(NeonPurpleAccent, Color(0x33B026FF), Color.Transparent)
            SpeechManager.SpeechState.SPEAKING -> listOf(AccentPink, Color(0x33FF2E93), Color.Transparent)
            SpeechManager.SpeechState.IDLE -> listOf(Color(0xFF141126), Color(0x1100F0FF), Color.Transparent)
        }
    )

    Box(
        modifier = Modifier
            .size(190.dp)
            .scale(finalScale)
            .testTag("microphone_pulsing_orb")
            .clip(CircleShape)
            .clickable { onOrbTap() },
        contentAlignment = Alignment.Center
    ) {
        // Glowing Background Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = glowBrush,
                radius = size.minDimension / 1.1f,
                alpha = 0.95f
            )
        }

        // Inner Rings Visualizer
        Canvas(
            modifier = Modifier
                .size(130.dp)
                .scale(if (speechState == SpeechManager.SpeechState.LISTENING) breathingScale else 1f)
        ) {
            drawCircle(
                color = when (speechState) {
                    SpeechManager.SpeechState.LISTENING -> NeonBlueAccent
                    SpeechManager.SpeechState.PROCESSING -> NeonPurpleAccent
                    SpeechManager.SpeechState.SPEAKING -> AccentPink
                    SpeechManager.SpeechState.IDLE -> NeonBlueAccent.copy(alpha = 0.5f)
                },
                radius = size.minDimension / 2.1f,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Microphones logo inside
        Surface(
            modifier = Modifier.size(90.dp),
            shape = CircleShape,
            color = Color(0xFF080614),
            shadowElevation = 8.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Custom metallic gradient frame border
                        drawCircle(
                            brush = Brush.linearGradient(
                                colors = listOf(NeonBlueAccent, NeonPurpleAccent, AccentPink)
                            ),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
            ) {
                if (speechState == SpeechManager.SpeechState.PROCESSING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(54.dp),
                        color = NeonPurpleAccent,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone Button",
                        modifier = Modifier.size(44.dp),
                        tint = when (speechState) {
                            SpeechManager.SpeechState.LISTENING -> NeonBlueAccent
                            SpeechManager.SpeechState.PROCESSING -> NeonPurpleAccent
                            SpeechManager.SpeechState.SPEAKING -> AccentPink
                            SpeechManager.SpeechState.IDLE -> TextPrimaryWhite
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TranscriptPanel(
    speechState: SpeechManager.SpeechState,
    inputText: String,
    responseText: String,
    statusMessage: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transcript_display_panel"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCardBg),
        border = BorderStroke(1.dp, BorderCyan)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when (speechState) {
                                    SpeechManager.SpeechState.LISTENING -> NeonBlueAccent
                                    SpeechManager.SpeechState.PROCESSING -> NeonPurpleAccent
                                    SpeechManager.SpeechState.SPEAKING -> AccentPink
                                    SpeechManager.SpeechState.IDLE -> TextSecondaryGrey
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (speechState) {
                            SpeechManager.SpeechState.LISTENING -> "اوري... / Listening..."
                            SpeechManager.SpeechState.PROCESSING -> "تحلیلوي... / Analyzing..."
                            SpeechManager.SpeechState.SPEAKING -> "غږېږي... / Speaking..."
                            SpeechManager.SpeechState.IDLE -> "بې کاره / Ready"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryWhite
                    )
                }

                if (statusMessage.isNotEmpty()) {
                    Text(
                        text = statusMessage,
                        fontSize = 11.sp,
                        color = NeonPurpleAccent,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // User input transcription
            Column(modifier = Modifier.weight(1f)) {
                if (inputText.isNotEmpty()) {
                    Text(
                        text = "ما اوریدلي / You Said:",
                        fontSize = 10.sp,
                        color = NeonBlueAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "\"$inputText\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimaryWhite,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                }

                // Assistant output
                if (responseText.isNotEmpty()) {
                    Text(
                        text = "د ریس ځواب / President response:",
                        fontSize = 10.sp,
                        color = NeonPurpleAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = responseText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextPrimaryWhite,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                } else if (inputText.isEmpty() && responseText.isEmpty() && speechState == SpeechManager.SpeechState.IDLE) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "رئیس ته د غږیدو لپاره مایک کلیک کړئ\nTap mic to speak to your President Voice AI",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = TextSecondaryGrey,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ControlTogglesCard(
    isSmsOn: Boolean,
    onSmsToggle: (Boolean) -> Unit,
    isNotifOn: Boolean,
    onNotifToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCardBg),
        border = BorderStroke(1.dp, Color(0x11FFFFFF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "د پیغام لوستلو اتوماتیک کنټرول / Auto Message Reader",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonBlueAccent,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // SMS switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "SMS",
                        tint = TextSecondaryGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "د SMS غږیز لوستل", fontSize = 12.sp, color = TextPrimaryWhite, fontWeight = FontWeight.Bold)
                        Text(text = "Automatic incoming SMS TTS reader", fontSize = 10.sp, color = TextSecondaryGrey)
                    }
                }
                Switch(
                    checked = isSmsOn,
                    onCheckedChange = onSmsToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonBlueAccent,
                        checkedTrackColor = Color(0x4400F0FF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notification Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notif",
                        tint = TextSecondaryGrey,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "د خبرتیاوو (Notif) لوستل", fontSize = 12.sp, color = TextPrimaryWhite, fontWeight = FontWeight.Bold)
                        Text(text = "Read WhatsApp/Social notification alerts", fontSize = 10.sp, color = TextSecondaryGrey)
                    }
                }
                Switch(
                    checked = isNotifOn,
                    onCheckedChange = onNotifToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonPurpleAccent,
                        checkedTrackColor = Color(0x44B026FF)
                    )
                )
            }
        }
    }
}

@Composable
fun PermissionsWidgetCard(context: Context) {
    val audioGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    val smsGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCardBg),
        border = BorderStroke(1.dp, Color(0x11FFFFFF))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "د سیستم اجازه لیکونه / Android Permissions Setup",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NeonBlueAccent,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Audio permission item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (audioGranted) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = "Status",
                        tint = if (audioGranted) Color.Green else AccentPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "د مایکروفون لاسرسی / Record Audio Mic",
                        fontSize = 11.sp,
                        color = TextPrimaryWhite
                    )
                }
                if (audioGranted) {
                    Text(text = "فعال / Active", fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = "غیرفعال / Action Required", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                }
            }

            // SMS permission item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (smsGranted) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = "Status",
                        tint = if (smsGranted) Color.Green else AccentPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "د پیغامونو لوستل / Receive Incoming SMS",
                        fontSize = 11.sp,
                        color = TextPrimaryWhite
                    )
                }
                if (smsGranted) {
                    Text(text = "فعال / Active", fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = "غیرفعال / Action Required", fontSize = 11.sp, color = AccentPink, fontWeight = FontWeight.Bold)
                }
            }

            // Notification Access permission (highly required for NotificationListenerService!)
            val hasNotifAccess = isNotificationListenerEnabled(context)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hasNotifAccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = "Status",
                        tint = if (hasNotifAccess) Color.Green else AccentPink,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "خبرتیاو ته غوږ نیول / Notification Access",
                        fontSize = 11.sp,
                        color = TextPrimaryWhite
                    )
                }
                if (hasNotifAccess) {
                    Text(text = "مجاز / Enabled", fontSize = 11.sp, color = Color.Green, fontWeight = FontWeight.Bold)
                } else {
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Log.e("Permissions", "Failed to open settings", e)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(text = "فعالول / Turn On", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ApiKeySecurityWarningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x1FDD2C00)),
        border = BorderStroke(1.dp, Color(0x33DD2C00))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFFF3D00),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "امنیتي خبرداری / API Security Notice",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "د دې پروټوټایپ API کلی په APK کې خوندي شوې. مهرباني وکړئ دا APK په عامه ډول مه خپروئ ترڅو د ناوړه ګټې اخیستنې مخه ونیول شي.\nWarning: API Keys are embedded in this prototype. Do not distribute the APK publicly.",
                    fontSize = 10.sp,
                    color = TextSecondaryGrey
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Empty History",
            modifier = Modifier.size(36.dp),
            tint = TextSecondaryGrey
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "هیڅ غږیز ریکارډ شتون نلري\nNo voice commands logged yet.",
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = TextSecondaryGrey
        )
    }
}

@Composable
fun HistoryCard(
    message: MessageEntity,
    onPlayTap: () -> Unit,
    onDeleteTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumCardBg),
        border = BorderStroke(1.dp, Color(0x0AFFFFFF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header showing action and language details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val actionIcon = when (message.actionExecuted.uppercase()) {
                        "CALL" -> Icons.Default.Call
                        "ALARM" -> Icons.Default.Alarm
                        "MAP" -> Icons.Default.Map
                        "SETTINGS" -> Icons.Default.Settings
                        else -> Icons.Default.Home
                    }
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action Executed",
                        tint = NeonBlueAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (message.actionExecuted == "NONE") "CONVERSATION" else message.actionExecuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonBlueAccent
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0x33B026FF), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = message.detectedLanguage.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurpleAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDeleteTap,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Log",
                            tint = AccentPink.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // User audio request text
            Text(
                text = "سؤال / Prompt: \"${message.request}\"",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            // System speech response
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.response,
                    fontSize = 11.sp,
                    color = TextSecondaryGrey,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onPlayTap,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x1A00F0FF), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play back speech response",
                        tint = NeonBlueAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Helper to check if Notification Listener permission is granted
private fun isNotificationListenerEnabled(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    if (flat != null && flat.isNotEmpty()) {
        val names = flat.split(":")
        for (name in names) {
            val cn = android.content.ComponentName.unflattenFromString(name)
            if (cn != null && cn.packageName == pkgName) {
                return true
            }
        }
    }
    return false
}

@Composable
fun ImmersiveFooter(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_green")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE6050507)) // bg-black/60 backdrop style
            .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .padding(16.dp)
    ) {
        // Upper line with language badges & Active Pipeline status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language badges (PS, DA, EN, AR, AUTO)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("PS", "DA", "EN", "AR", "AUTO").forEach { lang ->
                    val isActive = lang == "AUTO"
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isActive) Color(0x2200F0FF) else Color(0xFF141226),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) NeonBlueAccent.copy(alpha = 0.5f) else Color(0x338B92B2),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = lang,
                            color = if (isActive) NeonBlueAccent else TextSecondaryGrey,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Active Pipeline green pulse status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(1f + (dotAlpha - 0.4f) * 0.4f)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E).copy(alpha = dotAlpha))
                )
                Text(
                    text = "ACTIVE PIPELINE",
                    color = TextSecondaryGrey,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick action grid (Tasks, Voice, System, History)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tasks (Inactive)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(0.5f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = "Tasks",
                        tint = TextPrimaryWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Tasks", fontSize = 9.sp, color = TextPrimaryWhite)
            }

            // Voice (Active)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x2200F0FF), RoundedCornerShape(12.dp))
                        .border(1.dp, NeonBlueAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        tint = NeonBlueAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Voice", fontSize = 9.sp, color = NeonBlueAccent, fontWeight = FontWeight.Bold)
            }

            // System (Inactive)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(0.5f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "System",
                        tint = TextPrimaryWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "System", fontSize = 9.sp, color = TextPrimaryWhite)
            }

            // History (Inactive)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(0.5f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "History",
                        tint = TextPrimaryWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "History", fontSize = 9.sp, color = TextPrimaryWhite)
            }
        }
    }
}
