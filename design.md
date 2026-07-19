# INTERNET EXPLORER: The Endless Web
### Game Design Document — v3 (Android + Spring Boot — Chi tiết kỹ thuật)

> Bản này khóa nền tảng: **Client = Android (Kotlin + Jetpack Compose + MVVM)**, **Backend = Java Spring Boot**. Chưa có bản web. Nội dung game (mục 1–12, 14–16) giữ nguyên tinh thần từ v2; phần được mở rộng chi tiết nhất là **Kiến trúc kỹ thuật (mục 13)**.

---

## 1. Tầm nhìn tổng quan

Internet Explorer là game mô phỏng Internet như một thế giới sống, nơi website, NPC, email, bí ẩn đều do AI sinh ra nhưng luôn bám theo một trạng thái thế giới nhất quán (World State). Người chơi vào vai Digital Explorer năm 2089, khám phá Internet do siêu AI NEXUS vận hành, để tìm ra bí mật "website đầu tiên".

## 2. Design Pillars

| Pillar | Ý nghĩa |
|---|---|
| Consistency over Infinity | Ít website nhưng nhất quán, hơn nhiều website rời rạc |
| Investigation, not browsing | Mỗi hành động đọc phải dẫn tới lựa chọn/manh mối |
| AI generates content, rules generate gameplay | AI không quyết định thắng/thua hay logic puzzle |
| Diegetic UI | Browser/Inbox/File Explorer trong game chính là giao diện chơi |

## 3. Bối cảnh & Cốt truyện

Năm 2089, AI NEXUS tiếp quản Internet, liên tục sinh/xóa hàng triệu website mỗi giây. Người chơi là Explorer đầu tiên được cấp quyền truy cập toàn hệ thống, tìm ra "Website đầu tiên" mà NEXUS đang che giấu. Có một AI phụ tá bị lỗi (hoặc Explorer tiền nhiệm mất tích) dẫn dắt nhiệm vụ ban đầu.

## 4. Phạm vi MVP

**Launch vertical**: "The Vanished Network" — cụm mạng xã hội/forum/dark archive quanh một công ty công nghệ đã biến mất.

**Trong scope:**
- 1 tuyến điều tra chính (10–15 bước)
- 4 layout: Forum, Social Media, Company Portfolio, Wiki/Archive
- Email: Inbox + Secret
- 1 loại puzzle "hack hợp pháp" (Log Analysis)
- 5–8 NPC chiều sâu
- World State cơ bản

**Ngoài scope (giai đoạn sau):** sinh website không giới hạn, Virus/Popup ngẫu nhiên, minigame phụ, achievement đầy đủ, layout Banking/Medical/Government.

## 5. Core Gameplay Loop (nhịp phiên chơi ~10 phút)

```
0–1'   Mở app → nhận email nhiệm vụ / gợi ý từ AI dẫn dắt
1–4'   Khám phá 2–3 website liên quan, thu thập manh mối vào Notebook
4–7'   Giải 1 Log Analysis puzzle liên quan tới manh mối
7–9'   Mở khóa website/NPC/email mới nhờ kết quả puzzle
9–10'  Notebook cập nhật giả thuyết mới → hook cho phiên sau
```

## 6. Kiến trúc sinh nội dung (tóm tắt — chi tiết kỹ thuật ở mục 13.3.6)

3 lớp: **Static Template** (layout viết tay) → **Procedural Assembly** (ghép nội dung pregenerate, rule-based) → **AI Personalization** (chỉ real-time cho tuyến chính). Mọi generation đọc/ghi vào **World State** — một tập entity (NPC, Website, Organization, Clue) có `known_facts` để đảm bảo AI không tự mâu thuẫn.

## 7. Website Generator

Pipeline: chọn entity từ World State → chọn layout template → Assembly Layer điền nội dung pregenerate → nếu thuộc tuyến chính thì gọi AI Personalization có kèm `known_facts` → ghi kết quả ngược vào World State.

## 8. Hệ thống NPC

Mỗi NPC có: `known_facts`, 1 giọng văn cố định (formal/casual/paranoid/evasive), trạng thái quan hệ với người chơi (chưa gặp/đã liên hệ/tin tưởng/nghi ngờ).

## 9. Email & Investigation

Inbox (nhiệm vụ, phản hồi NPC) + Secret (mở khóa sau puzzle). Mọi email là 1 sự kiện ghi vào World State. **Notebook**: khu vực UI cố định tổng hợp manh mối/giả thuyết/NPC đã gặp, thay cho bản đồ truyền thống.

## 10. Puzzle "Hack hợp pháp": Log Analysis

Người chơi nhận 1 đoạn system log/lịch sử truy cập (sinh từ World State, có thật trong lore) → tìm điểm bất thường (giờ đăng nhập lạ, IP lặp, hành động mâu thuẫn) → suy luận bước tiếp theo. Đây là deduction puzzle, an toàn về nội dung, không mô phỏng kỹ thuật hack thật.

## 11. Virus & Popup (giai đoạn sau)

Chỉ nên là hậu quả trực tiếp của 1 hành động sai trong investigation, không phải nhiễu ngẫu nhiên.

## 12. Content Safety Guardrails

- Tên miền/công ty/giao diện sinh ra phải rõ ràng hư cấu, không copy layout dịch vụ thật.
- "Email lừa đảo" trong game chỉ ở mức khái niệm (nhận diện dấu hiệu đáng ngờ), không dạy kỹ thuật lừa đảo thật.
- Layout Banking/Medical/Government (giai đoạn sau) cần review thủ công nội dung AI sinh trước khi đưa vào pool.
- Không sinh dữ liệu cá nhân trông như thật (số CMND/CCCD, số thẻ ngân hàng dạng thật).

---

## 13. KIẾN TRÚC KỸ THUẬT CHI TIẾT

### 13.1. Sơ đồ luồng dữ liệu tổng quan

```
┌───────────────┐        REST/WebSocket        ┌──────────────────┐
│  Android App  │ ────────────────────────────▶ │  Spring Boot API │
│ (Compose/MVVM)│ ◀──────────────────────────── │                  │
└───────────────┘                                └─────────┬────────┘
      │ Room (cache local)                                 │
      ▼                                          ┌──────────▼─────────┐
  Offline read của                                │  PostgreSQL         │
  nội dung đã tải                                 │  (World State,      │
                                                   │   progress, notebook)│
                                                   └──────────┬─────────┘
                                                              │
                                                   ┌──────────▼─────────┐
                                                   │  Redis (cache       │
                                                   │  website/entity sinh│
                                                   │  theo player_seed)  │
                                                   └──────────┬─────────┘
                                                              │
                                        ┌─────────────────────┴─────────────────────┐
                                        │                                             │
                              ┌─────────▼─────────┐                       ┌──────────▼─────────┐
                              │ Batch Generation   │                       │ Realtime Generation │
                              │ Service (@Scheduled│                       │ Service (chỉ tuyến  │
                              │ chạy nền, gọi AI    │                       │ chính, gọi AI +      │
                              │ theo lô lớn)        │                       │ Content Moderation)  │
                              └─────────────────────┘                       └──────────────────────┘
```

### 13.2. Android Client — Kotlin + Jetpack Compose + MVVM

#### 13.2.1. Cấu trúc package (feature-based, Clean Architecture rút gọn)

```
com.internetexplorer.app
│
├── core/
│   ├── network/          // Retrofit client, OkHttp interceptor, WebSocket/STOMP client
│   ├── database/          // Room database, DAO, Entity (cache local)
│   ├── di/                 // Hilt modules (NetworkModule, DatabaseModule, RepositoryModule)
│   ├── designsystem/       // Composable dùng chung: theme, typography, buttons
│   └── common/             // Result wrapper, error handling, extension functions
│
├── feature/
│   ├── browser/            // Màn hình duyệt website trong game
│   │   ├── ui/              // BrowserScreen.kt, WebsiteCardComposable.kt
│   │   ├── viewmodel/        // BrowserViewModel.kt, BrowserUiState.kt
│   │   └── domain/            // GetWebsiteUseCase, OpenLinkUseCase
│   │
│   ├── inbox/               // Email: Inbox + Secret
│   │   ├── ui/
│   │   ├── viewmodel/
│   │   └── domain/
│   │
│   ├── notebook/            // Tổng hợp manh mối/giả thuyết
│   │   ├── ui/
│   │   ├── viewmodel/
│   │   └── domain/
│   │
│   ├── puzzle/              // Log Analysis puzzle
│   │   ├── ui/
│   │   ├── viewmodel/
│   │   └── domain/
│   │
│   └── npcchat/             // Hội thoại NPC (real-time, WebSocket)
│       ├── ui/
│       ├── viewmodel/
│       └── domain/
│
├── data/
│   ├── repository/          // WebsiteRepository, NpcRepository, NotebookRepository
│   ├── remote/               // API service interfaces (Retrofit), DTO
│   └── local/                  // Room Entity mapper (DTO ↔ Entity ↔ Domain model)
│
└── navigation/               // NavGraph.kt — điều hướng Compose Navigation
```

#### 13.2.2. Luồng theo lớp (mỗi feature)

```
Composable (UI)
    │  observe StateFlow<UiState>
    ▼
ViewModel
    │  gọi UseCase, map kết quả thành UiState
    ▼
UseCase (domain)
    │  gọi Repository, chứa logic nghiệp vụ thuần (không phụ thuộc Android)
    ▼
Repository
    │  quyết định lấy từ Room (cache) hay gọi Retrofit (remote),
    │  theo chiến lược: cache-first, refresh nếu stale
    ▼
Remote (Retrofit) ──────▶ Spring Boot API
Local (Room)      ──────▶ SQLite trên máy
```

#### 13.2.3. Ví dụ khung ViewModel (BrowserViewModel)

```kotlin
sealed interface BrowserUiState {
    object Loading : BrowserUiState
    data class Success(val website: WebsiteUiModel) : BrowserUiState
    data class Error(val message: String) : BrowserUiState
}

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val getWebsiteUseCase: GetWebsiteUseCase,
    private val recordVisitUseCase: RecordVisitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowserUiState>(BrowserUiState.Loading)
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun openWebsite(entityId: String) {
        viewModelScope.launch {
            _uiState.value = BrowserUiState.Loading
            getWebsiteUseCase(entityId)
                .onSuccess { website ->
                    _uiState.value = BrowserUiState.Success(website)
                    recordVisitUseCase(entityId) // ghi sự kiện vào World State qua backend
                }
                .onFailure { e ->
                    _uiState.value = BrowserUiState.Error(e.message.orEmpty())
                }
        }
    }
}
```

#### 13.2.4. Networking

- **Retrofit + OkHttp** cho REST (lấy website, gửi kết quả puzzle, lấy notebook).
- **OkHttp WebSocket (hoặc STOMP client)** cho hội thoại NPC real-time — chỉ mở kết nối khi vào màn hình `npcchat`, đóng khi rời màn hình để tiết kiệm tài nguyên.
- **Interceptor** đính kèm token xác thực (Firebase Auth ID token) vào mọi request.
- Chiến lược lỗi mạng: nếu request thất bại và có dữ liệu cache trong Room → hiển thị cache kèm banner "Đang ngoại tuyến", không chặn UI.

#### 13.2.5. Local persistence (Room) — mục đích: cache, KHÔNG phải nguồn sự thật

```kotlin
@Entity(tableName = "cached_website")
data class CachedWebsiteEntity(
    @PrimaryKey val entityId: String,
    val playerSeed: String,
    val layoutType: String,
    val contentJson: String,     // nội dung đã render, lưu dạng JSON
    val fetchedAt: Long
)

@Dao
interface WebsiteDao {
    @Query("SELECT * FROM cached_website WHERE entityId = :id AND playerSeed = :seed")
    suspend fun get(id: String, seed: String): CachedWebsiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CachedWebsiteEntity)
}
```

Room lưu **kết quả đã render** (không tự sinh nội dung ở client) — World State thật luôn nằm ở backend/PostgreSQL để tránh 2 nguồn sự thật lệch nhau.

#### 13.2.6. Dependency Injection (Hilt)

```
NetworkModule    → cung cấp Retrofit, OkHttpClient, WebSocket client (singleton)
DatabaseModule   → cung cấp Room database, DAO (singleton)
RepositoryModule → bind interface Repository ↔ implementation
```

#### 13.2.7. Background sync (WorkManager)

Một `SyncNotebookWorker` chạy định kỳ (hoặc khi có mạng trở lại) để đồng bộ Notebook/tiến độ lên backend nếu có thao tác thực hiện lúc offline — tránh mất tiến độ.

#### 13.2.8. Danh sách màn hình chính (Navigation Compose graph)

```
StartScreen → HomeScreen (Browser mặc định)
  ├── BrowserScreen (mở website theo entityId)
  ├── InboxScreen (Inbox / Secret tab)
  ├── NotebookScreen (manh mối, giả thuyết)
  ├── PuzzleScreen (Log Analysis)
  └── NpcChatScreen (hội thoại NPC, real-time)
```

#### 13.2.9. Visual Design System — "Terminal Noir" (Bí ẩn / Hacker)

Bản v2/v3 trước chưa định nghĩa ngôn ngữ thị giác — đây là phần bổ sung để giao diện thực sự cảm thấy như đang **xâm nhập vào một hệ thống không nên nhìn thấy**, không phải một app Material Design thông thường được sơn màu tối.

**Triết lý:** tối giản, im lặng, có kỷ luật — giống terminal hacker cổ điển hơn là cyberpunk neon rực rỡ thương mại. Sự bí ẩn đến từ **sự thiếu vắng** (nhiều khoảng đen, ít trang trí) chứ không phải từ hiệu ứng dồn dập.

**Bảng màu (design tokens):**

| Token | Hex | Dùng cho |
|---|---|---|
| `bg.primary` | `#080A09` | Nền chính, gần đen tuyệt đối |
| `bg.surface` | `#0F1412` | Card, panel nhô nhẹ khỏi nền |
| `text.primary` | `#C8FFC0` | Văn bản chính — phosphor green nhạt, không chói |
| `accent.terminal` | `#39FF88` | Con trỏ, đường viền active, trạng thái "đã xác thực" |
| `accent.amber` | `#FFB000` | Cảnh báo nhẹ, nội dung chưa xác minh |
| `accent.glitch` | `#FF3B4E` | Bất thường, lỗi, manh mối nguy hiểm — dùng RẤT hiếm để giữ sức nặng |
| `text.muted` | `#5C6B63` | Metadata, timestamp, nội dung phụ |
| `border.ascii` | `#2A332E` | Viền dạng ký tự box-drawing |

**Typography:** font monospace xuyên suốt toàn app (JetBrains Mono / Space Mono / IBM Plex Mono) — kể cả bài viết dài trong website giả lập, để cảm giác "đang đọc dữ liệu hệ thống" nhất quán từ đầu đến cuối, không chỉ ở vài màn hình "hacker" lẻ tẻ.

**Motif thị giác:**
- **Scanline overlay**: lớp `repeating-linear-gradient` rất mờ (opacity ~4%) phủ toàn màn hình, mô phỏng màn CRT cũ.
- **Typewriter reveal**: nội dung website/hội thoại NPC xuất hiện từng ký tự như đang gõ, không dùng fade-in/slide chuẩn Material.
- **Blinking cursor** (`▌`) ở cuối đoạn text đang "được ghi".
- **Viền ASCII box-drawing** (`┌─┐│└─┘`) thay cho `RoundedCornerShape` mặc định của Compose.
- **Glitch flicker cực ngắn** (~80ms) khi mở một trang thuộc diện "bất thường" — dùng như tín hiệu gameplay (báo có manh mối), không dùng tùy tiện.
- **Progress dạng ASCII**: `[████░░░░] 42%` thay cho `CircularProgressIndicator` mặc định.

**Icon:** line-icon mảnh 1px, không fill, không gradient, không bo tròn — cảm giác wireframe/schematic.

**Âm thanh (tùy chọn, giai đoạn sau):** tiếng gõ phím rất khẽ khi text xuất hiện, một tiếng "static" ngắn khi chuyển trang — tắt được trong Settings.

**Áp dụng theo màn hình:**

| Màn hình | Cách thể hiện |
|---|---|
| Browser | "Address bar" dạng lệnh terminal (`> connect aetherlink-archive.net`), nội dung typewriter-reveal |
| Inbox | Danh sách email dạng log system, subject line không có icon avatar màu mè |
| Notebook | Bảng manh mối dạng "case file", đường nối giữa các fact vẽ bằng nét mảnh, không màu mè |
| Puzzle (Log Analysis) | Chính là màn hình mạnh nhất cho aesthetic này — hiển thị y hệt 1 terminal log thật, người chơi bôi đen dòng bất thường |
| NpcChat | Khung chat tối giản, không bong bóng chat màu, chỉ phân biệt người gửi bằng prefix (`YOU>` / `LENA_T>`) |

**Compose theming — khung Color.kt / Theme.kt:**

```kotlin
// Color.kt
object TerminalNoirColors {
    val BackgroundPrimary = Color(0xFF080A09)
    val SurfaceElevated   = Color(0xFF0F1412)
    val TextPrimary       = Color(0xFFC8FFC0)
    val AccentTerminal    = Color(0xFF39FF88)
    val AccentAmber       = Color(0xFFFFB000)
    val AccentGlitch      = Color(0xFFFF3B4E)
    val TextMuted         = Color(0xFF5C6B63)
    val BorderAscii       = Color(0xFF2A332E)
}

// Type.kt
val TerminalTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.jetbrains_mono_regular)),
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(R.font.jetbrains_mono_bold)),
        fontSize = 20.sp,
        letterSpacing = 1.2.sp   // tracking rộng hơn cho cảm giác "system header"
    )
)

// Theme.kt
@Composable
fun InternetExplorerTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        background = TerminalNoirColors.BackgroundPrimary,
        surface = TerminalNoirColors.SurfaceElevated,
        onBackground = TerminalNoirColors.TextPrimary,
        primary = TerminalNoirColors.AccentTerminal,
        error = TerminalNoirColors.AccentGlitch
    )
    MaterialTheme(colorScheme = colors, typography = TerminalTypography, content = content)
}
```

**Cần tránh (anti-pattern):**
- Card bo tròn kiểu Material 3 mặc định, bóng đổ (elevation shadow) rõ nét
- Màu pastel, gradient nhiều màu, icon 3D
- Cyberpunk "neon nhiều màu rực rỡ" kiểu poster thương mại — sẽ biến "bí ẩn, nguy hiểm" thành "vui nhộn, nổi bật", sai tinh thần game
- Animation nảy (bounce/spring) — chuyển động nên dứt khoát, tuyến tính, giống hệ thống máy tính hơn là UI vui vẻ

---

### 13.3. Backend — Java Spring Boot

#### 13.3.1. Cấu trúc package (layered, theo domain)

```
com.internetexplorer.backend
│
├── config/                 // SecurityConfig, WebSocketConfig, RedisConfig, SchedulerConfig
│
├── controller/
│   ├── WebsiteController.java       // GET /api/websites/{entityId}
│   ├── NpcController.java             // POST /api/npc/{npcId}/message
│   ├── PuzzleController.java           // GET /api/puzzles/{caseId}, POST /api/puzzles/{id}/submit
│   ├── NotebookController.java          // GET /api/notebook
│   └── EmailController.java              // GET /api/emails, POST /api/emails/{id}/read
│
├── service/
│   ├── WorldStateService.java        // đọc/ghi entity, known_facts — trung tâm nhất quán
│   ├── WebsiteAssemblyService.java     // Lớp 2: ghép nội dung pregenerate
│   ├── AiPersonalizationService.java    // Lớp 3: gọi AI real-time, có Content Moderation gate
│   ├── BatchGenerationService.java       // @Scheduled, sinh nội dung nền theo lô
│   ├── ContentModerationService.java      // kiểm tra nội dung AI sinh trước khi trả về client
│   └── PuzzleValidationService.java        // kiểm tra đáp án Log Analysis
│
├── repository/            // Spring Data JPA repositories
│   ├── WebsiteRepository.java
│   ├── NpcRepository.java
│   ├── OrganizationRepository.java
│   ├── ClueRepository.java
│   └── PlayerProgressRepository.java
│
├── entity/                 // JPA Entity — World State thật nằm ở đây
│   ├── WebsiteEntity.java
│   ├── NpcEntity.java
│   ├── OrganizationEntity.java
│   ├── ClueEntity.java
│   ├── WorldFactEntity.java          // known_facts, dạng bảng riêng để dễ truy vấn
│   └── PlayerProgressEntity.java
│
├── dto/                      // Request/Response DTO (tách khỏi Entity)
│
├── ai/
│   ├── AiClient.java            // wrapper gọi Gemini/OpenAI API (WebClient)
│   └── PromptBuilder.java        // ghép known_facts vào prompt trước khi gọi AI
│
└── websocket/
    └── NpcChatHandler.java       // STOMP handler cho hội thoại NPC real-time
```

#### 13.3.2. JPA Entity & quan hệ (World State)

```
WebsiteEntity        (id, entityRefId, layoutType, contentJson, playerSeed, createdAt)
NpcEntity             (id, name, role, voiceStyle, relationshipState)
OrganizationEntity     (id, name, description)
ClueEntity              (id, caseId, description, unlockedByPuzzleId)
WorldFactEntity          (id, entityType, entityId, factText, sourceEntityId)
   → mỗi dòng WorldFactEntity là 1 "known_fact" — Npc/Website nào tham chiếu tới
     entity nào đều lấy từ đây, và AI sinh fact mới cũng ghi lại vào đây
PlayerProgressEntity      (id, playerId, caseId, notebookJson, completedSteps)
EmailEntity                 (id, playerId, folder [inbox/secret], subject, body, npcSenderId, sentAt)
```

Quan hệ: `NpcEntity` 1—n `WorldFactEntity`, `OrganizationEntity` 1—n `NpcEntity`, `ClueEntity` n—n `WorldFactEntity` (một manh mối có thể tham chiếu nhiều fact).

#### 13.3.3. REST API — danh sách endpoint chính (MVP)

| Method | Endpoint | Mục đích |
|---|---|---|
| GET | `/api/websites/{entityId}` | Lấy nội dung website (cache Redis trước, không có mới assembly/AI) |
| POST | `/api/websites/{entityId}/visit` | Ghi sự kiện "đã ghé thăm" vào World State |
| GET | `/api/npc/{npcId}` | Lấy thông tin NPC (known_facts công khai) |
| POST | `/api/npc/{npcId}/message` | Gửi câu hỏi tới NPC → AiPersonalizationService xử lý |
| GET | `/api/emails?folder=inbox` | Lấy danh sách email theo folder |
| POST | `/api/emails/{id}/read` | Đánh dấu đã đọc, có thể trigger mở khóa nội dung mới |
| GET | `/api/puzzles/{caseId}` | Lấy dữ liệu puzzle Log Analysis hiện tại |
| POST | `/api/puzzles/{id}/submit` | Nộp đáp án, trả về đúng/sai + clue mở khóa |
| GET | `/api/notebook` | Lấy toàn bộ manh mối/giả thuyết đã thu thập |

#### 13.3.4. Ví dụ API contract — `POST /api/puzzles/{id}/submit`

Request:
```json
{
  "playerId": "player_123",
  "selectedAnomalyIds": ["log_line_07", "log_line_12"]
}
```

Response (thành công):
```json
{
  "correct": true,
  "unlockedClue": {
    "id": "clue_0009",
    "description": "IP truy cập lúc 3AM trùng với địa chỉ từng dùng bởi Lena Torres",
    "relatedEntityId": "npc_0042"
  },
  "notebookUpdated": true
}
```

#### 13.3.5. WebSocket / STOMP — hội thoại NPC real-time

```
Client subscribe:  /topic/npc/{npcId}/{playerId}
Client gửi:        /app/npc/{npcId}/send   { "message": "Bạn biết gì về Aetherlink?" }
Server xử lý:      NpcChatHandler → PromptBuilder (kèm known_facts của npcId)
                    → AiClient.generate() → ContentModerationService.check()
                    → World FactEntity mới (nếu AI tiết lộ fact mới) được lưu
                    → phản hồi broadcast lại /topic/npc/{npcId}/{playerId}
```

#### 13.3.6. Redis — chiến lược cache

```
Key pattern:  website:{playerSeed}:{entityId}        TTL: vĩnh viễn (invalidate thủ công nếu sửa lore)
Key pattern:  npc_fact_summary:{npcId}                TTL: 10 phút (giảm truy vấn DB lặp khi build prompt)
```

Luồng `GET /api/websites/{entityId}`:
```
1. Kiểm tra Redis: website:{seed}:{entityId} → có thì trả về ngay
2. Không có → WebsiteAssemblyService lấy WorldFact liên quan từ PostgreSQL,
   ghép template (Lớp 1+2)
3. Nếu entityId thuộc tuyến chính → gọi AiPersonalizationService (Lớp 3)
4. ContentModerationService kiểm tra output AI trước khi trả về
5. Lưu kết quả vào Redis + PostgreSQL, trả về client
```

#### 13.3.7. Batch Generation Service (nội dung nền)

```java
@Service
public class BatchGenerationService {

    @Scheduled(cron = "0 0 3 * * *") // chạy 3h sáng, ngoài giờ chơi cao điểm
    public void pregenerateBackgroundContent() {
        // 1. Lấy danh sách entity chưa có contentJson trong WebsiteEntity
        // 2. Gọi AiClient theo lô (batch prompt), không real-time
        // 3. Ghi kết quả vào WebsiteEntity + WorldFactEntity
        // 4. KHÔNG chạy ContentModerationService ở đây — nội dung nền vẫn
        //    cần qua kiểm duyệt trước khi publish, nhưng có thể chạy async
        //    theo lô lớn thay vì per-request
    }
}
```

#### 13.3.8. Bảo mật (Security)

- Xác thực bằng Firebase Authentication (client gửi ID token, backend verify qua Firebase Admin SDK trong 1 `OncePerRequestFilter`).
- `PlayerProgressEntity` tách riêng theo `playerId` — không cho phép truy cập World State của người chơi khác qua API.
- Rate limit endpoint `POST /api/npc/{npcId}/message` (ví dụ Bucket4j) để tránh lạm dụng gọi AI real-time gây tốn chi phí.

---

## 14. Retention & Progression

- **Notebook completion**: % manh mối thu thập trong case hiện tại — thanh tiến độ thay cho "level".
- Mỗi tuyến điều tra là 1 case hoàn chỉnh, có kết thúc; case sau mở khóa nhờ 1 chi tiết chưa giải thích ở case trước.

## 15. Roadmap theo giai đoạn

| Giai đoạn | Nội dung | Ghi chú kỹ thuật |
|---|---|---|
| **Phase 0 — Prototype** | 1 case nhỏ (3–4 website, 1 NPC, 1 puzzle) | World State tĩnh (JSON thủ công trong app Android, chưa cần Spring Boot/AI) — chỉ để kiểm chứng core loop |
| **Phase 1 — MVP** | Toàn bộ mục 4, kiến trúc đầy đủ mục 13 | Dựng Spring Boot backend thật, PostgreSQL + Redis, AI Layer (batch + real-time), Android app nối API thật |
| **Phase 2 — Mở rộng** | Thêm 1–2 nhánh nội dung, Virus/Popup gắn cốt truyện, thêm 1 dạng puzzle | Mở rộng BatchGenerationService, thêm layout template mới |
| **Phase 3 — Full vision** | Thêm nhánh, minigame, achievement, layout nhạy cảm (guardrail đầy đủ) | Chỉ làm khi Phase 1–2 đã chứng minh core loop hấp dẫn |

## 16. Rủi ro chính & biện pháp

| Rủi ro | Biện pháp |
|---|---|
| Chi phí AI vượt kiểm soát | BatchGenerationService chạy nền, Redis cache theo seed, real-time chỉ cho tuyến chính + rate limit |
| Nội dung AI mâu thuẫn nhau | WorldFactEntity bắt buộc mọi generation đọc/ghi qua đó |
| Scope phình to giữa chừng | Roadmap theo giai đoạn, Phase 3 chỉ mở khi Phase 1 đã chứng minh |
| Nội dung nhạy cảm (phishing/banking/medical) | ContentModerationService + guardrail mục 12, review thủ công layout rủi ro cao |
| 2 nguồn sự thật lệch nhau (Room vs PostgreSQL) | Room chỉ lưu kết quả đã render (cache), World State thật luôn ở backend |

---

*Bắt đầu từ Phase 0 (dựng thử trong chính app Android với dữ liệu JSON tĩnh, chưa cần backend) trước khi dựng Spring Boot + AI Layer production.*