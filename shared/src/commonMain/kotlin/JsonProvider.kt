import kotlinx.serialization.json.Json

object JsonProvider {
    val tolerantJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}