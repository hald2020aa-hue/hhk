package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.RepoPreset
import com.example.ui.AppTab
import com.example.ui.ExtensionDetailBottomSheet
import com.example.ui.ExtensionItemCard
import com.example.ui.FileInspectorBottomSheet
import com.example.ui.GitHubFileRowCard
import com.example.ui.RepoHeroBanner
import com.example.ui.RepoUiState
import com.example.ui.RepoViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RobotoMonoFontFamily

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TachiRepoExplorerApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TachiRepoExplorerApp(
    repoViewModel: RepoViewModel = viewModel()
) {
    val uiState by repoViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // BackHandler for secondary tabs or active bottom sheets
    BackHandler(
        enabled = uiState.selectedExtension != null ||
            uiState.selectedFileEntry != null ||
            uiState.currentTab != AppTab.EXTENSIONS
    ) {
        when {
            uiState.selectedExtension != null -> repoViewModel.selectExtension(null)
            uiState.selectedFileEntry != null -> repoViewModel.selectFileEntry(null)
            uiState.currentTab != AppTab.EXTENSIONS -> repoViewModel.setTab(AppTab.EXTENSIONS)
        }
    }

    LaunchedEffect(uiState.statusBannerMessage) {
        val msg = uiState.statusBannerMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            repoViewModel.clearFeedbackMessage()
        }
    }

    val openExternalWebUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            repoViewModel.showFeedbackMessage(
                if (uiState.isArabic) "تعذر فتح الرابط الخارجي" else "Could not open external URL"
            )
        }
    }

    val copyToClipboard: (String, String) -> Unit = { text, confirmMsg ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("TachiRepo URL", text))
        repoViewModel.showFeedbackMessage(confirmMsg)
    }

    val layoutDirection = if (uiState.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpandedScreen = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Extension,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = if (uiState.isArabic) {
                                            "مستخرج إضافات تاتشيومي (GitHub)"
                                        } else {
                                            "TachiRepo GitHub Extractor"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Dranixx/tachiyomi-extensions-apk",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        },
                        actions = {
                            OutlinedButton(
                                onClick = { repoViewModel.toggleLanguage() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .minimumInteractiveComponentSize()
                                    .testTag("toggle_language_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isArabic) "EN" else "عربي",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.EXTENSIONS,
                                onClick = { repoViewModel.setTab(AppTab.EXTENSIONS) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Extension,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        if (uiState.isArabic) "الإضافات (Index)" else "Extensions"
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_extensions")
                            )
                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.GITHUB_FILES,
                                onClick = { repoViewModel.setTab(AppTab.GITHUB_FILES) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        if (uiState.isArabic) "ملفات المشروع" else "Repo Files"
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_files")
                            )
                            NavigationBarItem(
                                selected = uiState.currentTab == AppTab.REPO_SETUP,
                                onClick = { repoViewModel.setTab(AppTab.REPO_SETUP) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null
                                    )
                                },
                                label = {
                                    Text(
                                        if (uiState.isArabic) "الروابط والإعداد" else "Repo & Links"
                                    )
                                },
                                modifier = Modifier.testTag("nav_tab_setup")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        NavigationRail(
                            modifier = Modifier
                                .fillMaxHeight()
                                .testTag("side_navigation_rail")
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            NavigationRailItem(
                                selected = uiState.currentTab == AppTab.EXTENSIONS,
                                onClick = { repoViewModel.setTab(AppTab.EXTENSIONS) },
                                icon = { Icon(Icons.Default.Extension, contentDescription = null) },
                                label = { Text(if (uiState.isArabic) "الإضافات" else "Extensions") }
                            )
                            NavigationRailItem(
                                selected = uiState.currentTab == AppTab.GITHUB_FILES,
                                onClick = { repoViewModel.setTab(AppTab.GITHUB_FILES) },
                                icon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                label = { Text(if (uiState.isArabic) "الملفات" else "Files") }
                            )
                            NavigationRailItem(
                                selected = uiState.currentTab == AppTab.REPO_SETUP,
                                onClick = { repoViewModel.setTab(AppTab.REPO_SETUP) },
                                icon = { Icon(Icons.Default.Link, contentDescription = null) },
                                label = { Text(if (uiState.isArabic) "الروابط" else "Setup") }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 840.dp)
                        ) {
                            AnimatedVisibility(visible = uiState.isLoading) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("loading_indicator")
                                )
                            }

                            when (uiState.currentTab) {
                                AppTab.EXTENSIONS -> ExtensionsCatalogContent(
                                    uiState = uiState,
                                    onSearchChange = repoViewModel::updateSearchQuery,
                                    onLangSelect = repoViewModel::selectLangFilter,
                                    onToggleBookmarkFilter = repoViewModel::toggleBookmarkFilter,
                                    onToggleBookmark = repoViewModel::toggleBookmark,
                                    onSelectExtension = repoViewModel::selectExtension,
                                    onSyncClick = repoViewModel::extractCustomRepository,
                                    onOpenUrl = openExternalWebUrl,
                                    onCopyUrl = { url ->
                                        copyToClipboard(
                                            url,
                                            if (uiState.isArabic) {
                                                "تم نسخ رابط GitHub إلى الحافظة"
                                            } else {
                                                "Copied GitHub URL to clipboard"
                                            }
                                        )
                                    }
                                )

                                AppTab.GITHUB_FILES -> GitHubFilesTabContent(
                                    uiState = uiState,
                                    onInspectFile = repoViewModel::selectFileEntry,
                                    onOpenUrl = openExternalWebUrl,
                                    onCopyText = copyToClipboard
                                )

                                AppTab.REPO_SETUP -> RepoSetupTabContent(
                                    uiState = uiState,
                                    onPresetSelect = repoViewModel::extractFromPreset,
                                    onCustomUrlChange = repoViewModel::updateCustomRepoUrl,
                                    onCustomBranchChange = repoViewModel::updateCustomBranch,
                                    onExtractCustomClick = repoViewModel::extractCustomRepository,
                                    onOpenUrl = openExternalWebUrl,
                                    onCopyText = copyToClipboard
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sheet for Extension Details
            val activeExt = uiState.selectedExtension
            if (activeExt != null) {
                ExtensionDetailBottomSheet(
                    extension = activeExt,
                    isArabic = uiState.isArabic,
                    onDismiss = { repoViewModel.selectExtension(null) },
                    onOpenUrl = openExternalWebUrl,
                    onCopyText = copyToClipboard
                )
            }

            // Bottom Sheet for File Inspector
            val activeFile = uiState.selectedFileEntry
            if (activeFile != null) {
                val fallbackContent = when {
                    activeFile.name.endsWith(".json") -> uiState.snapshot?.rawIndexPreview.orEmpty()
                    else -> uiState.snapshot?.readmeContent.orEmpty()
                }
                FileInspectorBottomSheet(
                    fileEntry = activeFile,
                    fallbackContent = fallbackContent,
                    isArabic = uiState.isArabic,
                    onDismiss = { repoViewModel.selectFileEntry(null) },
                    onOpenUrl = openExternalWebUrl,
                    onCopyContent = { text ->
                        copyToClipboard(
                            text,
                            if (uiState.isArabic) "تم نسخ محتوى الملف" else "Copied file content"
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ExtensionsCatalogContent(
    uiState: RepoUiState,
    onSearchChange: (String) -> Unit,
    onLangSelect: (String) -> Unit,
    onToggleBookmarkFilter: () -> Unit,
    onToggleBookmark: (String) -> Unit,
    onSelectExtension: (com.example.data.TachiExtension) -> Unit,
    onSyncClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyUrl: (String) -> Unit
) {
    val extensions = uiState.filteredExtensions
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("extensions_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RepoHeroBanner(
                metadata = uiState.snapshot?.metadata,
                extensionCount = uiState.snapshot?.extensions?.size ?: 0,
                fileCount = uiState.snapshot?.files?.size ?: 0,
                isArabic = uiState.isArabic,
                onSyncClick = onSyncClick,
                onOpenGitHubRepo = onOpenUrl,
                onCopyCloneUrl = onCopyUrl
            )
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_extensions_input"),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        if (uiState.isArabic) {
                            "ابحث عن إضافة (مثل MangaLek, MangaDex, Azora)..."
                        } else {
                            "Search extension, package, or source domain..."
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchChange("") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = if (uiState.isArabic) "مسح البحث" else "Clear search"
                            )
                        }
                    }
                }
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = uiState.showBookmarksOnly,
                    onClick = onToggleBookmarkFilter,
                    label = {
                        Text(
                            if (uiState.isArabic) {
                                "المحفوظة (${uiState.bookmarkedPackages.size})"
                            } else {
                                "Saved (${uiState.bookmarkedPackages.size})"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("filter_bookmarks_chip")
                )

                uiState.availableLanguages.forEach { lang ->
                    FilterChip(
                        selected = uiState.selectedLangFilter == lang,
                        onClick = { onLangSelect(lang) },
                        label = {
                            Text(
                                when {
                                    lang == "ALL" && uiState.isArabic -> "كل اللغات"
                                    lang == "ALL" -> "All Languages"
                                    lang == "ar" && uiState.isArabic -> "العربية (AR)"
                                    lang == "all" && uiState.isArabic -> "متعدد اللغات (MULTI)"
                                    else -> lang.uppercase()
                                }
                            )
                        },
                        modifier = Modifier.testTag("lang_chip_$lang")
                    )
                }
            }
        }

        if (extensions.isEmpty() && !uiState.isLoading) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (uiState.isArabic) {
                                "لا توجد إضافات مطابقة لفلتر البحث الحالي"
                            } else {
                                "No extensions match your current search filter"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(extensions, key = { it.pkg }) { extension ->
                ExtensionItemCard(
                    extension = extension,
                    isBookmarked = uiState.bookmarkedPackages.contains(extension.pkg),
                    isArabic = uiState.isArabic,
                    onCardClick = { onSelectExtension(extension) },
                    onBookmarkToggle = { onToggleBookmark(extension.pkg) },
                    onOpenApkWebLink = { onOpenUrl(extension.apkWebUrl) }
                )
            }
        }
    }
}

@Composable
private fun GitHubFilesTabContent(
    uiState: RepoUiState,
    onInspectFile: (com.example.data.GitHubFileEntry) -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val files = uiState.snapshot?.files ?: emptyList()
    val rawIndexPreview = uiState.snapshot?.rawIndexPreview.orEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("github_files_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (uiState.isArabic) {
                            "شجرة ملفات مستودع GitHub المستخرجة"
                        } else {
                            "Extracted GitHub Repository File Tree"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.isArabic) {
                            "اضغط على أي ملف أو مجلد لمعاينة محتوياته المستخرجة (index.min.json / README.md / apk / icon) أو فتحه مباشرة في GitHub."
                        } else {
                            "Tap any file or folder to inspect its extracted contents or open it on GitHub."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(files, key = { it.path }) { entry ->
            GitHubFileRowCard(
                entry = entry,
                isArabic = uiState.isArabic,
                onInspectClick = { onInspectFile(entry) },
                onOpenGitHubUrl = { onOpenUrl(entry.htmlUrl) }
            )
        }

        if (rawIndexPreview.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "index.min.json (Extracted Schema)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = {
                                    onCopyText(
                                        rawIndexPreview,
                                        if (uiState.isArabic) {
                                            "تم نسخ مقتطف index.min.json"
                                        } else {
                                            "Copied index.min.json snippet"
                                        }
                                    )
                                },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = if (uiState.isArabic) "نسخ" else "Copy"
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = rawIndexPreview,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = RobotoMonoFontFamily,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RepoSetupTabContent(
    uiState: RepoUiState,
    onPresetSelect: (RepoPreset) -> Unit,
    onCustomUrlChange: (String) -> Unit,
    onCustomBranchChange: (String) -> Unit,
    onExtractCustomClick: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyText: (String, String) -> Unit
) {
    val meta = uiState.snapshot?.metadata
    val rawIndexMinUrl = meta?.rawIndexMinUrl
        ?: "https://raw.githubusercontent.com/Dranixx/tachiyomi-extensions-apk/repo/index.min.json"
    val gitCloneUrl = meta?.gitCloneUrl
        ?: "https://github.com/Dranixx/tachiyomi-extensions-apk.git"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("repo_setup_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Direct Repository Index URL Card for Tachiyomi / Mihon
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (uiState.isArabic) {
                                "رابط الفهرس المباشر (index.min.json)"
                            } else {
                                "Direct Repository Index URL (index.min.json)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = rawIndexMinUrl,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = RobotoMonoFontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onCopyText(
                                    rawIndexMinUrl,
                                    if (uiState.isArabic) {
                                        "تم نسخ رابط index.min.json!"
                                    } else {
                                        "Copied index.min.json URL!"
                                    }
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("copy_index_min_url_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isArabic) "نسخ رابط الفهرس" else "Copy Index URL"
                            )
                        }

                        OutlinedButton(
                            onClick = { onOpenUrl(rawIndexMinUrl) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("open_index_min_url_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = if (uiState.isArabic) "فتح الفهرس" else "Open Index",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Custom GitHub Repository Extractor Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (uiState.isArabic) {
                            "استخراج مستودع GitHub مباشر"
                        } else {
                            "Live GitHub Repository Extractor"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RepoPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = uiState.activePreset == preset,
                                onClick = { onPresetSelect(preset) },
                                label = {
                                    Text(if (uiState.isArabic) preset.labelAr else preset.labelEn)
                                },
                                modifier = Modifier.testTag("preset_chip_${preset.name}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = uiState.customRepoUrlInput,
                        onValueChange = onCustomUrlChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_repo_url_input"),
                        label = {
                            Text(
                                if (uiState.isArabic) {
                                    "رابط مستودع GitHub (أو owner/repo)"
                                } else {
                                    "GitHub Repo URL (or owner/repo)"
                                }
                            )
                        },
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.customBranchInput,
                            onValueChange = onCustomBranchChange,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("custom_branch_input"),
                            label = {
                                Text(if (uiState.isArabic) "الفرع (Branch)" else "Branch")
                            },
                            singleLine = true
                        )

                        Button(
                            onClick = onExtractCustomClick,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("extract_custom_repo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (uiState.isArabic) "استخراج الآن" else "Extract Now")
                        }
                    }
                }
            }
        }

        // Google Play Creator Program Compliance Notice Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (uiState.isArabic) {
                                "متوافق مع سياسات Google Play Creator Program"
                            } else {
                                "Google Play Creator Program Compliant"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.isArabic) {
                                "لتجنب صلاحية تثبيت الحزم المحظورة (REQUEST_INSTALL_PACKAGES / PackageInstaller) على برنامج Google Play Creator Program، يقوم التطبيق باستخراج وفحص بيانات المستودع وفتح روابط ملفات APK والمصادر عبر المتصفح الخارجي الآمن (Intent.ACTION_VIEW) دون أي صلاحيات حساسة."
                            } else {
                                "To comply with Google Play Creator Program policies prohibiting direct in-app APK installation (REQUEST_INSTALL_PACKAGES / PackageInstaller), this app extracts the repository index and opens APK/source links via zero-permission external browser intents (Intent.ACTION_VIEW)."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
