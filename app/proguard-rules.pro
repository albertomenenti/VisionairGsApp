# Regole ProGuard del progetto.
# Al momento isMinifyEnabled = false, quindi queste regole non sono attive,
# ma sono pronte per quando attiveremo il code shrinking.

# Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Kotlin Metadata
-keep class kotlin.Metadata { *; }
