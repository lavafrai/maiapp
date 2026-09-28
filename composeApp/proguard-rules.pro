-dontobfuscate
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

# Classes created by name through the no-arg constructor. Their libraries keep them with member-less -keep rules
# (Room 2.2 and WorkManager 2.7 come with Glance), which don't keep the constructor since AGP 9 (strict R8 full mode)
# Room creates this WorkManager database implementation at startup
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
# WorkManager creates it for every one-time work, e.g. the widget's Glance session; without it the widget never renders
-keepclassmembers class * extends androidx.work.InputMerger { public <init>(); }
# Glance creates actionRunCallback targets, e.g. the widget's refresh button
-keepclassmembers class * implements androidx.glance.appwidget.action.ActionCallback { public <init>(); }

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
