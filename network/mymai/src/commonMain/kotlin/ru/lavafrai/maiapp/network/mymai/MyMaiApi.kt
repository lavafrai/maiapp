package ru.lavafrai.maiapp.network.mymai

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ru.lavafrai.maiapp.HttpClientProvider
import ru.lavafrai.maiapp.JsonProvider
import ru.lavafrai.maiapp.models.account.*
import ru.lavafrai.maiapp.network.mymai.exceptions.AuthenticationServerException
import ru.lavafrai.maiapp.network.mymai.exceptions.InvalidLoginOrPasswordException
import kotlinx.serialization.SerializationException

class MyMaiApi(
    private val credentials: MyMaiCredentials,
    private val httpClient: HttpClient = HttpClientProvider.default,
) {
    private suspend inline fun <reified T>method(methodName: String, urlQueryParams: Map<String, String> = mapOf()): T {
        val response = client.get {
            url {
                protocol = URLProtocol.HTTPS
                host = "my.mai.ru"
                encodedPath = methodName

                urlQueryParams.forEach { (key, value) ->
                    parameters.append(key, value)
                }
            }
            headers {
                append("Cookie", "kk_access=${credentials.accessToken}")
            }
        }


        return response.body()
    }

    suspend fun studentInfo() = method<StudentInfo>("/api_student/students/info/")
    suspend fun studentMarks(studentCode: String) = method<StudentMarks>("/api_student/students/grades/", urlQueryParams = mapOf("student_code" to studentCode))
    suspend fun applicants() = method<Applicants>("/api_abit/applicants/")
    suspend fun certificates(): List<Certificate> {
        return try {
            method<Certificates>("/api_services/get_certificates").certificates
        }
        catch (e: SerializationException) {
            listOf()
        }
    }
    suspend fun person() = applicants().person

    companion object {
        val client = HttpClientProvider.default
        val json = JsonProvider.tolerantJson

        @OptIn(InternalAPI::class)
        suspend fun authorize(login: String, password: String): MyMaiApi {
            val cookieResponse =
                client.get("https://esia.mai.ru/auth/realms/lk_mai/protocol/openid-connect/auth?client_id=proxy&redirect_uri=https%3A%2F%2Fmy.mai.ru%2F&response_type=code&scope=openid&nonce=850dea0b-baac-42bd-a922-1acc62f52fdb&state=f1832d85-94f6-45eb-9009-d00da51fda60")
            val cookies = cookieResponse.headers.getAll("Set-Cookie") ?: throw AuthenticationServerException()

            val authSessionId = cookies.first { it.startsWith("AUTH_SESSION_ID") }.split(";")[0]
            val authSessionIdLegacy = cookies.first { it.startsWith("AUTH_SESSION_ID_LEGACY") }.split(";")[0]
            val kcRestart = cookies.first { it.startsWith("KC_RESTART") }.split(";")[0]
            val authCookies = listOf(authSessionId, authSessionIdLegacy, kcRestart)

            val doAuthResponse: suspend (String) -> HttpResponse = { url: String ->
                client.submitForm(
                    url,
                    formParameters = Parameters.build { append("username", login); append("password", password) }) {
                    headers {
                        append("Cookie", authCookies.joinToString("; "))
                    }
                }
            }

            val tabIdUrlResponse = doAuthResponse("https://esia.mai.ru/auth/realms/lk_mai/login-actions/authenticate?client_id=proxy")
            val tabIdUrl = tabIdUrlResponse.headers["Location"] ?: throw AuthenticationServerException()

            val firstRepeatResponse = doAuthResponse(tabIdUrl)

            val authUrl = Ksoup.parse(firstRepeatResponse.bodyAsText()).select("#kc-form-login").attr("action")
            val authResponse = doAuthResponse(authUrl)
            val code =
                authResponse.headers["Location"]?.split("code=")?.get(1) ?: throw InvalidLoginOrPasswordException()
            // println(code)

            val tokenResponse = client.post("https://esia.mai.ru/auth/realms/lk_mai/protocol/openid-connect/token") {
                body = FormDataContent(Parameters.build {
                    append("client_id", "proxy")
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("redirect_uri", "https://my.mai.ru/")
                })
            }
            val credentials: MyMaiCredentials = json.decodeFromString(tokenResponse.bodyAsText())

            return MyMaiApi(credentials)
        }
    }
}