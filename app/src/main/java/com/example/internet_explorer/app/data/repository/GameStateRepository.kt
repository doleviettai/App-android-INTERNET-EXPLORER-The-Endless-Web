package com.example.internet_explorer.app.data.repository

import android.content.Context
import com.example.internet_explorer.app.data.local.CaseAssetLoader
import com.example.internet_explorer.app.data.local.CaseJson
import com.example.internet_explorer.app.data.local.ClueJson
import com.example.internet_explorer.app.data.local.EmailJson
import com.example.internet_explorer.app.data.local.NpcJson
import com.example.internet_explorer.app.data.local.OrganizationJson
import com.example.internet_explorer.app.data.local.PuzzleJson
import com.example.internet_explorer.app.data.local.WebsiteJson
import com.example.internet_explorer.app.data.local.WorldFactJson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * World State ở Phase 0: static content (load 1 lần từ assets, bất biến)
 * + progress runtime (mutable, chỉ tồn tại trong bộ nhớ -- mất khi tắt app,
 * điều đó tạm ổn cho prototype, sẽ được ghi xuống Room/PostgreSQL ở Phase 1).
 *
 * ViewModel chỉ nói chuyện với các hàm public của object này. Khi lên Phase 1,
 * ta thay ruột bên trong bằng gọi Retrofit -- chữ ký hàm public giữ nguyên nên
 * UI layer (ViewModel + Composable) không cần sửa.
 */
object GameStateRepository {

    private lateinit var loader: CaseAssetLoader

    private lateinit var caseData: CaseJson
    private lateinit var organization: OrganizationJson
    private lateinit var npcs: List<NpcJson>
    private lateinit var websites: List<WebsiteJson>
    private lateinit var worldFacts: List<WorldFactJson>
    private lateinit var emails: List<EmailJson>
    private lateinit var puzzle: PuzzleJson
    private lateinit var clues: List<ClueJson>

    private val _progress = MutableStateFlow(PlayerProgress())
    val progress: StateFlow<PlayerProgress> = _progress.asStateFlow()

    // ==================== Trace Meter (gameplay-mechanics-phase2.md mục 2) ====================

    private val _traceLevel = MutableStateFlow(0f)
    /** 0.0-1.0 -- mức độ "ồn ào/liều lĩnh" của người chơi trong case hiện tại. */
    val traceLevel: StateFlow<Float> = _traceLevel.asStateFlow()

    private val _traceLog = MutableStateFlow<List<TraceLogEntry>>(emptyList())
    /** Lịch sử từng hành động đã cộng vào Trace -- dùng để dựng puzzle "dọn log" (mục 2.3 danger tier). */
    val traceLog: StateFlow<List<TraceLogEntry>> = _traceLog.asStateFlow()

    private val _isLockedOut = MutableStateFlow(false)
    /** true khi Trace vượt ngưỡng nguy hiểm -- toàn app phải hiện CleanupScreen, chặn mọi thao tác khác. */
    val isLockedOut: StateFlow<Boolean> = _isLockedOut.asStateFlow()

    private var traceConfig = TraceConfig()
    private var lastTraceActionAtMillis: Long = 0L

    /** Cho phép 1 màn hình debug/tuning sau này chỉnh trọng số mà không sửa code (mục 1.5). */
    fun updateTraceConfig(config: TraceConfig) {
        traceConfig = config
    }

    /**
     * Ghi 1 hành động vào Trace + tăng currentStep (Site Decay dùng chung bộ đếm step này,
     * xem mục Site Decay bên dưới). Nếu hành động cách hành động trước đó dưới 3s, nhân thêm
     * rapidActionMultiplier -- mô phỏng NEXUS chú ý nhiều hơn khi thao tác dồn dập/máy móc.
     * Khi Trace vượt config.dangerThreshold -> khóa toàn app, bắt buộc dọn log trước khi tiếp tục.
     */
    fun registerAction(action: TraceAction, config: TraceConfig = traceConfig) {
        val baseWeight = when (action) {
            TraceAction.OPEN_NEW_SITE -> config.openNewSiteWeight
            TraceAction.FAILED_PUZZLE -> config.failedPuzzleWeight
            TraceAction.SUCCESSFUL_HACK -> config.successfulHackWeight
        }
        val now = System.currentTimeMillis()
        val isRapid = lastTraceActionAtMillis != 0L && (now - lastTraceActionAtMillis) < 3000
        lastTraceActionAtMillis = now
        val delta = if (isRapid) baseWeight * config.rapidActionMultiplier else baseWeight

        _progress.update { it.copy(currentStep = it.currentStep + 1) }
        _traceLevel.update { (it + delta).coerceIn(0f, 1f) }

        _traceLog.update { current ->
            val entry = TraceLogEntry(
                id = "trace_${now}_${current.size}",
                action = action,
                weight = delta,
                atStep = _progress.value.currentStep
            )
            current + entry
        }

        if (_traceLevel.value >= config.dangerThreshold) {
            _isLockedOut.value = true
        }
    }

    /**
     * Giảm nhẹ Trace mỗi khi khởi động phiên chơi mới (naturalDecayPerSessionStart).
     * Ở Phase 0 progress chưa persist qua các lần tắt app, nên lệnh này hiện chưa có
     * hiệu ứng quan sát được (traceLevel luôn bắt đầu từ 0 mỗi lần mở app) -- vẫn gọi
     * để đúng logic khi Phase 1 nối Room/backend và progress bắt đầu được giữ lại.
     */
    private fun applyNaturalDecay(config: TraceConfig = traceConfig) {
        _traceLevel.update { (it - config.naturalDecayPerSessionStart).coerceIn(0f, 1f) }
    }

    /** Tiến độ case thuần theo fact đã thu thập -- tách riêng khỏi Trace để dễ blend/test độc lập. */
    fun caseProgressStatic(): Float {
        val total = getTotalFactCount()
        val collected = _progress.value.collectedFactIds.size
        return if (total > 0) collected.toFloat() / total.toFloat() else 0f
    }

    /**
     * Integrity Static cuối cùng = giá trị LỚN HƠN giữa tiến độ case và Trace
     * (đúng công thức mục 2.2 gameplay-mechanics-phase2.md). Hàm tiện ích cho logic
     * một lần; Composable cần phản ứng real-time nên tự collect progress + traceLevel
     * rồi blend, xem IntegrityStaticOverlay trong TerminalComponents.kt.
     */
    fun currentStaticIntensity(): Float = maxOf(caseProgressStatic(), _traceLevel.value)

    /**
     * Puzzle "dọn log" đảo ngược (mục Ngưỡng "Nguy hiểm" 0.85-1.0): người chơi chọn
     * NHỮNG DÒNG LOG CỦA CHÍNH MÌNH cần xóa để hạ Trace xuống dưới safeThresholdAfterCleanup.
     * Chọn chưa đủ -> vẫn bị khóa, thử lại (không giới hạn số lần).
     */
    fun submitCleanup(selectedEntryIds: Set<String>): CleanupResult {
        val entriesToRemove = _traceLog.value.filter { it.id in selectedEntryIds }
        val weightRemoved = entriesToRemove.sumOf { it.weight.toDouble() }.toFloat()

        _traceLog.update { current -> current.filterNot { it.id in selectedEntryIds } }
        _traceLevel.update { (it - weightRemoved).coerceIn(0f, 1f) }

        val stillLocked = _traceLevel.value >= traceConfig.safeThresholdAfterCleanup
        _isLockedOut.value = stillLocked
        return CleanupResult(success = !stillLocked, remainingTrace = _traceLevel.value)
    }

    // ==================== Site Decay (gameplay-mechanics-phase2.md mục 4) ====================

    /**
     * true nếu site đã "bị NEXUS xóa" -- currentStep vượt ngưỡng decayAfterStep (được rút ngắn
     * nhẹ theo Trace hiện tại, tối đa 2 step, đúng liên kết chéo mục 4.3.3) VÀ người chơi chưa
     * lưu (archive) kịp. Site không có decayAfterStep (null) không bao giờ decay.
     */
    fun isWebsiteDecayed(websiteId: String): Boolean {
        val website = getWebsite(websiteId) ?: return false
        val decayStep = website.decayAfterStep ?: return false
        if (websiteId in _progress.value.archivedWebsiteIds) return false

        val traceReduction = (_traceLevel.value * 2).toInt()
        val effectiveDecayStep = (decayStep - traceReduction).coerceAtLeast(1)
        return _progress.value.currentStep >= effectiveDecayStep
    }

    /**
     * true nếu site sắp decay trong 1-2 step tới -- dùng để hiện tag cảnh báo
     * [INDEX: UNSTABLE] ở address bar (mục 4.4), KHÔNG dùng đồng hồ đếm ngược lộ liễu.
     */
    fun isWebsiteUnstable(websiteId: String): Boolean {
        val website = getWebsite(websiteId) ?: return false
        val decayStep = website.decayAfterStep ?: return false
        if (websiteId in _progress.value.archivedWebsiteIds) return false
        if (isWebsiteDecayed(websiteId)) return false

        val traceReduction = (_traceLevel.value * 2).toInt()
        val effectiveDecayStep = (decayStep - traceReduction).coerceAtLeast(1)
        val stepsRemaining = effectiveDecayStep - _progress.value.currentStep
        return stepsRemaining in 1..2
    }

    /**
     * Người chơi chủ động "lưu vào Notebook" trước khi decay -- giữ nội dung vĩnh viễn.
     * Gọi sau khi đã decay sẽ không có tác dụng (nội dung đã mất thật sự, không "cứu" lại được) --
     * UI nên ẩn nút này khi isWebsiteDecayed() == true thay vì dựa vào no-op này.
     */
    fun archiveWebsite(websiteId: String) {
        if (isWebsiteDecayed(websiteId)) return
        _progress.update { it.copy(archivedWebsiteIds = it.archivedWebsiteIds + websiteId) }
    }

    // ==================== Hết Site Decay / Trace Meter ====================

    /** Gọi 1 lần duy nhất, ví dụ trong Application.onCreate() hoặc MainActivity. */
    fun init(context: Context) {
        if (GameStateRepository::caseData.isInitialized) return
        loader = CaseAssetLoader(context.applicationContext)
        caseData = loader.loadCase()
        organization = loader.loadOrganization()
        npcs = loader.loadNpcs()
        websites = loader.loadWebsites()
        worldFacts = loader.loadWorldFacts()
        emails = loader.loadEmails()
        puzzle = loader.loadPuzzle()
        clues = loader.loadClues()
        applyNaturalDecay()
    }

    fun getCase(): CaseJson = caseData
    fun getOrganization(): OrganizationJson = organization
    fun getAllWebsites(): List<WebsiteJson> = websites
    fun getWebsite(id: String): WebsiteJson? = websites.find { it.id == id }
    fun getPuzzle(): PuzzleJson = puzzle
    fun getNpc(id: String): NpcJson? = npcs.find { it.id == id }
    fun getEmails(folder: String): List<EmailJson> = emails.filter { it.folder == folder }
    fun getGson() = loader.gsonInstance
    fun getTotalFactCount(): Int = worldFacts.size

    /** Tương đương POST /api/websites/{entityId}/visit ở mục 13.3.3 -- ghi sự kiện vào World State. */
    fun markWebsiteVisited(websiteId: String) {
        val website = getWebsite(websiteId) ?: return
        val isNewVisit = websiteId !in _progress.value.visitedWebsiteIds

        _progress.update { current ->
            current.copy(
                visitedWebsiteIds = current.visitedWebsiteIds + websiteId,
                collectedFactIds = current.collectedFactIds + website.revealedFactIds
            )
        }

        // Chỉ tính Trace/step cho lượt ghé THẬT SỰ mới -- xem lại site đã ghé không nên
        // cộng dồn Trace vô hạn chỉ vì người chơi bấm lại nhiều lần.
        if (isNewVisit) {
            registerAction(TraceAction.OPEN_NEW_SITE)
        }
    }

    /**
     * Tương đương POST /api/puzzles/{id}/submit ở mục 13.3.4 -- response shape
     * {correct, unlockedClue} khớp với API contract thật để sau này dễ swap.
     * Cả 2 nhánh đúng/sai đều ghi Trace -- puzzle sai (dò dẫm) gây "tiếng" nhiều
     * hơn puzzle đúng (failedPuzzleWeight > successfulHackWeight theo config mặc định).
     */
    fun submitPuzzleAnswer(selectedAnomalyIds: Set<String>): PuzzleSubmitResult {
        val correct = selectedAnomalyIds == puzzle.correctAnomalyIds.toSet()
        if (!correct) {
            registerAction(TraceAction.FAILED_PUZZLE)
            return PuzzleSubmitResult(correct = false, unlockedClue = null)
        }

        registerAction(TraceAction.SUCCESSFUL_HACK)
        val clue = clues.find { it.id == puzzle.unlocksClueId }
        _progress.update { current ->
            current.copy(
                solvedPuzzleIds = current.solvedPuzzleIds + puzzle.id,
                unlockedClueIds = current.unlockedClueIds + listOfNotNull(clue?.id),
                collectedFactIds = current.collectedFactIds + (clue?.relatedFactIds ?: emptyList())
            )
        }
        return PuzzleSubmitResult(correct = true, unlockedClue = clue)
    }

    fun getNotebookClues(): List<ClueJson> =
        clues.filter { it.id in _progress.value.unlockedClueIds }

    fun getNotebookFacts(): List<WorldFactJson> =
        worldFacts.filter { it.id in _progress.value.collectedFactIds }

    /**
     * unlockCondition trong emails.json theo format "{puzzleId}_solved" (ví dụ "puzzle_log_001_solved").
     * null -> luôn mở khóa (email inbox thông thường).
     */
    fun isEmailUnlocked(email: EmailJson): Boolean {
        val condition = email.unlockCondition ?: return true
        val requiredPuzzleId = condition.removeSuffix("_solved")
        return requiredPuzzleId in _progress.value.solvedPuzzleIds
    }

    fun isWebsiteArchived(entityId: String): Boolean {
        return true
    }
}

data class PlayerProgress(
    val visitedWebsiteIds: Set<String> = emptySet(),
    val collectedFactIds: Set<String> = emptySet(),
    val solvedPuzzleIds: Set<String> = emptySet(),
    val unlockedClueIds: Set<String> = emptySet(),
    val currentStep: Int = 0,
    val archivedWebsiteIds: Set<String> = emptySet()
)

data class PuzzleSubmitResult(
    val correct: Boolean,
    val unlockedClue: ClueJson?
)

/** Loại hành động ảnh hưởng tới Trace -- xem gameplay-mechanics-phase2.md mục 2.2. */
enum class TraceAction {
    OPEN_NEW_SITE,
    FAILED_PUZZLE,
    SUCCESSFUL_HACK
}

/** Toàn bộ trọng số Trace nằm ở đây, không hardcode rải rác (mục 2.5 gameplay-mechanics-phase2.md). */
data class TraceConfig(
    val openNewSiteWeight: Float = 0.02f,
    val failedPuzzleWeight: Float = 0.05f,
    val successfulHackWeight: Float = 0.03f,
    val rapidActionMultiplier: Float = 1.5f,
    val naturalDecayPerSessionStart: Float = 0.05f,
    val dangerThreshold: Float = 0.85f,
    val safeThresholdAfterCleanup: Float = 0.6f
)

/** 1 dòng trong lịch sử Trace -- dữ liệu thật cho puzzle "dọn log" đảo ngược. */
data class TraceLogEntry(
    val id: String,
    val action: TraceAction,
    val weight: Float,
    val atStep: Int
)

data class CleanupResult(
    val success: Boolean,
    val remainingTrace: Float
)