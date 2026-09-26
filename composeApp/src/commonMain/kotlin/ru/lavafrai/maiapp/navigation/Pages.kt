package ru.lavafrai.maiapp.navigation

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.json.Json
import kotlin.reflect.KType
import kotlin.reflect.typeOf


/**
 * One instance per type. NavArgument.equals compares NavType by identity, so with new instances the same graph built
 * again (e.g. when the language changes) looks new to NavController, and it resets the whole back stack
 */
@PublishedApi
internal val navTypes = mutableMapOf<Pair<KType, Boolean>, NavType<*>>()

@Suppress("UNCHECKED_CAST")
inline fun <reified T> navTypeOf(
    isNullableAllowed: Boolean = false,
    json: Json = Json,
): NavType<T> = navTypes.getOrPut(typeOf<T>() to isNullableAllowed) {
    object : NavType<T>(isNullableAllowed = isNullableAllowed) {
        override fun get(bundle: SavedState, key: String): T? =
            bundle.read { if (!contains(key) || isNull(key)) null else getString(key) }
                ?.let(json::decodeFromString)

        override fun parseValue(value: String): T = json.decodeFromString(value)

        override fun serializeAsValue(value: T): String = json.encodeToString(value)

        override fun put(bundle: SavedState, key: String, value: T) =
            bundle.write { putString(key, json.encodeToString(value)) }
    }
} as NavType<T>
