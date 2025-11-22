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

# Keep serializable classes
-keep,includedescriptorclasses class com.phoenix.companionforcodblackops7.**$$serializer { *; }
-keepclassmembers class com.phoenix.companionforcodblackops7.** {
    *** Companion;
}
-keepclasseswithmembers class com.phoenix.companionforcodblackops7.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# Retrofit & OkHttp
# ============================================
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# ============================================
# Realm
# ============================================
-keep class io.realm.** { *; }
-dontwarn io.realm.**

# ============================================
# Firebase
# ============================================
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ============================================
# Google Play Services / AdMob
# ============================================
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep public class com.google.android.gms.ads.** {
    public *;
}

# ============================================
# Coil
# ============================================
-dontwarn coil.**

# ============================================
# Timber
# ============================================
-dontwarn org.jetbrains.annotations.**

# ============================================
# Keep data/domain models
# ============================================
-keep class com.phoenix.companionforcodblackops7.feature.**.domain.model.** { *; }
-keep class com.phoenix.companionforcodblackops7.feature.**.data.remote.dto.** { *; }
-keep class com.phoenix.companionforcodblackops7.core.data.remote.dto.** { *; }

# ============================================
# Compose
# ============================================
-dontwarn androidx.compose.**

# ============================================
# Coroutines
# ============================================
-dontwarn kotlinx.coroutines.**
