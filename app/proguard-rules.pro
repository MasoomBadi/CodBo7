# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Kotlinx Serialization
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable classes
-keep,includedescriptorclasses class com.phoenix.companionforcodblackops7.**$$serializer { *; }
-keepclassmembers class com.phoenix.companionforcodblackops7.** {
    *** Companion;
}
-keepclasseswithmembers class com.phoenix.companionforcodblackops7.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep classes with @Serializable annotation
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# ============================================
# Retrofit & OkHttp
# ============================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.**

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep Retrofit service interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep API service interface
-keep interface com.phoenix.companionforcodblackops7.core.data.remote.api.Bo7ApiService { *; }
-keep interface com.phoenix.companionforcodblackops7.feature.feedback.data.remote.FeedbackApiService { *; }

-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ============================================
# Realm Kotlin
# ============================================
-keep class io.realm.kotlin.** { *; }
-keep class org.mongodb.kbson.** { *; }
-dontwarn io.realm.**

# Keep Realm entity classes
-keep class com.phoenix.companionforcodblackops7.feature.checklist.data.local.** { *; }
-keep class com.phoenix.companionforcodblackops7.feature.masterybadge.data.local.** { *; }
-keep class com.phoenix.companionforcodblackops7.core.data.local.** { *; }

# ============================================
# Hilt / Dagger
# ============================================
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Hilt generated classes
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }

# ============================================
# Firebase
# ============================================
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.firebase.**

# Crashlytics
-keepattributes *Annotation*
-keep public class * extends java.lang.Exception

# ============================================
# Google Play Services / AdMob
# ============================================
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public class com.google.ads.** {
    public *;
}

# ============================================
# Coil
# ============================================
-dontwarn coil.**
-keep class coil.** { *; }

# ============================================
# Timber
# ============================================
-dontwarn org.jetbrains.annotations.**
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ============================================
# DataStore
# ============================================
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}

# ============================================
# Navigation Compose
# ============================================
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# ============================================
# Compose
# ============================================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ============================================
# Coroutines
# ============================================
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================
# Keep all domain models
# ============================================
-keep class com.phoenix.companionforcodblackops7.feature.**.domain.model.** { *; }
-keep class com.phoenix.companionforcodblackops7.core.domain.model.** { *; }

# ============================================
# Keep all DTOs (Data Transfer Objects)
# ============================================
-keep class com.phoenix.companionforcodblackops7.feature.**.data.remote.dto.** { *; }
-keep class com.phoenix.companionforcodblackops7.feature.**.data.remote.** { *; }
-keep class com.phoenix.companionforcodblackops7.core.data.remote.dto.** { *; }

# ============================================
# Keep enums
# ============================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================
# Kotlin specific
# ============================================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ============================================
# Remove logging in release (optional optimization)
# ============================================
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
