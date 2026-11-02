package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ExtractionSnapshot
import com.example.data.GitHubFileEntry
import com.example.data.RepoExtractorService
import com.example.data.RepoPreset
import com.example.data.TachiExtension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab {
    EXTENSIONS,
    GITHUB_FILES,
    REPO_SETUP
}

data class RepoUiState(
    val isLoading: Boolean = true,
    val isArabic: Boolean = true,
    val currentTab: AppTab = AppTab.EXTENSIONS,
    val snapshot: ExtractionSnapshot? = null,
    val searchQuery: String = "",
    val selectedLangFilter: String = "ALL",
    val availableLanguages: List<String> = listOf("ALL", "ar", "all", "en", "fr", "es", "ja"),
    val bookmarkedPackages: Set<String> = emptySet(),
    val showBookmarksOnly: Boolean = false,
    val selectedExtension: TachiExtension? = null,
    val selectedFileEntry: GitHubFileEntry? = null,
    val customRepoUrlInput: String = "https://github.com/Dranixx/tachiyomi-extensions-apk.git",
    val customBranchInput: String = "repo",
    val activePreset: RepoPreset = RepoPreset.DRANIXX,
    val statusBannerMessage: String? = null
) {
    val filteredExtensions: List<TachiExtension>
        get() {
            val all = snapshot?.extensions ?: emptyList()
            return all.filter { ext ->
                val matchesBookmark = !showBookmarksOnly || bookmarkedPackages.contains(ext.pkg)
                val matchesLang = selectedLangFilter == "ALL" ||
                    ext.lang.equals(selectedLangFilter, ignoreCase = true) ||
                    ext.sources.any { it.lang.equals(selectedLangFilter, ignoreCase = true) }
                val q = searchQuery.trim()
                val matchesQuery = q.isEmpty() ||
                    ext.name.contains(q, ignoreCase = true) ||
                    ext.pkg.contains(q, ignoreCase = true) ||
                    ext.apk.contains(q, ignoreCase = true) ||
                    ext.sources.any {
                        it.name.contains(q, ignoreCase = true) ||
                            it.baseUrl.contains(q, ignoreCase = true)
                    }
                matchesBookmark && matchesLang && matchesQuery
            }
        }
}

class RepoViewModel(
    private val extractorService: RepoExtractorService = RepoExtractorService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RepoUiState())
    val uiState: StateFlow<RepoUiState> = _uiState.asStateFlow()

    init {
        extractFromPreset(RepoPreset.DRANIXX)
    }

    fun extractFromPreset(preset: RepoPreset) {
        _uiState.update {
            it.copy(
                isLoading = true,
                activePreset = preset,
                customRepoUrlInput = "https://github.com/${preset.owner}/${preset.repo}.git",
                customBranchInput = preset.defaultBranch,
                statusBannerMessage = null
            )
        }
        viewModelScope.launch {
            val result = extractorService.extractRepository(
                ownerInput = preset.owner,
                repoInput = preset.repo,
                preferredBranch = preset.defaultBranch
            )
            val langs = buildLanguageList(result.extensions)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    snapshot = result,
                    availableLanguages = langs,
                    selectedLangFilter = "ALL"
                )
            }
        }
    }

    fun extractCustomRepository() {
        val rawInput = _uiState.value.customRepoUrlInput.trim()
        val branchInput = _uiState.value.customBranchInput.trim().ifBlank { "repo" }
        if (rawInput.isBlank()) return

        _uiState.update { it.copy(isLoading = true, statusBannerMessage = null) }
        viewModelScope.launch {
            val (owner, repo) = extractorService.parseOwnerAndRepo(rawInput, "tachiyomi-extensions-apk")
            val result = extractorService.extractRepository(
                ownerInput = owner,
                repoInput = repo,
                preferredBranch = branchInput
            )
            val langs = buildLanguageList(result.extensions)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    snapshot = result,
                    availableLanguages = langs,
                    selectedLangFilter = "ALL",
                    statusBannerMessage = if (state.isArabic) {
                        "تم استخراج المستودع $owner/$repo (${result.extensions.size} إضافة)"
                    } else {
                        "Extracted $owner/$repo (${result.extensions.size} extensions)"
                    }
                )
            }
        }
    }

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isArabic = !it.isArabic) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectLangFilter(lang: String) {
        _uiState.update { it.copy(selectedLangFilter = lang) }
    }

    fun toggleBookmarkFilter() {
        _uiState.update { it.copy(showBookmarksOnly = !it.showBookmarksOnly) }
    }

    fun toggleBookmark(pkg: String) {
        _uiState.update { state ->
            val updated = state.bookmarkedPackages.toMutableSet()
            if (!updated.add(pkg)) {
                updated.remove(pkg)
            }
            state.copy(bookmarkedPackages = updated)
        }
    }

    fun selectExtension(extension: TachiExtension?) {
        _uiState.update { it.copy(selectedExtension = extension) }
    }

    fun selectFileEntry(entry: GitHubFileEntry?) {
        _uiState.update { it.copy(selectedFileEntry = entry) }
    }

    fun updateCustomRepoUrl(url: String) {
        _uiState.update { it.copy(customRepoUrlInput = url) }
    }

    fun updateCustomBranch(branch: String) {
        _uiState.update { it.copy(customBranchInput = branch) }
    }

    fun showFeedbackMessage(message: String) {
        _uiState.update { it.copy(statusBannerMessage = message) }
    }

    fun clearFeedbackMessage() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    private fun buildLanguageList(extensions: List<TachiExtension>): List<String> {
        val counts = mutableMapOf<String, Int>()
        extensions.forEach { ext ->
            counts[ext.lang] = (counts[ext.lang] ?: 0) + 1
        }
        val sorted = counts.keys.sortedWith(
            compareByDescending<String> { it == "ar" }
                .thenByDescending { it == "all" }
                .thenByDescending { it == "en" }
                .thenByDescending { counts[it] ?: 0 }
        )
        return listOf("ALL") + sorted
    }
}
