package ru.lavafrai.maiapp.data.repositories

import io.ktor.client.*
import ru.lavafrai.maiapp.BuildConfig.API_BASE_URL
import ru.lavafrai.maiapp.models.maidata.MaiDataItem
import ru.lavafrai.maiapp.models.maidata.MaiDataManifest
import ru.lavafrai.maiapp.network.MaiApi

class MaiDataRepository(
    httpClient: HttpClient = baseHttpClient,
    baseUrl: String = API_BASE_URL,
): BaseRepository() {
    private val api = MaiApi(httpClient, baseUrl)

    suspend fun getData(): MaiDataManifest {
        val manifest = api.data()
        return localizeManifest(manifest)
    }

    /** 服务器返回的信息页数据为俄语，这里在客户端做中文映射 */
    private fun localizeManifest(manifest: MaiDataManifest): MaiDataManifest {
        val language = ru.lavafrai.maiapp.data.settings.ApplicationSettings.state.value.language
            ?: ru.lavafrai.maiapp.localizers.appLocaleTag().take(2)
        if (!language.startsWith("zh")) return manifest

        return manifest.copy(data = manifest.data.map { item ->
            item.copy(
                name = zhMap[item.name] ?: item.name,
                subtitle = item.subtitle?.let { zhMap[it] ?: it },
                category = item.category?.let { zhMap[it] ?: it },
            )
        })
    }

    companion object {
        private val zhMap = mapOf(
            // 分类
            "Преподаватели" to "教师",
            "Студгородок" to "校园",
            "Жизнь" to "生活",
            // 条目
            "Расписания преподавателей" to "教师课表",
            "О преподавателях" to "教师评价",
            "План студгородка" to "校园地图",
            "Столовые и буфеты" to "食堂与小卖部",
            "Библиотеки" to "图书馆",
            "Спортивные секции" to "体育社团",
            "Маёвский словарик" to "MAI 词典",
            "Творческие коллективы" to "文艺社团",
            "Студенческие организации" to "学生组织",
        )
    }
}
