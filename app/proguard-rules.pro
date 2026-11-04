# ProGuard rules for Possession Counter

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModelProvider.Factory { *; }

# Keep Compose related
-keep class androidx.compose.** { *; }
-keep class * extends androidx.compose.runtime.Composable { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep SoundPool
-keep class android.media.SoundPool { *; }

# Keep Settings model (Parcelable)
-keep class com.lluis.possessioncounter.core.domain.model.Settings { *; }
-keep class com.lluis.possessioncounter.core.domain.model.TimerState { *; }
-keep class com.lluis.possessioncounter.core.domain.model.SoundType { *; }
-keep class com.lluis.possessioncounter.core.domain.model.VibrationPattern { *; }
-keep class com.lluis.possessioncounter.core.domain.model.DigitSize { *; }
-keep class com.lluis.possessioncounter.core.domain.model.ResetGesture { *; }
-keep class com.lluis.possessioncounter.core.domain.model.PauseGesture { *; }

# Keep Hilt Entry Points
-keep class com.lluis.possessioncounter.PossessionCounterApplication { *; }
-keep class com.lluis.possessioncounter.ui.main.MainActivity { *; }
-keep class com.lluis.possessioncounter.ui.settings.SettingsActivity { *; }

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }

# Optimize
-optimizationpasses 5
-allowaccessmodification
-dontpreverify