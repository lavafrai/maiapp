package ru.lavafrai.maiapp.models.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubTree(
    val sha: String,
    val url: String,
    val tree: List<GitHubTreeItem>,
    val truncated: Boolean,
)