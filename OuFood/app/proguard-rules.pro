# --- Android ProGuard & R8 Configuration for Production MVP ---

# Firebase Realtime Database & Auth DTOs
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class com.nttung.oufood.data.remote.dto.** { *; }
-keep class com.nttung.oufood.domain.model.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Timber Logging (Tự động strip log trong release nếu cần)
-assumenosideeffects class timber.log.Timber$Tree {
    public void d(...);
    public void v(...);
}

# Preserve line numbers for Crashlytics
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
