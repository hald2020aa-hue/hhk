package com.example.data

data class ExtensionSource(
    val name: String,
    val lang: String,
    val id: String,
    val baseUrl: String
)

data class TachiExtension(
    val name: String,
    val pkg: String,
    val apk: String,
    val lang: String,
    val code: Int,
    val version: String,
    val nsfw: Int = 0,
    val sources: List<ExtensionSource> = emptyList(),
    val iconUrl: String,
    val apkWebUrl: String,
    val repoPageUrl: String
) {
    val cleanDisplayName: String
        get() = name.removePrefix("Tachiyomi: ").trim()
}

data class GitHubFileEntry(
    val name: String,
    val path: String,
    val type: String, // "file" or "dir"
    val sizeBytes: Long,
    val htmlUrl: String,
    val downloadUrl: String?,
    val descriptionAr: String,
    val descriptionEn: String,
    val previewContent: String? = null
)

data class RepoMetadata(
    val owner: String,
    val repoName: String,
    val branch: String,
    val gitCloneUrl: String,
    val htmlUrl: String,
    val rawIndexMinUrl: String,
    val rawIndexUrl: String,
    val description: String,
    val starsCount: Int,
    val forksCount: Int,
    val lastUpdated: String,
    val isLiveFromNetwork: Boolean,
    val statusNoteAr: String,
    val statusNoteEn: String
)

data class ExtractionSnapshot(
    val metadata: RepoMetadata,
    val extensions: List<TachiExtension>,
    val files: List<GitHubFileEntry>,
    val rawIndexPreview: String,
    val readmeContent: String
)

enum class RepoPreset(
    val labelAr: String,
    val labelEn: String,
    val owner: String,
    val repo: String,
    val defaultBranch: String
) {
    DRANIXX(
        labelAr = "Dranixx / tachiyomi-extensions-apk (المطلوب)",
        labelEn = "Dranixx / tachiyomi-extensions-apk (Target)",
        owner = "Dranixx",
        repo = "tachiyomi-extensions-apk",
        defaultBranch = "repo"
    ),
    KEIYOUSHI(
        labelAr = "Keiyoushi / extensions (المستودع الرسمي المجتمعي)",
        labelEn = "Keiyoushi / extensions (Community Standard)",
        owner = "keiyoushi",
        repo = "extensions",
        defaultBranch = "repo"
    )
}
