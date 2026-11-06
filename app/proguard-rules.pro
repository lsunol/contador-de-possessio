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
-keep class dev.lluissunol.vuitsegons.core.domain.model.Settings { *; }
-keep class dev.lluissunol.vuitsegons.core.domain.model.TimerState { *; }
-keep class dev.lluissunol.vuitsegons.core.domain.model.SoundType { *; }
-keep class dev.lluissunol.vuitsegons.core.domain.model.VibrationPattern { *; }
-keep class dev.lluissunol.vuitsegons.core.domain.model.DigitSize { *; }
-keep class dev.lluissunol.vuitsegons.core.domain.model.ResetGesture { *; }
-keep class dev.lluissunol.vuitsegons.core.domain.model.PauseGesture { *; }

# Keep Hilt Entry Points
-keep class dev.lluissunol.vuitsegons.PossessionCounterApplication { *; }
-keep class dev.lluissunol.vuitsegons.ui.main.MainActivity { *; }
-keep class dev.lluissunol.vuitsegons.ui.settings.SettingsActivity { *; }

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }

# Optimize
-optimizationpasses 5
-allowaccessmodification
-dontpreverify