package ru.lavafrai.maiapp.models.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepo(
    val owner: String,
    val name: String,
) {
    val fullName: String
        get() = "$owner/$name"

    companion object {
        fun parse(fullName: String): GitHubRepo {
            val (owner, name) = fullName.split('/')
            return GitHubRepo(owner, name)
        }
    }
}