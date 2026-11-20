package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.InnovationPurple
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.TacticalGold

enum class StepStage(val arabicLabel: String) {
    EXTERIOR("مسار خارجي"),
    VERTICAL("انتقال رأسي"),
    CORRIDOR("ممر داخلي"),
    ARRIVAL("نقطة الوصول")
}

data class NavigationStep(
    val stepIndex: Int,
    val stage: StepStage,
    val title: String,
    val instruction: String,
    val voiceGuidanceArabic: String,
    val distanceMeters: Int,
    val landmarkIconType: String, // "GATE", "ELEVATOR", "CORRIDOR", "ROOM"
    val targetTurnAngle: Float = 0f, // -45 left, 0 straight, +45 right
    val sideDoorsInSegment: List<String> = emptyList()
)

enum class HallCategory(val label: String) {
    ALL("الكل"),
    CONFERENCES("مؤتمرات"),
    TECH_AI("حاسب وتقنية"),
    AUDITORIUMS("مدرجات كبرى"),
    FINANCE_ADMIN("مالية وإدارة"),
    DIGITAL_RESEARCH("بحث رقمي")
}

data class CampusBuilding(
    val id: Int,
    val shortTitle: String,
    val officialName: String,
    val floorsCount: Int,
    val entrancesCount: Int,
    val colorName: String,
    val tacticalColor: Color,
    val description: String,
    // Relative 3D grid coordinates (-1.0f to 1.0f) and footprint dimensions
    val gridX: Float,
    val gridZ: Float,
    val widthUnits: Float,
    val depthUnits: Float,
    val radarAngleDeg: Float,
    val radarDistanceRatio: Float,
    val energySavingPercent: Int,
    val crowdLoadPercent: Int
)

data class CampusHall(
    val code: String,          // e.g. "#402"
    val rawNumber: String,     // e.g. "402"
    val officialName: String,
    val buildingId: Int,
    val buildingName: String,
    val floorNumber: Int,      // 0 = Ground, 1..4 = Floors
    val floorLabel: String,
    val capacityLabel: String,
    val category: HallCategory,
    val isAvailableNow: Boolean,
    val liveStatusText: String,
    val currentLecturer: String,
    val currentTopic: String,
    val isWheelchairAccessible: Boolean,
    val bleBeaconId: String,
    val uwbPrecisionMeters: Float,
    val radarAzimuthDeg: Float,
    val steps: List<NavigationStep>,
    val specialtyKeywords: List<String>
)

data class FeasibilityMetric(
    val id: String,
    val title: String,
    val subtitle: String,
    val highlightValue: String,
    val description: String,
    val progressRatio: Float,
    val accentColor: Color
)

data class RoadmapMilestone(
    val number: String,
    val title: String,
    val englishSubtitle: String,
    val bulletPoints: List<String>,
    val statusBadge: String,
    val accentColor: Color
)

object CampusRepository {

    val buildings: List<CampusBuilding> = listOf(
        CampusBuilding(
            id = 1,
            shortTitle = "مبنى 1",
            officialName = "مبنى 1",
            floorsCount = 4,
            entrancesCount = 2,
            colorName = "فيروزي تكتيكي",
            tacticalColor = NeonCyan,
            description = "مبنى 1 في الحرم الجامعي",
            gridX = -0.52f,
            gridZ = -0.35f,
            widthUnits = 0.36f,
            depthUnits = 0.28f,
            radarAngleDeg = 315f,
            radarDistanceRatio = 0.62f,
            energySavingPercent = 34,
            crowdLoadPercent = 42
        ),
        CampusBuilding(
            id = 2,
            shortTitle = "مبنى 2",
            officialName = "مبنى 2",
            floorsCount = 4,
            entrancesCount = 2,
            colorName = "أخضر نيون",
            tacticalColor = NeonEmerald,
            description = "مبنى 2 في الحرم الجامعي",
            gridX = 0.48f,
            gridZ = -0.32f,
            widthUnits = 0.36f,
            depthUnits = 0.28f,
            radarAngleDeg = 45f,
            radarDistanceRatio = 0.58f,
            energySavingPercent = 41,
            crowdLoadPercent = 55
        ),
        CampusBuilding(
            id = 3,
            shortTitle = "مبنى 3",
            officialName = "مبنى 3",
            floorsCount = 3,
            entrancesCount = 2,
            colorName = "ذهبي تكتيكي",
            tacticalColor = TacticalGold,
            description = "مبنى 3 في الحرم الجامعي",
            gridX = -0.50f,
            gridZ = 0.38f,
            widthUnits = 0.34f,
            depthUnits = 0.26f,
            radarAngleDeg = 225f,
            radarDistanceRatio = 0.65f,
            energySavingPercent = 29,
            crowdLoadPercent = 38
        ),
        CampusBuilding(
            id = 4,
            shortTitle = "مبنى 4",
            officialName = "مبنى 4",
            floorsCount = 2,
            entrancesCount = 2,
            colorName = "أزرق محيطي",
            tacticalColor = OceanBlue,
            description = "مبنى 4 في الحرم الجامعي",
            gridX = 0.50f,
            gridZ = 0.38f,
            widthUnits = 0.38f,
            depthUnits = 0.26f,
            radarAngleDeg = 135f,
            radarDistanceRatio = 0.60f,
            energySavingPercent = 48,
            crowdLoadPercent = 24
        ),
        CampusBuilding(
            id = 5,
            shortTitle = "مبنى 5",
            officialName = "مبنى 5",
            floorsCount = 3,
            entrancesCount = 3,
            colorName = "بنفسجي نيون",
            tacticalColor = InnovationPurple,
            description = "مبنى 5 في الحرم الجامعي",
            gridX = 0.0f,
            gridZ = 0.02f,
            widthUnits = 0.32f,
            depthUnits = 0.30f,
            radarAngleDeg = 12f,
            radarDistanceRatio = 0.28f,
            energySavingPercent = 39,
            crowdLoadPercent = 31
        ),
        CampusBuilding(
            id = 6,
            shortTitle = "مبنى 6",
            officialName = "مبنى 6",
            floorsCount = 4,
            entrancesCount = 2,
            colorName = "سماوي تكتيكي",
            tacticalColor = NeonCyan,
            description = "مبنى 6 في الحرم الجامعي",
            gridX = -0.85f,
            gridZ = -0.35f,
            widthUnits = 0.32f,
            depthUnits = 0.26f,
            radarAngleDeg = 300f,
            radarDistanceRatio = 0.85f,
            energySavingPercent = 32,
            crowdLoadPercent = 46
        ),
        CampusBuilding(
            id = 7,
            shortTitle = "مبنى 7",
            officialName = "مبنى 7",
            floorsCount = 5,
            entrancesCount = 3,
            colorName = "أخضر زمردي",
            tacticalColor = NeonEmerald,
            description = "مبنى 7 في الحرم الجامعي",
            gridX = 0.85f,
            gridZ = -0.32f,
            widthUnits = 0.35f,
            depthUnits = 0.28f,
            radarAngleDeg = 60f,
            radarDistanceRatio = 0.82f,
            energySavingPercent = 37,
            crowdLoadPercent = 62
        ),
        CampusBuilding(
            id = 8,
            shortTitle = "مبنى 8",
            officialName = "مبنى 8",
            floorsCount = 3,
            entrancesCount = 4,
            colorName = "كهرماني تكتيكي",
            tacticalColor = TacticalGold,
            description = "مبنى 8 في الحرم الجامعي",
            gridX = -0.85f,
            gridZ = 0.38f,
            widthUnits = 0.32f,
            depthUnits = 0.26f,
            radarAngleDeg = 240f,
            radarDistanceRatio = 0.85f,
            energySavingPercent = 28,
            crowdLoadPercent = 58
        ),
        CampusBuilding(
            id = 9,
            shortTitle = "مبنى 9",
            officialName = "مبنى 9",
            floorsCount = 2,
            entrancesCount = 4,
            colorName = "وردي تكتيكي",
            tacticalColor = InnovationPurple,
            description = "مبنى 9 في الحرم الجامعي",
            gridX = 0.85f,
            gridZ = 0.38f,
            widthUnits = 0.40f,
            depthUnits = 0.30f,
            radarAngleDeg = 120f,
            radarDistanceRatio = 0.84f,
            energySavingPercent = 45,
            crowdLoadPercent = 20
        ),
        CampusBuilding(
            id = 10,
            shortTitle = "مبنى 10",
            officialName = "مبنى 10",
            floorsCount = 2,
            entrancesCount = 2,
            colorName = "نيلي تكتيكي",
            tacticalColor = OceanBlue,
            description = "مبنى 10 في الحرم الجامعي",
            gridX = -0.50f,
            gridZ = 0.82f,
            widthUnits = 0.38f,
            depthUnits = 0.28f,
            radarAngleDeg = 200f,
            radarDistanceRatio = 0.90f,
            energySavingPercent = 30,
            crowdLoadPercent = 35
        ),
        CampusBuilding(
            id = 11,
            shortTitle = "مبنى 11",
            officialName = "مبنى 11",
            floorsCount = 3,
            entrancesCount = 2,
            colorName = "تيل تكتيكي",
            tacticalColor = NeonCyan,
            description = "مبنى 11 في الحرم الجامعي",
            gridX = 0.50f,
            gridZ = 0.82f,
            widthUnits = 0.34f,
            depthUnits = 0.26f,
            radarAngleDeg = 160f,
            radarDistanceRatio = 0.88f,
            energySavingPercent = 65,
            crowdLoadPercent = 22
        ),
        CampusBuilding(
            id = 12,
            shortTitle = "مبنى 12",
            officialName = "مبنى 12",
            floorsCount = 4,
            entrancesCount = 2,
            colorName = "بنفسجي تكتيكي",
            tacticalColor = InnovationPurple,
            description = "مبنى 12 في الحرم الجامعي",
            gridX = 0.0f,
            gridZ = 0.80f,
            widthUnits = 0.36f,
            depthUnits = 0.28f,
            radarAngleDeg = 180f,
            radarDistanceRatio = 0.80f,
            energySavingPercent = 42,
            crowdLoadPercent = 40
        )
    )

    val halls: List<CampusHall> = listOf(
        CampusHall(
            code = "#402",
            rawNumber = "402",
            officialName = "قاعة الشيخ صالح الفهد للمؤتمرات",
            buildingId = 1,
            buildingName = "مبنى 1",
            floorNumber = 4,
            floorLabel = "الدور الرابع",
            capacityLabel = "250 مقعداً",
            category = HallCategory.CONFERENCES,
            isAvailableNow = true,
            liveStatusText = "متاح الآن (حجز مجاني)",
            currentLecturer = "متاحة للحجز الفوري والندوات",
            currentTopic = "ملتقى الهندسة التطبيقية والأنظمة الذكية",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-ENG-402A",
            uwbPrecisionMeters = 0.35f,
            radarAzimuthDeg = 318f,
            specialtyKeywords = listOf("402", "مؤتمرات", "صالح الفهد", "هندسة", "ندوات"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "البوابة الشرقية لمبنى الهندسة",
                    instruction = "الوصول إلى البوابة الشرقية لمبنى الهندسة والعلوم التطبيقية (مسافة 35م)",
                    voiceGuidanceArabic = "ابدأ السير للأمام لمسافة خمسة وثلاثين متراً نحو البوابة الشرقية لمبنى الهندسة رقم واحد.",
                    distanceMeters = 35,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-E1", "#101", "#102")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "المصعد البانورامي الرئيسي",
                    instruction = "ركوب المصعد الكهربائي السريع رقم 1 للصعود إلى الدور الرابع",
                    voiceGuidanceArabic = "بعد عشرة أمتار، اتجه نحو المصعد البانورامي رقم واحد واصعد إلى الدور الرابع.",
                    distanceMeters = 15,
                    landmarkIconType = "ELEVATOR",
                    targetTurnAngle = -20f,
                    sideDoorsInSegment = listOf("ELEV-1", "ELEV-2", "STAIRS-A")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "ممر المؤتمرات العلوي",
                    instruction = "السير في الممر الشرقي الفيروزي لمسافة 20م مع تتبع أشعة الليزر الأرضية",
                    voiceGuidanceArabic = "اسلك الممر الشرقي في الدور الرابع لمسافة عشرين متراً وتابع الأسهم النيونية المتدفقة.",
                    distanceMeters = 20,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#400", "#401", "#402")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى قاعة #402",
                    instruction = "الوصول إلى قاعة الشيخ صالح الفهد للمؤتمرات #402 على اليسار - نهاية المسار",
                    voiceGuidanceArabic = "لقد وصلت إلى وجهتك، قاعة الشيخ صالح الفهد للمؤتمرات رقم أربعمئة واثنين على يسارك.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = -35f,
                    sideDoorsInSegment = listOf("#401", "#402")
                )
            )
        ),
        CampusHall(
            code = "#208",
            rawNumber = "208",
            officialName = "معمل الذكاء الاصطناعي والروبوتات",
            buildingId = 2,
            buildingName = "مبنى 2",
            floorNumber = 2,
            floorLabel = "الدور الثاني",
            capacityLabel = "40 محطة",
            category = HallCategory.TECH_AI,
            isAvailableNow = false,
            liveStatusText = "قيد الاستخدام حتى 11:30",
            currentLecturer = "أ.د. طارق المنصور",
            currentTopic = "الشبكات العصبية العميقة والروبوتات المستقلة",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-AI-208X",
            uwbPrecisionMeters = 0.40f,
            radarAzimuthDeg = 48f,
            specialtyKeywords = listOf("208", "ذكاء اصطناعي", "روبوتات", "حاسب", "تقنية", "معمل"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "البوابة الرئيسية لكلية الحاسبات",
                    instruction = "الوصول إلى البوابة الشمالية لمبنى الحاسبات والذكاء الاصطناعي (مسافة 30م)",
                    voiceGuidanceArabic = "اتجه للأمام لمسافة ثلاثين متراً نحو البوابة الشمالية لمبنى الحاسبات والذكاء الاصطناعي.",
                    distanceMeters = 30,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-N2", "#G04", "#G05")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "المصعد الذكي رقم 2",
                    instruction = "استخدام المصعد الكهربائي رقم 2 للصعود إلى الدور الثاني",
                    voiceGuidanceArabic = "بعد خمسة عشر متراً، اتجه يميناً نحو المصعد الذكي رقم اثنين للصعود إلى الدور الثاني.",
                    distanceMeters = 15,
                    landmarkIconType = "ELEVATOR",
                    targetTurnAngle = 25f,
                    sideDoorsInSegment = listOf("ELEV-2", "STAIRS-B")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "جناح معامل الذكاء الاصطناعي",
                    instruction = "السير في ممر المختبرات الأخضر لمسافة 22م باتجاه جناح الروبوتات",
                    voiceGuidanceArabic = "سر للأمام في جناح معامل الذكاء الاصطناعي لمسافة اثنين وعشرين متراً.",
                    distanceMeters = 22,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#205", "#206", "#207", "#208")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى معمل #208",
                    instruction = "الوصول إلى معمل الذكاء الاصطناعي والروبوتات #208 على اليمين - نهاية المسار",
                    voiceGuidanceArabic = "لقد وصلت إلى معمل الذكاء الاصطناعي والروبوتات رقم مئتين وثمانية على يمينك.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = 35f,
                    sideDoorsInSegment = listOf("#207", "#208")
                )
            )
        ),
        CampusHall(
            code = "#101",
            rawNumber = "101",
            officialName = "مدرج د. أحمد زويل العام",
            buildingId = 1,
            buildingName = "مبنى 1",
            floorNumber = 0,
            floorLabel = "الدور الأرضي",
            capacityLabel = "320 مقعداً",
            category = HallCategory.AUDITORIUMS,
            isAvailableNow = true,
            liveStatusText = "متاح الآن (محاضرة 10:00)",
            currentLecturer = "د. خالد السبيعي",
            currentTopic = "فيزياء النانو وتطبيقات الطاقة المتجددة",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-ENG-101G",
            uwbPrecisionMeters = 0.30f,
            radarAzimuthDeg = 305f,
            specialtyKeywords = listOf("101", "مدرج", "أحمد زويل", "مدرجات كبرى", "هندسة", "عام"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "الساحة المركزية إلى مبنى 1",
                    instruction = "التقدم عبر الساحة المركزية إلى المدخل الأرضي المجهز بالمنحدرات (مسافة 25م)",
                    voiceGuidanceArabic = "تقدم عبر الساحة المركزية لمسافة خمسة وعشرين متراً نحو المدخل الأرضي لمبنى الهندسة.",
                    distanceMeters = 25,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-M1", "INFO-1")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "بهو الدور الأرضي المباشر",
                    instruction = "العبور عبر البهو الأرضي المفتوح دون الحاجة لسلالم أو مصاعد",
                    voiceGuidanceArabic = "اعبر البهو الأرضي المفتوح مباشرة، المسار مستوٍ ومجهز بالكامل.",
                    distanceMeters = 12,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("LOBBY-1", "#100")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "ممر المدرجات الكبرى",
                    instruction = "السير في الممر الغربي العريض لمسافة 18م نحو البوابة المزدوجة للمدرج",
                    voiceGuidanceArabic = "استمر في السير عبر ممر المدرجات الكبرى لمسافة ثمانية عشر متراً.",
                    distanceMeters = 18,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = -15f,
                    sideDoorsInSegment = listOf("#100", "#101-A", "#101")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى المدرج #101",
                    instruction = "الوصول إلى مدرج د. أحمد زويل العام #101 أمامك مباشرة - نهاية المسار",
                    voiceGuidanceArabic = "وصلت إلى مدرج الدكتور أحمد زويل العام رقم مئة وواحد.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#101")
                )
            )
        ),
        CampusHall(
            code = "#315",
            rawNumber = "315",
            officialName = "مختبر الأمن السيبراني واختبار الاختراق",
            buildingId = 2,
            buildingName = "مبنى 2",
            floorNumber = 3,
            floorLabel = "الدور الثالث",
            capacityLabel = "35 محطة",
            category = HallCategory.TECH_AI,
            isAvailableNow = true,
            liveStatusText = "متاح الآن",
            currentLecturer = "د. فهد العتيبي",
            currentTopic = "تحليل الثغرات والدفاع السيبراني المتقدم",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-SEC-315C",
            uwbPrecisionMeters = 0.45f,
            radarAzimuthDeg = 56f,
            specialtyKeywords = listOf("315", "أمن سيبراني", "اختبار الاختراق", "حاسب", "تقنية", "مختبر"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "البوابة الغربية لمبنى الحاسبات",
                    instruction = "الوصول إلى البوابة الغربية لمبنى الحاسبات (مسافة 40م)",
                    voiceGuidanceArabic = "تقدم لمسافة أربعين متراً للوصول إلى البوابة الغربية لمبنى الحاسبات رقم اثنين.",
                    distanceMeters = 40,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-W2", "#G01", "#G02")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "المصعد الكهربائي إلى الدور الثالث",
                    instruction = "ركوب المصعد الكهربائي إلى الدور الثالث",
                    voiceGuidanceArabic = "بعد عشرة أمتار، اتجه يساراً نحو المصعد رقم اثنين والصعود للدور الثالث.",
                    distanceMeters = 10,
                    landmarkIconType = "ELEVATOR",
                    targetTurnAngle = -30f,
                    sideDoorsInSegment = listOf("ELEV-1", "ELEV-2")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "الممر الرئيسي - الجناح الشمالي",
                    instruction = "السير في الجناح الشمالي عبر الممر الرئيسي لمسافة 25م",
                    voiceGuidanceArabic = "اسلك الجناح الشمالي عبر الممر الرئيسي لمسافة خمسة وعشرين متراً مع تتبع الخط النيوني.",
                    distanceMeters = 25,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#312", "#313", "#314", "#315")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى المختبر #315",
                    instruction = "الوصول إلى القاعة #315 على اليمين - نهاية المسار",
                    voiceGuidanceArabic = "وصلت إلى مختبر الأمن السيبراني واختبار الاختراق رقم ثلاثمئة وخمسة عشر على يمينك.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = 40f,
                    sideDoorsInSegment = listOf("#314", "#315")
                )
            )
        ),
        CampusHall(
            code = "#204",
            rawNumber = "204",
            officialName = "قاعة المحاكاة والأسواق المالية",
            buildingId = 3,
            buildingName = "مبنى 3",
            floorNumber = 2,
            floorLabel = "الدور الثاني",
            capacityLabel = "60 مقعداً",
            category = HallCategory.FINANCE_ADMIN,
            isAvailableNow = false,
            liveStatusText = "قيد المحاضرة",
            currentLecturer = "د. ريم القحطاني",
            currentTopic = "إدارة المحافظ الاستثمارية والتقنية المالية FinTech",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-FIN-204B",
            uwbPrecisionMeters = 0.50f,
            radarAzimuthDeg = 220f,
            specialtyKeywords = listOf("204", "محاكاة", "أسواق مالية", "مالية", "إدارة", "أعمال", "استثمار"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "البوابة الذهبية لكلية الأعمال",
                    instruction = "الوصول إلى البوابة الرئيسية لمبنى إدارة الأعمال والابتكار (مسافة 32م)",
                    voiceGuidanceArabic = "اتجه نحو البوابة الذهبية لمبنى كلية إدارة الأعمال رقم ثلاثة لمسافة اثنين وثلاثين متراً.",
                    distanceMeters = 32,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-B3", "#104")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "مصعد جناح الاستثمار",
                    instruction = "الصعود عبر المصعد الأوسط إلى الدور الثاني (طابق الأسواق المالية)",
                    voiceGuidanceArabic = "استخدم المصعد الأوسط للصعود إلى الدور الثاني حيث جناح المحاكاة المالية.",
                    distanceMeters = 14,
                    landmarkIconType = "ELEVATOR",
                    targetTurnAngle = 20f,
                    sideDoorsInSegment = listOf("ELEV-B3", "STAIRS-C")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "ممر التداول والمحاكاة",
                    instruction = "السير في الممر الجنوبي الذهبي لمسافة 18م بمحاذاة شاشات المؤشرات",
                    voiceGuidanceArabic = "سر في الممر الجنوبي لمسافة ثمانية عشر متراً بمحاذاة شاشات الأسواق المالية.",
                    distanceMeters = 18,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#201", "#202", "#203", "#204")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى قاعة #204",
                    instruction = "الوصول إلى قاعة المحاكاة والأسواق المالية #204 على اليسار - نهاية المسار",
                    voiceGuidanceArabic = "وصلت إلى قاعة المحاكاة والأسواق المالية رقم مئتين وأربعة على يسارك.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = -35f,
                    sideDoorsInSegment = listOf("#203", "#204")
                )
            )
        ),
        CampusHall(
            code = "#G12",
            rawNumber = "G12",
            officialName = "قاعة البحث الرقمي والمخطوطات",
            buildingId = 4,
            buildingName = "مبنى 4",
            floorNumber = 0,
            floorLabel = "الدور الأرضي",
            capacityLabel = "80 باحثاً",
            category = HallCategory.DIGITAL_RESEARCH,
            isAvailableNow = true,
            liveStatusText = "متاح الآن للبحث",
            currentLecturer = "أرشيف البحث الرقمي المفتوح",
            currentTopic = "قواعد البيانات العالمية وفهرسة المخطوطات النادرة",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-LIB-G12M",
            uwbPrecisionMeters = 0.35f,
            radarAzimuthDeg = 140f,
            specialtyKeywords = listOf("G12", "بحث رقمي", "مخطوطات", "مكتبة", "اطلاع", "دراسة"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "بوابة المكتبة الرقمية المركزية",
                    instruction = "الوصول إلى البوابة الغربية للمكتبة الرقمية المركزية (مسافة 28م)",
                    voiceGuidanceArabic = "تقدم لمسافة ثمانية وعشرين متراً نحو البوابة الغربية للمكتبة الرقمية المركزية مبنى أربعة.",
                    distanceMeters = 28,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-L4", "#G01")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "الرواق الأرضي الهادئ",
                    instruction = "الدخول عبر البوابة الزجاجية الذكية في نفس الطابق الأرضي",
                    voiceGuidanceArabic = "اعبر البوابة الزجاجية الذكية في الدور الأرضي وتابع المسار الأزرق المحيطي.",
                    distanceMeters = 10,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#G08", "#G09")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "جناح المخطوطات والأبحاث",
                    instruction = "السير في ممر الباحثين الهادئ لمسافة 20م نحو جناح المخطوطات",
                    voiceGuidanceArabic = "سر بهدوء في ممر الباحثين لمسافة عشرين متراً حتى نهاية الجناح الشرقي.",
                    distanceMeters = 20,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 15f,
                    sideDoorsInSegment = listOf("#G10", "#G11", "#G12")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى قاعة #G12",
                    instruction = "الوصول إلى قاعة البحث الرقمي والمخطوطات #G12 على اليمين - نهاية المسار",
                    voiceGuidanceArabic = "وصلت إلى قاعة البحث الرقمي والمخطوطات جي اثني عشر على يمينك.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = 30f,
                    sideDoorsInSegment = listOf("#G11", "#G12")
                )
            )
        ),
        CampusHall(
            code = "#305",
            rawNumber = "305",
            officialName = "حاضنة النماذج الأولية وريادة الأعمال",
            buildingId = 5,
            buildingName = "مبنى 5",
            floorNumber = 3,
            floorLabel = "الدور الثالث",
            capacityLabel = "45 مبتكراً",
            category = HallCategory.FINANCE_ADMIN,
            isAvailableNow = true,
            liveStatusText = "متاح الآن (مختبر مفتوح)",
            currentLecturer = "م. سارة الزهراني",
            currentTopic = "تصنيع النماذج ثلاثية الأبعاد واحتضان الشركات الناشئة",
            isWheelchairAccessible = true,
            bleBeaconId = "BLE-INV-305P",
            uwbPrecisionMeters = 0.38f,
            radarAzimuthDeg = 15f,
            specialtyKeywords = listOf("305", "ابتكار", "مشاريع", "ريادة أعمال", "نماذج أولية", "حاضنة"),
            steps = listOf(
                NavigationStep(
                    stepIndex = 1,
                    stage = StepStage.EXTERIOR,
                    title = "المدخل المركزي لمركز الابتكار",
                    instruction = "الوصول إلى البوابة البنفسجية لمركز الابتكار وحاضنة المشاريع (مسافة 22م)",
                    voiceGuidanceArabic = "تقدم لمسافة اثنين وعشرين متراً نحو المدخل المركزي لمركز الابتكار مبنى خمسة.",
                    distanceMeters = 22,
                    landmarkIconType = "GATE",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("GATE-C5", "#101")
                ),
                NavigationStep(
                    stepIndex = 2,
                    stage = StepStage.VERTICAL,
                    title = "المصعد الذكي المركزي",
                    instruction = "ركوب المصعد الذكي للصعود إلى الدور الثالث",
                    voiceGuidanceArabic = "اصعد بالمصعد الذكي المركزي إلى الدور الثالث حيث حاضنة المشاريع.",
                    distanceMeters = 12,
                    landmarkIconType = "ELEVATOR",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("ELEV-C5")
                ),
                NavigationStep(
                    stepIndex = 3,
                    stage = StepStage.CORRIDOR,
                    title = "ممر صانعات النماذج الأولية",
                    instruction = "السير في الممر الدائري البنفسجي لمسافة 16م",
                    voiceGuidanceArabic = "اسلك الممر الدائري في الدور الثالث لمسافة ستة عشر متراً.",
                    distanceMeters = 16,
                    landmarkIconType = "CORRIDOR",
                    targetTurnAngle = 20f,
                    sideDoorsInSegment = listOf("#303", "#304", "#305")
                ),
                NavigationStep(
                    stepIndex = 4,
                    stage = StepStage.ARRIVAL,
                    title = "الوصول إلى حاضنة #305",
                    instruction = "الوصول إلى حاضنة النماذج الأولية وريادة الأعمال #305 - نهاية المسار",
                    voiceGuidanceArabic = "وصلت إلى حاضنة النماذج الأولية وريادة الأعمال رقم ثلاثمئة وخمسة.",
                    distanceMeters = 0,
                    landmarkIconType = "ROOM",
                    targetTurnAngle = 0f,
                    sideDoorsInSegment = listOf("#305")
                )
            )
        )
    )

    val feasibilityMetrics: List<FeasibilityMetric> = listOf(
        FeasibilityMetric(
            id = "transit_time",
            title = "تحسين زمن العبور (Transit Time Reduction)",
            subtitle = "تقليص وقت وصول الطلاب الجدد والزوار للقاعات",
            highlightValue = "-65%",
            description = "أظهرت القياسات الميدانية للنظام تقليل زمن البحث والوصول للقاعات والمختبرات بنسبة تتجاوز 65% مقارنة بالخرائط التقليدية.",
            progressRatio = 0.88f,
            accentColor = NeonCyan
        ),
        FeasibilityMetric(
            id = "crowd_flow",
            title = "كفاءة تدفق الحشود (Crowd Flow Balancing)",
            subtitle = "توزيع ذكي للمسارات والمصاعد وقت الذروة",
            highlightValue = "+78%",
            description = "توجيه الطلاب تلقائياً نحو بوابات ومصاعد بديلة أقل ازدحاماً خلال فترات تبديل المحاضرات كل ساعة.",
            progressRatio = 0.78f,
            accentColor = NeonEmerald
        ),
        FeasibilityMetric(
            id = "accessibility",
            title = "مسارات مجهزة لذوي الإعاقة (Accessibility First)",
            subtitle = "تغطية شاملة بالمنحدرات والمصاعد التلقائية",
            highlightValue = "100%",
            description = "فرز فوري للمسارات المجهزة للكراسي المتحركة (Wheelchair Accessible) وربطها بأقرب المصاعد والأبواب الأوتوماتيكية.",
            progressRatio = 1.0f,
            accentColor = TacticalGold
        ),
        FeasibilityMetric(
            id = "green_campus",
            title = "كفاءة استهلاك الطاقة (Green Campus Metrics)",
            subtitle = "ربط حالة الإشغال اللحظية بأنظمة التكييف والإنارة",
            highlightValue = "-38%",
            description = "المساهمة في إطفاء وجدولة القاعات غير المستخدمة عبر ربط حالة الإشغال المباشرة (Live Status) بحساسات المباني.",
            progressRatio = 0.74f,
            accentColor = OceanBlue
        )
    )

    val roadmapMilestones: List<RoadmapMilestone> = listOf(
        RoadmapMilestone(
            number = "7.1",
            title = "تحديد المواقع الداخلية الدقيقة",
            englishSubtitle = "Indoor Micro-Location (BLE Beacons & UWB)",
            bulletPoints = listOf(
                "منارات البلوتوث منخفض الطاقة (BLE Beacons) على زوايا الممرات بدقة تصل إلى 0.5 متر دون GPS.",
                "تقنية الموجات فوق العريضة (Ultra-Wideband UWB) لإظهار موقع المستخدم كنقطة حية متدفقة."
            ),
            statusBadge = "مُفعّل بالمحاكي ±0.4m",
            accentColor = NeonCyan
        ),
        RoadmapMilestone(
            number = "7.2",
            title = "الواقع المعزز الحي بالكاميرا",
            englishSubtitle = "Live AR Navigation Overlay",
            bulletPoints = listOf(
                "رسم أسهم النيون والمسار ثلاثي الأبعاد مباشرة فوق بث كاميرا الهاتف للممر.",
                "لافتات رقمية طافية فوق أبواب القاعات تعرض اسم المحاضر وحالة القاعة اللحظية."
            ),
            statusBadge = "متاح في شاشة الملاحة",
            accentColor = NeonEmerald
        ),
        RoadmapMilestone(
            number = "7.3",
            title = "التكامل مع الجداول الدراسية وأنظمة الجامعة",
            englishSubtitle = "SIS / LMS & IoT Occupancy Sensors",
            bulletPoints = listOf(
                "مزامنة الجدول الأكاديمي وتنبيه الطالب قبل المحاضرة بـ 15 دقيقة مع بدء ملاحة فوري.",
                "تحديثات الإشغال اللحظية عبر حساسات إنترنت الأشياء (IoT Occupancy Sensors)."
            ),
            statusBadge = "متصل بالجدول الذكي",
            accentColor = TacticalGold
        ),
        RoadmapMilestone(
            number = "7.4",
            title = "المساعد الصوتي التكتيكي باللغة العربية",
            englishSubtitle = "Arabic Voice Guided Navigation & Live AI",
            bulletPoints = listOf(
                "توجيه صوتي ناطق باللغة العربية الفصحى ينبه الطالب بالمسافات والانعطافات أثناء المشي.",
                "تكامل مع محركات Gemini للتفكير العميق والبحث المباشر والمحادثة الصوتية."
            ),
            statusBadge = "مُدمج بالكامل",
            accentColor = InnovationPurple
        ),
        RoadmapMilestone(
            number = "7.5",
            title = "حجز القاعات ومشاركة المواقع الحية",
            englishSubtitle = "Live Location Sharing («أنا هنا») & Hall Booking",
            bulletPoints = listOf(
                "ميزة «أنا هنا» لمشاركة إحداثيات القاعة والمسار التكتيكي مع الزملاء عبر رابط سريع.",
                "حجز فوري للقاعات المتاحة للمذاكرة أو الاجتماعات الطلابية."
            ),
            statusBadge = "متاح للتجربة الفورية",
            accentColor = OceanBlue
        )
    )
}
