# INTERNET EXPLORER — Phase 2 Gameplay Mechanics
### Spec chi tiết: Trust & Leak Network · Trace Meter · Link Map · Site Decay

> Đây là tài liệu bổ sung cho GDD v3 (mục Roadmap — Phase 2: Mở rộng). Cả 4 cơ chế đều được thiết kế để **tái dùng hạ tầng đã có** (World State, `GameStateRepository`, `CaseModel.kt`, Notebook, Integrity Static) thay vì đòi hỏi hệ thống hoàn toàn mới — giữ đúng nguyên tắc "Consistency over Infinity" đã đặt ra từ đầu.

---

## 0. Tổng quan mối liên hệ giữa 4 cơ chế

Không thiết kế 4 hệ thống rời rạc — chúng nên "biết" về nhau:

- **Trace Meter** cao → rút ngắn thời gian trước khi site "decay" (Site Decay) — NEXUS phản ứng nhanh hơn khi phát hiện bạn.
- **Trust & Leak Network**: một rò rỉ bị lan truyền cũng có thể tự động cộng nhẹ vào Trace (bạn "gây tiếng" trên hệ thống).
- **Link Map** là nơi hiển thị trực quan kết quả của cả 2 cơ chế trên — node của NPC đang nghi ngờ bạn có thể đổi màu viền, site đã decay hiển thị node mờ đi.

Không bắt buộc làm cả 4 cùng lúc — mục 6 cuối tài liệu đề xuất thứ tự triển khai.

---

## 1. Trust & Leak Network — mạng lưới tin tưởng & rò rỉ thông tin

### 1.1. Khái niệm

Thông tin bạn tiết lộ với một NPC không nằm yên ở đó — nếu NPC đó có quan hệ với NPC khác (đồng nghiệp, bạn bè, người thân), thông tin có thể "rò rỉ" sang, thay đổi cách NPC kia phản ứng với bạn. Biến lựa chọn "nói gì, với ai, lúc nào" thành quyết định có hậu quả thật.

### 1.2. Data model bổ sung

```kotlin
// CaseModel.kt — mở rộng NpcModel hiện có
data class NpcModel(
    val id: String,
    val name: String,
    val role: String,
    val voiceStyle: VoiceStyle,
    var trustLevel: Int = 0,               // -3..+3, thay cho enum quan hệ cũ
    val connections: List<NpcConnection> = emptyList(),
    val knownDisclosures: MutableSet<String> = mutableSetOf() // factId đã biết
)

data class NpcConnection(
    val targetNpcId: String,
    val relationshipType: RelationshipType, // COLLEAGUE, FRIEND, RIVAL, FAMILY
    val leakProbability: Float              // 0.0–1.0
)

data class Disclosure(
    val factId: String,
    val disclosedToNpcId: String,
    val atStep: Int,
    val sensitivity: Float                  // 0.0–1.0, fact càng nhạy cảm càng dễ lan
)
```

### 1.3. Logic cơ chế

1. Người chơi chọn tiết lộ fact `F` với NPC `A` (qua lựa chọn hội thoại hoặc hành động điều tra) → tạo `Disclosure` mới, ghi qua `GameStateRepository`.
2. Với mỗi `connection` của `A`, roll `leakProbability * sensitivity` — nếu thành công, fact `F` được thêm vào `knownDisclosures` của NPC đích, **nhưng không lộ ngay lập tức** — có độ trễ 1 "step" (mô phỏng "tin đồn qua đêm"), tránh cảm giác phản hồi máy móc tức thời.
3. Nếu fact khiến NPC nhận tin cảm thấy bị đe dọa/bị theo dõi → `trustLevel -= 1`. Nếu fact trung tính/có lợi → không đổi hoặc `+1`.
4. **Giới hạn lan truyền ở độ sâu 1** (A→B, không lan tiếp B→C) để tránh domino không kiểm soát được.
5. Rò rỉ đủ lớn có thể sinh ra 1 nội dung mới trong Browser (ví dụ một bài forum ẩn danh nhắc gián tiếp tới việc bị lộ) — dùng lại pipeline Website Assembly đã có (template có sẵn, điền tên/fact vào chỗ trống), không cần AI real-time.

### 1.4. Tích hợp UI/UX

- Notebook: mỗi NPC có thêm 1 badge nhỏ hiển thị `trustLevel` bằng `accent.terminal` (tin tưởng) / `accent.amber` (trung lập) / `accent.glitch` (nghi ngờ) — dùng đúng token màu đã có trong design skill.
- Trước khi xác nhận một lựa chọn hội thoại "rủi ro" (sensitivity cao), viền ô lựa chọn đó hiển thị nhẹ `accent.amber` để gợi ý "cái này có thể lan ra" — không cần giải thích bằng chữ.

### 1.5. Cân bằng

- Mỗi case chỉ nên có 2–3 fact "nhạy cảm" (sensitivity cao) để cơ chế có ý nghĩa, không phải mọi lựa chọn đều rủi ro.
- Cần 1 màn hình debug/tuning nội bộ để chỉnh `leakProbability` theo playtest thực tế, không hardcode số cuối.

**Effort: Thấp–Trung bình** — chủ yếu là data model + 1 service resolve rò rỉ chạy theo step, tái dùng NPC repository và content template đã có.

---

## 2. Trace Meter — đồng hồ "dấu vết", dùng lại chính visual layer của Integrity Static

### 2.1. Khái niệm

Một chỉ số theo dõi mức độ "ồn ào/liều lĩnh" của người chơi trong case hiện tại — khác với Integrity Static (vốn chỉ gắn với *tiến độ* case) — nhưng được **hiển thị qua đúng lớp visual đã thiết kế sẵn** để tiết kiệm công sức, không cần thêm UI mới.

### 2.2. Data model bổ sung

```kotlin
// GameStateRepository.kt
data class TraceConfig(
    val openNewSiteWeight: Float = 0.02f,
    val failedPuzzleWeight: Float = 0.05f,
    val successfulHackWeight: Float = 0.03f,
    val rapidActionMultiplier: Float = 1.5f,   // nếu hành động cách nhau < 3s
    val naturalDecayPerSessionStart: Float = 0.05f
)

class GameStateRepository(/* ... */) {
    private var traceLevel: Float = 0f   // 0.0–1.0, theo case hiện tại

    fun registerAction(action: TraceAction, config: TraceConfig) { /* cập nhật traceLevel */ }
    fun currentStaticIntensity(): Float =
        maxOf(caseProgressStatic(), traceLevel)   // blend với Integrity Static gốc
}
```

### 2.3. Ngưỡng & hệ quả

| Mức | Trace | Hiệu ứng |
|---|---|---|
| Thấp | 0.0–0.3 | Không có gì thêm ngoài Integrity Static nền |
| Tăng nhẹ | 0.3–0.6 | Scanline tăng nhẹ, glitch 1-frame thỉnh thoảng, một số site hiện tag `[RECENTLY MODIFIED]` |
| Cao | 0.6–0.85 | Site từng vào có thể trả về `[ARCHIVED]` nếu chưa lưu (liên kết với mục 4), noise/manh mối giả xuất hiện nhiều hơn |
| Nguy hiểm | 0.85–1.0 | Bị "đăng xuất" cưỡng bức, phải hoàn thành 1 thử thách "dọn log" ngắn (dùng lại chính dạng puzzle Log Analysis, nhưng đảo ngược: chọn dòng log của MÌNH cần xóa) trước khi tiếp tục |

### 2.4. Tích hợp UI/UX

**Không hiển thị số Trace trực tiếp** (phá vỡ nguyên tắc Diegetic UI) — chỉ giao tiếp qua:
- Cường độ overlay Integrity Static đã có sẵn (`IntegrityStaticOverlay` trong `TerminalComponents.kt`)
- Status tag ở address bar: `[CONNECTION FLAGGED]` màu `accent.amber`, leo thang thành `accent.glitch` ở mức nguy hiểm

### 2.5. Cân bằng

Toàn bộ trọng số nằm trong `TraceConfig` (không hardcode rải rác trong code) để dễ tinh chỉnh sau playtest.

**Effort: Trung bình** — gần như không cần component UI mới, chỉ cần thêm nguồn input thứ 2 cho overlay đã tồn tại.

---

## 3. Link Map — bảng kết nối manh mối kiểu "corkboard điều tra"

### 3.1. Khái niệm

Notebook từ danh sách phẳng nâng thành một mặt phẳng nơi người chơi tự nối các entity đã khám phá để "chốt" giả thuyết, mở khóa bước tiếp theo — thay vì chỉ đọc rồi bấm tiếp.

### 3.2. Data model bổ sung

```kotlin
// CaseModel.kt
data class ClueNode(
    val id: String,
    val nodeType: NodeType,        // PERSON, ORGANIZATION, EVENT, LOCATION, ARTIFACT
    val label: String
)

data class Deduction(
    val nodeAId: String,
    val nodeBId: String,           // không quan trọng thứ tự
    val relationshipLabel: String,
    val unlocksClueId: String
)

// Người chơi
data class PlayerConnection(val nodeAId: String, val nodeBId: String, val confirmed: Boolean)
```

`Deduction` là bảng được **thiết kế thủ công theo từng case** (giống đáp án puzzle), không sinh procedural — giữ độ khó/logic luôn có chủ đích.

### 3.3. Logic cơ chế

1. Node được "mở khóa" trên Link Map ngay khi entity tương ứng xuất hiện trong Browser/Inbox (dùng chung dữ liệu với Notebook hiện tại).
2. Người chơi chọn node A → chọn node B → bấm `CONNECT`.
3. Nếu cặp `(A,B)` khớp với 1 `Deduction` đã định nghĩa → hiện `MATCH FOUND` (dùng lại đúng feedback pattern từ Puzzle Log Analysis để nhất quán toàn app) → mở khóa clue liên quan.
4. Nếu không khớp: **vẫn cho phép giữ đường nối lại**, không báo sai, không phạt — đây là "giả thuyết cá nhân" của người chơi. Chỉ có cặp đúng mới đẩy cốt truyện tiến lên. Không nên trừng phạt suy luận sai, vì sẽ làm người chơi ngại thử nghiệm.

### 3.4. Tích hợp UI/UX

- Node hiển thị dạng chip nhỏ tái dùng `BracketBox`/`StatusTag` đã có.
- Đường nối mặc định màu `border.ascii`; đường đã xác nhận đúng chuyển `accent.terminal`.
- Đây là màn hình phù hợp nhất để thể hiện Integrity Static dạng "bảng ngày càng nhiễu" khi case đi sâu.

### 3.5. Đề xuất phạm vi v1 (giảm độ phức tạp kỹ thuật)

Thay vì kéo-thả thật (phức tạp về gesture trong Compose), bản v1 nên dùng **chọn A → chọn B → bấm CONNECT** (2 lần tap). Chỉ nâng lên kéo-thả thật ở Phase 3 nếu thấy thực sự đáng đầu tư.

**Effort: Cao nhất trong 4 cơ chế** — nhưng có thể giảm đáng kể với phiên bản 2-tap thay vì drag-and-drop.

---

## 4. Site Decay — website có thể biến mất nếu không lưu trữ kịp

### 4.1. Khái niệm

Một số website quan trọng chỉ tồn tại trong giới hạn số "step" nhất định (đúng lore: NEXUS liên tục xóa/tạo lại Internet). Nếu không lưu vào Notebook trước khi hết hạn, nội dung chi tiết mất vĩnh viễn — chỉ còn khung trống.

### 4.2. Data model bổ sung

```kotlin
// CaseModel.kt
data class WebsiteModel(
    val id: String,
    val layoutType: LayoutType,
    val content: String,
    val decayAfterStep: Int? = null,   // null = không bao giờ decay
    var isArchived: Boolean = false
)
```

`decayAfterStep` tính theo **step trong case** (không phải thời gian thật) — giữ đúng cách tiếp cận offline-friendly của Phase 0/1 hiện tại (dữ liệu JSON tĩnh qua `CaseAssetLoader`), không cần server tính giờ thật.

### 4.3. Logic cơ chế

1. Khi `GameStateRepository.currentStep >= decayAfterStep` và `isArchived == false` → nội dung site bị thay bằng trạng thái `[ARCHIVED BY NEXUS]`, chỉ còn metadata, mất phần nội dung mấu chốt.
2. Người chơi phải **chủ động** lưu site vào Notebook trước ngưỡng đó để set `isArchived = true`, giữ nội dung vĩnh viễn.
3. (Tùy chọn, liên kết mục 2) Trace cao rút ngắn `decayAfterStep` một chút — NEXUS phản ứng nhanh hơn khi bạn gây chú ý.

### 4.4. Tích hợp UI/UX

- Không dùng đồng hồ đếm ngược lộ liễu (phá Diegetic UI) — chỉ gợi ý qua status tag ở address bar: `[INDEX: UNSTABLE]` màu `accent.amber` khi site còn trong khoảng vài step trước khi decay.
- Trạng thái đã decay dùng đúng Empty State pattern đã định nghĩa trong design skill: `[ARCHIVED BY NEXUS]`, màu `text.muted.readable`.

### 4.5. Cân bằng

Chỉ nên có **1–2 site decay mỗi case**, không áp dụng đại trà — nếu quá nhiều sẽ biến trải nghiệm thành chạy đua tốc độ, phá vỡ tinh thần "patient, forensic" đã đặt ra trong design philosophy.

**Effort: Trung bình** — chỉ cần 2 field mới trên `WebsiteModel` + 1 check trong `GameStateRepository`.

---

## 5. Đề xuất thứ tự triển khai

| Thứ tự | Cơ chế | Lý do |
|---|---|---|
| 1 | **Site Decay** | Effort thấp nhất, độc lập, kiểm chứng nhanh việc gắn mechanic vào cảm xúc "khẩn cấp" có hiệu quả không |
| 2 | **Trace Meter** | Tái dùng gần như 100% visual layer đã có, chỉ thêm logic — rủi ro kỹ thuật thấp |
| 3 | **Trust & Leak Network** | Cần thêm content templating cho phần "rò rỉ sinh nội dung mới", nhưng vẫn nằm trong hạ tầng sẵn có |
| 4 | **Link Map** | Effort cao nhất — chỉ nên làm sau khi 3 cơ chế trên đã chứng minh core loop hấp dẫn hơn, và nên bắt đầu bằng bản 2-tap thay vì kéo-thả thật |

---

*Tài liệu này bổ sung cho GDD v3 và Android Design Skill — Terminal Noir. Khi triển khai, ưu tiên đúng theo mục 5 để tránh lặp lại rủi ro phình scope đã nêu ở bản đánh giá đầu tiên.*