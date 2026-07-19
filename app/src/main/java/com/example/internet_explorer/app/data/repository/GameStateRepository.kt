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
        _progress.update { current ->
            current.copy(
                visitedWebsiteIds = current.visitedWebsiteIds + websiteId,
                collectedFactIds = current.collectedFactIds + website.revealedFactIds
            )
        }
    }

    /**
     * Tương đương POST /api/puzzles/{id}/submit ở mục 13.3.4 -- response shape
     * {correct, unlockedClue} khớp với API contract thật để sau này dễ swap.
     */
    fun submitPuzzleAnswer(selectedAnomalyIds: Set<String>): PuzzleSubmitResult {
        val correct = selectedAnomalyIds == puzzle.correctAnomalyIds.toSet()
        if (!correct) return PuzzleSubmitResult(correct = false, unlockedClue = null)

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
}

data class PlayerProgress(
    val visitedWebsiteIds: Set<String> = emptySet(),
    val collectedFactIds: Set<String> = emptySet(),
    val solvedPuzzleIds: Set<String> = emptySet(),
    val unlockedClueIds: Set<String> = emptySet()
)

data class PuzzleSubmitResult(
    val correct: Boolean,
    val unlockedClue: ClueJson?
)