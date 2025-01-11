package ru.lavafrai.maiapp.models.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GitHubTreeItemType {
    @SerialName("blob") BLOB,
    @SerialName("tree") TREE,
    @SerialName("commit") COMMIT,
}