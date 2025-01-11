package ru.lavafrai.maiapp.models.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubTreeItem(
    val path: String,
    val mode: String,
    val type: GitHubTreeItemType,
    val sha: String,
    val size: Long? = null,
    val url: String? = null,
)