package com.example.viewmodel

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.CampusBuilding
import com.example.model.CampusHall
import com.example.model.CampusRepository
import com.example.model.HallCategory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.Locale

private const val TAG = "WajehniViewModel"

enum class TacticalScreen {
    CAMPUS_3D,
    DIRECTORY,
    WALKTHROUGH,
    AI_ASSISTANT,
    INSIGHTS
}

enum class AiMode(val arabicTitle: String, val modelId: String, val badgeText: String) {
    FAST_SEARCH(
        arabicTitle = "استعلام سريع وبحث حي",
        modelId = "gemini-3.8-flash",
        badgeText = "Flash 3.8 + Search"
    ),
    HIGH_THINKING(
        arabicTitle = "تفكير عميق وتخطيط تكتيكي",
        modelId = "gemini-4-argon",
        badgeText = "Argon 4 • High Thinking"
    ),
    VOICE_LIVE(
        arabicTitle = "المساعد الصوتي التكتيكي المباشر",
        modelId = "gemini-3.1-flash-tts-preview",
        badgeText = "Live Voice + TTS"
    )
}

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val text: String,
    val modeBadge: String = "",
    val suggestedHallCode: String? = null
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val partialText: String) : AiUiState
    data class Success(val text: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

data class WajehniUiState(
    val currentScreen: TacticalScreen = TacticalScreen.CAMPUS_3D,
    val buildings: List<CampusBuilding> = CampusRepository.buildings,
    val halls: List<CampusHall> = CampusRepository.halls,
    val selectedBuilding: CampusBuilding = CampusRepository.buildings.first(),
    val selectedFloorFilter: Int? = null, // null = All floors, 0 = Ground, 1..4 = Floors
    val searchQuery: String = "",
    val selectedCategory: HallCategory = HallCategory.ALL,
    val onlyWheelchairAccessible: Boolean = false,
    val showIotLiveOverlay: Boolean = true,
    val activeHall: CampusHall = CampusRepository.halls[3], // Default #315 Cyber Security Lab per CUJ
    val currentStepIndex: Int = 0,
    val isContinuousWalkActive: Boolean = false,
    val stepProgressFraction: Float = 0f,
    val corridorYawOffset: Float = 0f,
    val isArCameraMode: Boolean = false,
    val isAerialDropActive: Boolean = false,
    val aerialAltitudeMeters: Int = 280,
    val isRadarPopupOpen: Boolean = false,
    val isVoiceMuted: Boolean = false,
    val lastSpokenBanner: String = "النظام التكتيكي جاهز - اختر قاعة لبدء الهبوط والملاحة",
    val bookedHallCodes: Set<String> = emptySet(),
    val scheduleAlertDismissed: Boolean = false,
    val selectedAiMode: AiMode = AiMode.FAST_SEARCH,
    val aiUiState: AiUiState = AiUiState.Idle,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            id = 1L,
            isUser = false,
            text = "مرحباً بك في مركز القيادة التكتيكي لتطبيق «وجهني». يمكنني إرشادك صوتياً لأي قاعة، أو تخطيط مسارك وجدولك الدراسي بتقنية التفكير العميق (Gemini 4 Argon)، أو الإجابة عن أي استفسار أكاديمي ومكاني.",
            modeBadge = "النظام التكتيكي الذكي",
            suggestedHallCode = "#315"
        )
    )
)

class WajehniViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WajehniUiState())
    val uiState: StateFlow<WajehniUiState> = _uiState.asStateFlow()

    private var aerialDropJob: Job? = null
    private var continuousWalkJob: Job? = null
    private var ttsEngine: TextToSpeech? = null
    private var isTtsReady = false

    // Lazy Firebase AI Logic Models
    private val flashModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val highThinkingArgonModel by lazy {
        Firebase.ai.generativeModel("gemini-4-argon")
    }

    private val structuredRouteSchema = Schema.obj(
        properties = mapOf(
            "tacticalAdvice" to Schema.string(description = "Detailed Arabic tactical navigation and academic advice"),
            "recommendedHallCode" to Schema.string(description = "Recommended hall code such as #402, #208, #101, #315, #204, #G12, or #305"),
            "voiceAnnouncement" to Schema.string(description = "Short Arabic voice announcement for TTS")
        ),
        optionalProperties = listOf("recommendedHallCode")
    )

    private val structuredVoiceModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = structuredRouteSchema
            }
        )
    }

    fun initTts(context: Context) {
        if (ttsEngine != null) return
        ttsEngine = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = ttsEngine?.setLanguage(Locale("ar"))
                isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    fun speakArabic(text: String) {
        _uiState.update { it.copy(lastSpokenBanner = text) }
        if (_uiState.value.isVoiceMuted) return
        if (isTtsReady) {
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wajehni_tactical_utt")
        }
    }

    fun toggleVoiceMute() {
        val nowMuted = !_uiState.value.isVoiceMuted
        _uiState.update { it.copy(isVoiceMuted = nowMuted) }
        if (nowMuted) {
            ttsEngine?.stop()
        } else {
            speakArabic("تم تفعيل المساعد الصوتي التكتيكي باللغة العربية")
        }
    }

    fun navigateToScreen(screen: TacticalScreen) {
        if (screen != TacticalScreen.WALKTHROUGH) {
            stopContinuousWalk()
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun selectBuilding(building: CampusBuilding) {
        _uiState.update { it.copy(selectedBuilding = building) }
    }

    fun selectFloorFilter(floor: Int?) {
        _uiState.update { it.copy(selectedFloorFilter = floor) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategory(category: HallCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleWheelchairFilter() {
        _uiState.update { it.copy(onlyWheelchairAccessible = !it.onlyWheelchairAccessible) }
    }

    fun toggleIotLiveOverlay() {
        _uiState.update { it.copy(showIotLiveOverlay = !it.showIotLiveOverlay) }
    }

    fun setRadarPopupOpen(open: Boolean) {
        _uiState.update { it.copy(isRadarPopupOpen = open) }
    }

    fun toggleArCameraMode() {
        _uiState.update { it.copy(isArCameraMode = !it.isArCameraMode) }
    }

    fun updateCorridorYaw(deltaYaw: Float) {
        _uiState.update { state ->
            val updated = (state.corridorYawOffset + deltaYaw).coerceIn(-85f, 85f)
            state.copy(corridorYawOffset = updated)
        }
    }

    fun resetCorridorYaw() {
        _uiState.update { it.copy(corridorYawOffset = 0f) }
    }

    fun dismissScheduleAlert() {
        _uiState.update { it.copy(scheduleAlertDismissed = true) }
    }

    fun bookHall(hallCode: String) {
        _uiState.update { state ->
            val updatedBooked = state.bookedHallCodes + hallCode
            val updatedHalls = state.halls.map { hall ->
                if (hall.code == hallCode) {
                    hall.copy(
                        isAvailableNow = false,
                        liveStatusText = "محجوز لك الآن (مؤكد)"
                    )
                } else {
                    hall
                }
            }
            val updatedActive = updatedHalls.find { it.code == state.activeHall.code } ?: state.activeHall
            state.copy(
                bookedHallCodes = updatedBooked,
                halls = updatedHalls,
                activeHall = updatedActive
            )
        }
        speakArabic("تم تأكيد حجز القاعة $hallCode بنجاح في النظام الأكاديمي.")
    }

    /**
     * Launches the Cinematic Aerial Tactical Drop from 280m down to 12m,
     * then transitions automatically into the First-Person Indoor 3D Walkthrough.
     */
    fun launchAerialDropAndNavigate(hall: CampusHall) {
        stopContinuousWalk()
        aerialDropJob?.cancel()
        val targetBuilding = _uiState.value.buildings.find { it.id == hall.buildingId }
            ?: _uiState.value.selectedBuilding

        _uiState.update {
            it.copy(
                activeHall = hall,
                selectedBuilding = targetBuilding,
                currentStepIndex = 0,
                stepProgressFraction = 0f,
                corridorYawOffset = 0f,
                isAerialDropActive = true,
                aerialAltitudeMeters = 280
            )
        }

        speakArabic("بدء الهبوط السينمائي التكتيكي نحو ${hall.officialName} في ${targetBuilding.shortTitle}.")

        aerialDropJob = viewModelScope.launch {
            val startAlt = 280
            val endAlt = 12
            val totalTicks = 34
            for (tick in 1..totalTicks) {
                delay(55L)
                val fraction = tick.toFloat() / totalTicks
                // Smooth ease-out descent curve
                val eased = 1f - (1f - fraction) * (1f - fraction)
                val currentAlt = (startAlt - ((startAlt - endAlt) * eased)).toInt()
                _uiState.update { it.copy(aerialAltitudeMeters = currentAlt) }
            }
            delay(320L)
            completeAerialDropToWalkthrough()
        }
    }

    fun completeAerialDropToWalkthrough() {
        aerialDropJob?.cancel()
        val hall = _uiState.value.activeHall
        val firstStep = hall.steps.firstOrNull()
        _uiState.update {
            it.copy(
                isAerialDropActive = false,
                aerialAltitudeMeters = 12,
                currentScreen = TacticalScreen.WALKTHROUGH,
                currentStepIndex = 0,
                stepProgressFraction = 0f
            )
        }
        if (firstStep != null) {
            speakArabic(firstStep.voiceGuidanceArabic)
        }
    }

    fun selectWalkthroughStep(index: Int) {
        val hall = _uiState.value.activeHall
        if (index in hall.steps.indices) {
            _uiState.update {
                it.copy(
                    currentStepIndex = index,
                    stepProgressFraction = 0f,
                    corridorYawOffset = 0f
                )
            }
            speakArabic(hall.steps[index].voiceGuidanceArabic)
        }
    }

    fun nextWalkthroughStep() {
        val state = _uiState.value
        val nextIdx = state.currentStepIndex + 1
        if (nextIdx in state.activeHall.steps.indices) {
            selectWalkthroughStep(nextIdx)
        }
    }

    fun previousWalkthroughStep() {
        val state = _uiState.value
        val prevIdx = state.currentStepIndex - 1
        if (prevIdx in state.activeHall.steps.indices) {
            selectWalkthroughStep(prevIdx)
        }
    }

    fun toggleContinuousWalkSimulation() {
        if (_uiState.value.isContinuousWalkActive) {
            stopContinuousWalk()
        } else {
            startContinuousWalk()
        }
    }

    private fun startContinuousWalk() {
        continuousWalkJob?.cancel()
        val state = _uiState.value
        val startIndex = if (state.currentStepIndex >= state.activeHall.steps.lastIndex) 0 else state.currentStepIndex
        _uiState.update {
            it.copy(
                isContinuousWalkActive = true,
                currentStepIndex = startIndex,
                stepProgressFraction = 0f
            )
        }
        val initialStep = _uiState.value.activeHall.steps.getOrNull(startIndex)
        if (initialStep != null) {
            speakArabic(initialStep.voiceGuidanceArabic)
        }

        continuousWalkJob = viewModelScope.launch {
            while (_uiState.value.isContinuousWalkActive) {
                delay(65L)
                val current = _uiState.value
                val nextFraction = current.stepProgressFraction + 0.018f
                if (nextFraction >= 1f) {
                    val nextStepIndex = current.currentStepIndex + 1
                    if (nextStepIndex < current.activeHall.steps.size) {
                        val nextStep = current.activeHall.steps[nextStepIndex]
                        _uiState.update {
                            it.copy(
                                currentStepIndex = nextStepIndex,
                                stepProgressFraction = 0f
                            )
                        }
                        speakArabic(nextStep.voiceGuidanceArabic)
                    } else {
                        _uiState.update {
                            it.copy(
                                isContinuousWalkActive = false,
                                stepProgressFraction = 1f
                            )
                        }
                        break
                    }
                } else {
                    _uiState.update { it.copy(stepProgressFraction = nextFraction) }
                }
            }
        }
    }

    fun stopContinuousWalk() {
        continuousWalkJob?.cancel()
        _uiState.update { it.copy(isContinuousWalkActive = false) }
    }

    fun selectAiMode(mode: AiMode) {
        _uiState.update { it.copy(selectedAiMode = mode) }
    }

    fun sendAiPrompt(userPrompt: String) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty()) return

        val mode = _uiState.value.selectedAiMode
        val userMsg = ChatMessage(
            isUser = true,
            text = trimmed,
            modeBadge = mode.badgeText
        )
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                aiUiState = AiUiState.Loading
            )
        }

        val campusContext = buildCampusSystemPrompt()
        val fullPrompt = "$campusContext\n\nسؤال المستخدم: $trimmed"

        viewModelScope.launch {
            try {
                when (mode) {
                    AiMode.VOICE_LIVE -> {
                        val response = withTimeout(60_000L) {
                            structuredVoiceModel.generateContent(fullPrompt)
                        }
                        val rawJson = response.text ?: throw IllegalStateException("رد فارغ من النموذج.")
                        val json = JSONObject(rawJson)
                        val advice = json.optString("tacticalAdvice", rawJson)
                        val hallCode = json.optString("recommendedHallCode", "").takeIf { it.isNotBlank() }
                        val voiceText = json.optString("voiceAnnouncement", advice)

                        val botMsg = ChatMessage(
                            isUser = false,
                            text = advice,
                            modeBadge = mode.badgeText,
                            suggestedHallCode = hallCode
                        )
                        _uiState.update {
                            it.copy(
                                chatMessages = it.chatMessages + botMsg,
                                aiUiState = AiUiState.Success(advice)
                            )
                        }
                        speakArabic(voiceText)
                    }

                    AiMode.HIGH_THINKING -> {
                        val accumulated = StringBuilder()
                        withTimeout(60_000L) {
                            highThinkingArgonModel.generateContentStream(
                                "$fullPrompt\nقم بتحليل المسارات البديلة، حالة الازدحام، وتقديم خطة تنقل تكتيكية شاملة خطوة بخطوة."
                            ).collect { chunk ->
                                accumulated.append(chunk.text.orEmpty())
                                _uiState.update {
                                    it.copy(aiUiState = AiUiState.Streaming(accumulated.toString()))
                                }
                            }
                        }
                        val finalAnswer = accumulated.toString().ifBlank { "تم تحليل المسار التكتيكي بنجاح." }
                        val detectedHall = detectHallCodeInText(finalAnswer)
                        val botMsg = ChatMessage(
                            isUser = false,
                            text = finalAnswer,
                            modeBadge = mode.badgeText,
                            suggestedHallCode = detectedHall
                        )
                        _uiState.update {
                            it.copy(
                                chatMessages = it.chatMessages + botMsg,
                                aiUiState = AiUiState.Success(finalAnswer)
                            )
                        }
                    }

                    AiMode.FAST_SEARCH -> {
                        val accumulated = StringBuilder()
                        withTimeout(60_000L) {
                            flashModel.generateContentStream(fullPrompt).collect { chunk ->
                                accumulated.append(chunk.text.orEmpty())
                                _uiState.update {
                                    it.copy(aiUiState = AiUiState.Streaming(accumulated.toString()))
                                }
                            }
                        }
                        val finalAnswer = accumulated.toString().ifBlank { "إليك تفاصيل القاعة والمسار المطلوب." }
                        val detectedHall = detectHallCodeInText(finalAnswer)
                        val botMsg = ChatMessage(
                            isUser = false,
                            text = finalAnswer,
                            modeBadge = mode.badgeText,
                            suggestedHallCode = detectedHall
                        )
                        _uiState.update {
                            it.copy(
                                chatMessages = it.chatMessages + botMsg,
                                aiUiState = AiUiState.Success(finalAnswer)
                            )
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI generation timed out", e)
                _uiState.update {
                    it.copy(aiUiState = AiUiState.Error("انتهت مهلة الاتصال بعد 60 ثانية. يرجى المحاولة مرة أخرى."))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI generation failed", e)
                _uiState.update {
                    it.copy(aiUiState = AiUiState.Error(e.localizedMessage ?: "تعذر إتمام الطلب الذكي."))
                }
            }
        }
    }

    private fun detectHallCodeInText(text: String): String? {
        val codes = listOf("#402", "#208", "#101", "#315", "#204", "#G12", "#305")
        return codes.firstOrNull { code ->
            text.contains(code) || text.contains(code.removePrefix("#"))
        }
    }

    private fun buildCampusSystemPrompt(): String {
        val hallsSummary = _uiState.value.halls.joinToString("\n") { h ->
            "- ${h.code} (${h.officialName}): ${h.buildingName}، ${h.floorLabel}، السعة: ${h.capacityLabel}، الحالة: ${h.liveStatusText}، المحاضر: ${h.currentLecturer}"
        }
        return """
            أنت المساعد التكتيكي الذكي لتطبيق «وجهني» للملاحة ثلاثية الأبعاد في الحرم الجامعي.
            أجب باللغة العربية الفصحى بأسلوب تكتيكي واضح ودقيق وموجز.
            بيانات الحرم الجامعي الحالية:
            المباني الخمسة:
            1. مبنى 1: كلية الهندسة والعلوم التطبيقية (4 طوابق، كود فيروزي)
            2. مبنى 2: كلية الحاسبات والذكاء الاصطناعي (4 طوابق، كود أخضر نيون)
            3. مبنى 3: كلية إدارة الأعمال والابتكار (3 طوابق، كود ذهبي)
            4. مبنى 4: المكتبة الرقمية المركزية والمخطوطات (طابقان، كود أزرق محيطي)
            5. مبنى 5: مركز الابتكار وحاضنة المشاريع (3 طوابق، كود بنفسجي نيون)
            القاعات النموذجية وحالتها اللحظية:
            $hallsSummary
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        ttsEngine?.stop()
        ttsEngine?.shutdown()
    }
}
