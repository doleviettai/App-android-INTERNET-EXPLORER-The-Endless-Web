package com.example.internet_explorer.app.feature.browser

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.internet_explorer.app.data.local.ForumContent
import com.example.internet_explorer.app.data.local.PortfolioContent
import com.example.internet_explorer.app.data.local.SocialContent
import com.example.internet_explorer.app.data.local.WebsiteContent
import com.example.internet_explorer.app.data.local.WikiContent
import com.example.internet_explorer.app.ui.components.AddressBar
import com.example.internet_explorer.app.ui.components.BracketCard
import com.example.internet_explorer.app.ui.components.EmptyState
import com.example.internet_explorer.app.ui.components.StatusTag
import com.example.internet_explorer.app.ui.components.TerminalButton
import com.example.internet_explorer.app.ui.components.TypewriterText
import com.example.internet_explorer.app.ui.theme.*

@Composable
fun BrowserScreen(
    entityId: String,
    viewModel: BrowserViewModel = viewModel()
) {
    LaunchedEffect(entityId) { viewModel.openWebsite(entityId) }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when (val state = uiState) {
            is BrowserUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentTerminal)
                }
            }
            is BrowserUiState.NotFound -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "ERROR 404: NODE_NOT_FOUND. RETRY.",
                        color = AccentGlitch,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is BrowserUiState.Decayed -> {
                Column(Modifier.fillMaxSize()) {
                    AddressBar(command = state.website.url, isVerified = false)
                    Spacer(Modifier.height(32.dp))
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            message = "[ARCHIVED BY NEXUS]",
                            flavorText = "Nội dung đã bị xóa khỏi hệ thống. Không thể khôi phục."
                        )
                    }
                }
            }
            is BrowserUiState.Success -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Thanh địa chỉ chuẩn GDD
                    AddressBar(
                        command = state.website.url,
                        isVerified = state.website.id == "site_social" || state.website.id == "site_forum"
                    )

                    // Site Decay (gameplay-mechanics-phase2.md mục 4.4): cảnh báo âm thầm,
                    // không đồng hồ đếm ngược lộ liễu -- chỉ 1 tag khi còn 1-2 step trước decay.
                    if (state.isUnstable) {
                        Spacer(Modifier.height(8.dp))
                        StatusTag(text = "[INDEX: UNSTABLE]", color = AccentAmber)
                    }

                    Spacer(Modifier.height(16.dp))

                    when (val content = state.content) {
                        is WebsiteContent.Portfolio -> PortfolioView(content.data)
                        is WebsiteContent.Wiki -> WikiView(content.data)
                        is WebsiteContent.Forum -> ForumView(content.data)
                        is WebsiteContent.Social -> SocialView(content.data)
                    }

                    // Chỉ site có nguy cơ decay (decayAfterStep != null) mới cần nút lưu.
                    if (state.website.decayAfterStep != null) {
                        Spacer(Modifier.height(16.dp))
                        if (state.isArchived) {
                            StatusTag(text = "✓ ĐÃ LƯU VÀO NOTEBOOK", color = AccentTerminal)
                        } else {
                            TerminalButton(
                                text = "[ LƯU VÀO NOTEBOOK ]",
                                onClick = { viewModel.archiveCurrentWebsite() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioView(data: PortfolioContent) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        BracketCard(isFocused = true) {
            Text(
                text = data.heroTitle,
                style = MaterialTheme.typography.titleLarge,
                color = AccentTerminal,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = data.heroTagline,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }

        Text(
            text = "> ABOUT_US.DAT",
            style = MaterialTheme.typography.titleMedium,
            color = AccentTerminal,
            fontWeight = FontWeight.Bold
        )
        BracketCard {
            Text(
                text = data.aboutSection,
                style = MaterialTheme.typography.bodyLarge,
                color = TextMutedReadable
            )
        }

        Text(
            text = "> TEAM_DIRECTORY.CFG",
            style = MaterialTheme.typography.titleMedium,
            color = AccentTerminal,
            fontWeight = FontWeight.Bold
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.teamSection.forEach { member ->
                val isSignal = member.npcId != null // Nếu có NPC liên kết thì là Signal (Ví dụ: Lena Torres)
                BracketCard(isFocused = isSignal) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSignal) TextPrimary else TextNoise,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = member.title,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedReadable
                            )
                        }
                        if (isSignal) {
                            StatusTag(text = "SIGNAL_SOURCE", color = AccentTerminal)
                        } else {
                            StatusTag(text = "UNVERIFIED", color = TextMuted)
                        }
                    }
                }
            }
        }

        Text(
            text = "> ACTIVE_PROJECTS.LOG",
            style = MaterialTheme.typography.titleMedium,
            color = AccentTerminal,
            fontWeight = FontWeight.Bold
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.projectsSection.forEach { project ->
                BracketCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        // Project Absolom là điểm mấu chốt, đánh dấu đỏ nguy hiểm (AccentGlitch)
                        StatusTag(
                            text = project.status.uppercase(),
                            color = if (project.name.contains("Absolom")) AccentGlitch else AccentAmber
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = data.footerNote,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun WikiView(data: WikiContent) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = data.title,
            style = MaterialTheme.typography.titleLarge,
            color = AccentTerminal,
            fontWeight = FontWeight.Bold
        )

        // Infobox bọc trong BracketCard gọn gàng
        Text(
            text = "> META_INFOBOX.DAT",
            style = MaterialTheme.typography.titleSmall,
            color = AccentAmber
        )
        BracketCard(isFocused = true) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                data.infobox.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$key:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMutedReadable,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Các Section chia nhỏ
        data.sections.forEachIndexed { index, section ->
            val sectionNum = (index + 1).toString().padStart(2, '0')
            Text(
                text = "== SECTION $sectionNum: ${section.heading.uppercase()} ==",
                style = MaterialTheme.typography.titleMedium,
                color = AccentTerminal,
                fontWeight = FontWeight.Bold
            )
            BracketCard {
                Text(
                    text = section.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMutedReadable
                )
            }
        }
    }
}

@Composable
private fun ForumView(data: ForumContent) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = data.threadTitle,
            style = MaterialTheme.typography.titleLarge,
            color = AccentTerminal,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))

        data.posts.forEach { post ->
            val isVerified = post.npcId != null // Post của Lena Torres (L.T.) là Signal
            BracketCard(isFocused = isVerified) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "USER: ${post.author}",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isVerified) AccentTerminal else TextNoise
                        )
                        if (isVerified) {
                            StatusTag(text = "SIGNAL_CONFIRMED", color = AccentTerminal)
                        } else {
                            StatusTag(text = "UNVERIFIED", color = TextMuted)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = post.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isVerified) TextPrimary else TextMutedReadable
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialView(data: SocialContent) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Profile Header
        BracketCard(isFocused = true) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACCOUNT: ${data.profileName.uppercase()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentTerminal,
                        fontWeight = FontWeight.Bold
                    )
                    StatusTag(text = "SIGNAL_SOURCE", color = AccentTerminal)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = data.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMutedReadable
                )
            }
        }

        Text(
            text = "> RECENT_POSTS.LOG",
            style = MaterialTheme.typography.titleMedium,
            color = AccentTerminal,
            fontWeight = FontWeight.Bold
        )

        data.posts.forEach { post ->
            BracketCard {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "[TIMESTAMP: ${post.date}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentAmber,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ACCESS: PUBLIC",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    // Bài viết của nguồn Signal chạy chữ typewriter tạo cảm giác phục hồi dữ liệu trực tiếp
                    TypewriterText(
                        text = post.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary,
                        speedMs = 10
                    )
                }
            }
        }
    }
}