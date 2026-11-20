package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlightLand
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.CampusHall
import com.example.ui.theme.InnovationPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalCrimson
import com.example.ui.theme.TacticalDeepNavy
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalObsidian
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.viewmodel.AiMode
import com.example.viewmodel.AiUiState
import com.example.viewmodel.ChatMessage

@Composable
fun AiTacticalAssistantScreen(
    selectedMode: AiMode,
    aiUiState: AiUiState,
    messages: List<ChatMessage>,
    halls: List<CampusHall>,
    onSelectMode: (AiMode) -> Unit,
    onSendPrompt: (String) -> Unit,
    onSpeakText: (String) -> Unit,
    onLaunchHallDrop: (CampusHall) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isListeningMic by remember { mutableStateOf(false) }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListeningMic = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListeningMic = false
            }
            override fun onError(error: Int) {
                isListeningMic = false
            }
            override fun onResults(results: Bundle?) {
                isListeningMic = false
                val spoken = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!spoken.isNullOrBlank()) {
                    inputText = spoken
                    onSendPrompt(spoken)
                    inputText = ""
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer?.setRecognitionListener(listener)
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            }
            speechRecognizer.startListening(intent)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalObsidian)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("ai_tactical_assistant_screen")
    ) {
        // Header & AI Mode Switcher (Flash 3.8 + Search | Argon 4 High Thinking | Live Voice + TTS)
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = TacticalDeepNavy,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "المساعد التكتيكي والصوتي الذكي (Gemini AI)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TacticalTextPrimary
                        )
                        Text(
                            text = "ملاحة صوتية بالعربية الفصحى • تفكير عميق Argon 4 • استعلام فوري",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NeonEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    AiMode.entries.forEach { mode ->
                        val isSelected = mode == selectedMode
                        val accent = when (mode) {
                            AiMode.FAST_SEARCH -> NeonCyan
                            AiMode.HIGH_THINKING -> InnovationPurple
                            AiMode.VOICE_LIVE -> NeonEmerald
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) accent.copy(alpha = 0.22f) else TacticalCardSurface,
                            modifier = Modifier
                                .border(1.dp, if (isSelected) accent else accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .clickable { onSelectMode(mode) }
                                .testTag("ai_mode_chip_${mode.name}")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = mode.arabicTitle,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isSelected) accent else TacticalTextPrimary
                                )
                                Text(
                                    text = mode.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TacticalTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Tactical Prompt Chips
        val quickPrompts = listOf(
            "أرشدني صوتياً إلى مختبر الأمن السيبراني #315",
            "خطط لي مساراً ذكياً من مبنى 1 إلى معمل الذكاء الاصطناعي #208",
            "ما القاعات المتاحة الآن للحجز المجاني أو البحث الرقمي؟",
            "اقترح أفضل مسار مجهز لذوي الإعاقة نحو قاعة المؤتمرات #402"
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            quickPrompts.forEachIndexed { idx, prompt ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = TacticalCardSurface,
                    modifier = Modifier
                        .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { onSendPrompt(prompt) }
                        .testTag("quick_ai_prompt_$idx")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TacticalGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TacticalTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Messages Stream
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val alignment = if (msg.isUser) Alignment.CenterStart else Alignment.CenterEnd
                Box(
                    contentAlignment = alignment,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (msg.isUser) NeonCyan.copy(alpha = 0.16f) else TacticalDeepNavy,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .border(
                                1.dp,
                                if (msg.isUser) NeonCyan.copy(alpha = 0.6f) else NeonEmerald.copy(alpha = 0.45f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (msg.isUser) "أنت • استعلام ميداني" else "وجهني AI • ${msg.modeBadge}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (msg.isUser) NeonCyan else NeonEmerald
                                )
                                if (!msg.isUser) {
                                    IconButton(
                                        onClick = { onSpeakText(msg.text) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = "نطق الرد",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TacticalTextPrimary
                            )

                            val matchedHall = msg.suggestedHallCode?.let { code ->
                                halls.find { it.code.equals(code, ignoreCase = true) }
                            }
                            if (matchedHall != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onLaunchHallDrop(matchedHall) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan,
                                        contentColor = TacticalObsidian
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FlightLand,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "هبوط وملاحة نحو ${matchedHall.code} (${matchedHall.officialName})",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Streaming / Loading / Error State Indicator
            when (aiUiState) {
                is AiUiState.Loading -> {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TacticalDeepNavy)
                                .padding(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = NeonCyan,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "جاري التحليل التكتيكي عبر ${selectedMode.badgeText}...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan
                            )
                        }
                    }
                }
                is AiUiState.Streaming -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = TacticalDeepNavy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, InnovationPurple, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "بث مباشر • ${selectedMode.badgeText}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = InnovationPurple
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = aiUiState.partialText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TacticalTextPrimary
                                )
                            }
                        }
                    }
                }
                is AiUiState.Error -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = TacticalCrimson.copy(alpha = 0.16f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, TacticalCrimson, RoundedCornerShape(14.dp))
                        ) {
                            Text(
                                text = aiUiState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TacticalTextPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Row with Voice Conversation Mic + Text Input + Send
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    val hasAudioPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasAudioPerm) {
                        if (isListeningMic) {
                            speechRecognizer?.stopListening()
                            isListeningMic = false
                        } else if (speechRecognizer != null) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                            }
                            speechRecognizer.startListening(intent)
                        }
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListeningMic) TacticalCrimson else TacticalCardSurface)
                    .border(1.dp, NeonEmerald, CircleShape)
                    .testTag("voice_conversation_mic_button")
            ) {
                Icon(
                    imageVector = if (isListeningMic) Icons.Default.StopCircle else Icons.Default.Mic,
                    contentDescription = "تحدث صوتياً مع المساعد التكتيكي",
                    tint = if (isListeningMic) TacticalTextPrimary else NeonEmerald
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "اسأل المساعد الصوتي أو اطلب خطة مسار...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TacticalTextSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = NeonCyan.copy(alpha = 0.35f),
                    focusedContainerColor = TacticalDeepNavy,
                    unfocusedContainerColor = TacticalDeepNavy,
                    focusedTextColor = TacticalTextPrimary,
                    unfocusedTextColor = TacticalTextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input")
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendPrompt(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = TacticalObsidian
                )
            }
        }
    }
}
