package com.example.internet_explorer.app.data.local

import com.google.gson.Gson
import com.google.gson.JsonElement

// ---------- case.json ----------
data class CaseJson(
    val caseId: String,
    val title: String,
    val organizationId: String,
    val npcIds: List<String>,
    val websiteIds: List<String>,
    val puzzleIds: List<String>,
    val clueIds: List<String>,
    val emailIds: List<String>,
    val steps: List<CaseStepJson>
)

data class CaseStepJson(
    val order: Int,
    val type: String,
    val refId: String,
    val description: String
)

// ---------- organization.json ----------
data class OrganizationJson(
    val id: String,
    val name: String,
    val foundedYear: Int,
    val vanishedYear: Int,
    val description: String
)

// ---------- npcs.json ----------
data class NpcJson(
    val id: String,
    val name: String,
    val role: String,
    val organizationId: String,
    val voiceStyle: String,
    val relationshipState: String,
    val knownFactIds: List<String>,
    val dialogue: List<NpcDialogueJson>
)

data class NpcDialogueJson(
    val topic: String,
    val requiresRelationship: String,
    val text: String
)

// ---------- websites.json ----------
// `content` giữ nguyên dạng JsonElement thô -- giống hệt cách WebsiteEntity.contentJson
// được lưu ở backend (mục 13.3.2 GDD). Parse cụ thể theo layoutType chỉ khi cần render,
// không parse trước ở tầng loader.
//
// decayAfterStep (gameplay-mechanics-phase2.md mục 4): số "step" trong case, không phải
// thời gian thật -- null nghĩa là site không bao giờ decay. Luôn khai báo tường minh
// "decayAfterStep": null trong JSON cho site không decay (không bỏ trống field), đúng
// pattern đã dùng cho unlockCondition ở EmailJson để tránh phụ thuộc hành vi Gson với
// field bị thiếu hẳn.
data class WebsiteJson(
    val id: String,
    val layoutType: String,
    val organizationId: String,
    val url: String,
    val content: JsonElement,
    val revealedFactIds: List<String>,
    val decayAfterStep: Int? = null
)

data class PortfolioContent(
    val heroTitle: String,
    val heroTagline: String,
    val aboutSection: String,
    val teamSection: List<TeamMember>,
    val projectsSection: List<ProjectItem>,
    val footerNote: String
)
data class TeamMember(val name: String, val title: String, val npcId: String? = null)
data class ProjectItem(val name: String, val status: String)

data class WikiContent(
    val title: String,
    val infobox: Map<String, String>,
    val sections: List<WikiSection>
)
data class WikiSection(val heading: String, val text: String)

data class ForumContent(
    val threadTitle: String,
    val posts: List<ForumPost>
)
data class ForumPost(
    val author: String,
    val text: String,
    val npcId: String? = null,
    val note: String? = null
)

data class SocialContent(
    val profileName: String,
    val npcId: String?,
    val bio: String,
    val posts: List<SocialPost>
)
data class SocialPost(val date: String, val text: String)

sealed interface WebsiteContent {
    data class Portfolio(val data: PortfolioContent) : WebsiteContent
    data class Wiki(val data: WikiContent) : WebsiteContent
    data class Forum(val data: ForumContent) : WebsiteContent
    data class Social(val data: SocialContent) : WebsiteContent
}

fun WebsiteJson.parseContent(gson: Gson): WebsiteContent = when (layoutType) {
    "company_portfolio" -> WebsiteContent.Portfolio(
        gson.fromJson(
            content,
            PortfolioContent::class.java
        )
    )
    "wiki_archive" -> WebsiteContent.Wiki(gson.fromJson(content, WikiContent::class.java))
    "forum" -> WebsiteContent.Forum(gson.fromJson(content, ForumContent::class.java))
    "social_media" -> WebsiteContent.Social(gson.fromJson(content, SocialContent::class.java))
    else -> throw IllegalArgumentException("layoutType không xác định: $layoutType")
}

// ---------- world_facts.json ----------
data class WorldFactJson(
    val id: String,
    val entityType: String,
    val entityId: String,
    val factText: String,
    val sourceEntityId: String
)

// ---------- emails.json ----------
data class EmailJson(
    val id: String,
    val folder: String,
    val subject: String,
    val senderName: String,
    val npcSenderId: String?,
    val body: String,
    val unlockCondition: String?
)

// ---------- puzzle.json ----------
data class PuzzleJson(
    val id: String,
    val caseId: String,
    val type: String,
    val title: String,
    val logLines: List<LogLineJson>,
    val correctAnomalyIds: List<String>,
    val unlocksClueId: String,
    val hintText: String
)

data class LogLineJson(
    val id: String,
    val text: String,
    val isAnomaly: Boolean,
    val anomalyReason: String? = null
)

// ---------- clues.json ----------
data class ClueJson(
    val id: String,
    val caseId: String,
    val description: String,
    val unlockedByPuzzleId: String,
    val relatedEntityId: String,
    val relatedFactIds: List<String>
)