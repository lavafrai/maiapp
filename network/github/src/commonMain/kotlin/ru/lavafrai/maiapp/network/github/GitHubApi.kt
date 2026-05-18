package ru.lavafrai.maiapp.network.github

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.io.files.FileNotFoundException
import ru.lavafrai.maiapp.HttpClientProvider
import ru.lavafrai.maiapp.models.github.GitHubBlob
import ru.lavafrai.maiapp.models.github.GitHubRepo
import ru.lavafrai.maiapp.models.github.GitHubTree
import ru.lavafrai.maiapp.models.github.GitHubTreeItemType

class GitHubApi(
    private val httpClient: HttpClient = HttpClientProvider.default,
    private val baseUrl: String = "https://api.github.com",
) {
    suspend fun listRepoFiles(owner: String, repo: String, branch: String = "main") = httpClient
        .get("$baseUrl/repos/$owner/$repo/git/trees/$branch")
        .body<GitHubTree>()

    suspend fun listRepoFiles(gitHubRepo: GitHubRepo, branch: String = "main") = listRepoFiles(gitHubRepo.owner, gitHubRepo.name, branch)

    suspend fun getBlob(owner: String, repo: String, sha: String) = httpClient
        .get("$baseUrl/repos/$owner/$repo/git/blobs/$sha")
        .body<GitHubBlob>()

    suspend fun getBlob(gitHubRepo: GitHubRepo, sha: String) = getBlob(gitHubRepo.owner, gitHubRepo.name, sha)

    suspend fun resolvePath(
        owner: String,
        repo: String,
        path: String,
        branch: String = "main",
        type: GitHubTreeItemType? = null
    ): String {
        val pathParts = path.trim('/').split("/").filter { it.isNotEmpty() }
        val tree = listRepoFiles(owner, repo, branch)
        if (pathParts.isEmpty()) {
            if (type == null || type == GitHubTreeItemType.TREE) return tree.sha
            else throw FileNotFoundException("File has type ${GitHubTreeItemType.TREE} instead of $type: $path")
        }

        val item = tree.tree.find { it.path == pathParts.first() } ?: throw FileNotFoundException("File not found: $path")
        if (pathParts.size == 1) {
            if (type == null || type == item.type) return item.sha
            throw FileNotFoundException("File has type ${item.type} instead of $type: $path")
        }
        return when (item.type) {
            GitHubTreeItemType.BLOB -> throw FileNotFoundException("File not found: $path")
            GitHubTreeItemType.TREE -> resolvePath(
                owner,
                repo,
                pathParts.drop(1).joinToString("/"),
                item.sha,
                type,
            )
            GitHubTreeItemType.COMMIT -> throw FileNotFoundException("File not found: $path")
        }
    }

    suspend fun resolvePath(gitHubRepo: GitHubRepo, path: String, type: GitHubTreeItemType? = null) = resolvePath(
        owner = gitHubRepo.owner,
        repo = gitHubRepo.name,
        path = path,
        type = type
    )

    suspend fun listDir(repo: GitHubRepo, path: String) = listRepoFiles(repo, resolvePath(repo, path, type = GitHubTreeItemType.TREE)).tree.map { it.path }
    suspend fun readBlob(repo: GitHubRepo, path: String) = getBlob(repo, resolvePath(repo, path, type = GitHubTreeItemType.BLOB)).decodedContent
}

suspend fun main() {
    val api = GitHubApi()
    val repo = GitHubRepo.parse("lavafrai/maidata")
    val dir = api.readBlob(repo, "cafeteries.lm")
    println(dir)

    /*val file = api.getBlob(repo, dir)
    println(file)*/
    //println(file.decodedContent)
}