-keepattributes *Annotation*,Signature,InnerClasses
-keepattributes SourceFile,LineNumberTable

-keep public class * extends java.lang.Exception

# -keepnames class <1>$$serializer {
#     static <1>$$serializer INSTANCE;
# }

-keep class io.ktor.serialization.kotlinx.KotlinxSerializationExtensionProvider { *; }
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider { *; }
-keep class ru.lavafrai.maiapp.data.settings.ApplicationSettingsData { *; }
-keep class ru.lavafrai.maiapp.models.** { *; }
-keepclasseswithmembers class ru.lavafrai.maiapp.navigation.pages.** { *; }

-dontwarn com.google.api.client.http.GenericUrl
-dontwarn com.google.api.client.http.HttpHeaders
-dontwarn com.google.api.client.http.HttpRequest
-dontwarn com.google.api.client.http.HttpRequestFactory
-dontwarn com.google.api.client.http.HttpResponse
-dontwarn com.google.api.client.http.HttpTransport
-dontwarn com.google.api.client.http.javanet.NetHttpTransport$Builder
-dontwarn com.google.api.client.http.javanet.NetHttpTransport
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn org.joda.time.Instant
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn io.ktor.utils.io.jvm.nio.WritingKt