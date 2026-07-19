package com.example.internet_explorer.app.feature.browser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.internet_explorer.app.ui.components.TerminalCard
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

@Composable
fun BrowserScreen(
    entityId: String,
    viewModel: BrowserViewModel = viewModel()
) {
    LaunchedEffect(entityId) { viewModel.openWebsite(entityId) }
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = uiState) {
            is BrowserUiState.Loading -> CircularProgressIndicator()
            is BrowserUiState.NotFound -> Text("404 — Không tìm thấy trang này trong World State.")
            is BrowserUiState.Success -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = "> ${state.website.url}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))
                    when (val content = state.content) {
                        is WebsiteContent.Portfolio -> PortfolioView(content.data)
                        is WebsiteContent.Wiki -> WikiView(content.data)
                        is WebsiteContent.Forum -> ForumView(content.data)
                        is WebsiteContent.Social -> SocialView(content.data)
                    }
                }
            }
        }
    }
}

@Composable
private fun PortfolioView(data: PortfolioContent) {
    Text(data.heroTitle, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Text(data.heroTagline, style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(16.dp))
    Text(data.aboutSection, style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
    Text("Đội ngũ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    data.teamSection.forEach { member ->
        Text("• ${member.name} — ${member.title}", style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(16.dp))
    Text("Dự án", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    data.projectsSection.forEach { project ->
        Text("• ${project.name}: ${project.status}", style = MaterialTheme.typography.bodyMedium)
    }
    Spacer(Modifier.height(16.dp))
    Text(data.footerNote, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun WikiView(data: WikiContent) {
    Text(data.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    TerminalCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            data.infobox.forEach { (key, value) ->
                Text("$key: $value", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    data.sections.forEach { section ->
        Text(section.heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(section.text, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ForumView(data: ForumContent) {
    Text(data.threadTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    data.posts.forEach { post ->
        TerminalCard(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(post.author, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                Text(post.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SocialView(data: SocialContent) {
    Text(data.profileName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    Text(data.bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(16.dp))
    data.posts.forEach { post ->
        TerminalCard(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(post.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(post.text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}