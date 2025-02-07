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

class MyMaiApi(
    private val httpClient: HttpClient = HttpClientProvider.default,
    // private val baseUrl: String = "https://api.github.com",
) {

}