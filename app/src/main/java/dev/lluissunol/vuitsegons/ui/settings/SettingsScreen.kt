package dev.lluissunol.vuitsegons.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lluissunol.vuitsegons.PossessionCounterApplication
import dev.lluissunol.vuitsegons.core.domain.model.*
import dev.lluissunol.vuitsegons.ui.theme.Theme
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels {
        (application as PossessionCounterApplication).container.viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings: dev.lluissunol.vuitsegons.core.domain.model.Settings by viewModel.settings.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    MaterialTheme(colorScheme = SettingsColorScheme) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("Configuració", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).onBackPressedDispatcher.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Enrere"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // ===== SECCIÓN: TIEMPO =====
            SettingsSection(title = "⏱️ Temps") {
                SettingRow(
                    label = "Durada base",
                    value = "${settings.baseDurationSeconds}s"
                ) {
                    Slider(
                        value = settings.baseDurationSeconds.toFloat(),
                        onValueChange = { viewModel.updateBaseDuration(it.roundToInt()) },
                        valueRange = 1f..60f,
                        steps = 59
                    )
                }

                SettingRow(
                    label = "Avís als últims",
                    value = "${settings.warningThresholdSeconds}s"
                ) {
                    Slider(
                        value = settings.warningThresholdSeconds.toFloat(),
                        onValueChange = { viewModel.updateWarningThreshold(it.roundToInt()) },
                        valueRange = 1f..(settings.baseDurationSeconds - 1).toFloat().coerceAtLeast(1f),
                        steps = (settings.baseDurationSeconds - 2).coerceAtLeast(0)
                    )
                }
            }

            // ===== SECCIÓN: AUDIO =====
            SettingsSection(title = "🔊 Audio") {
                SettingRow(
                    label = "Volum general",
                    value = "${(settings.masterVolume * 100).roundToInt()}%"
                ) {
                    Slider(
                        value = settings.masterVolume,
                        onValueChange = { viewModel.updateMasterVolume(it) },
                        valueRange = 0f..1f
                    )
                }

                SettingToggle(
                    label = "Beep en reiniciar",
                    checked = settings.playTickOnReset,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.playTickOnReset },
                        { s, v -> s.copy(playTickOnReset = v) }
                    ) }
                )

                SettingToggle(
                    label = "Sons d'avís",
                    checked = settings.playWarningBeeps,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.playWarningBeeps },
                        { s, v -> s.copy(playWarningBeeps = v) }
                    ) }
                )

                SettingToggle(
                    label = "Xiulet final",
                    checked = settings.playFinalWhistle,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.playFinalWhistle },
                        { s, v -> s.copy(playFinalWhistle = v) }
                    ) }
                )

                SettingToggle(
                    label = "Ignorar mode silenci",
                    checked = settings.overrideDoNotDisturb,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.overrideDoNotDisturb },
                        { s, v -> s.copy(overrideDoNotDisturb = v) }
                    ) }
                )
            }

            // ===== SECCIÓN: HÁPTICOS =====
            SettingsSection(title = "📳 Hàptics") {
                SettingToggle(
                    label = "Vibrar en reiniciar",
                    checked = settings.vibrateOnReset,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.vibrateOnReset },
                        { s, v -> s.copy(vibrateOnReset = v) }
                    ) }
                )

                SettingToggle(
                    label = "Vibrar en l'avís",
                    checked = settings.vibrateOnWarning,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.vibrateOnWarning },
                        { s, v -> s.copy(vibrateOnWarning = v) }
                    ) }
                )

                SettingToggle(
                    label = "Vibrar fort en acabar",
                    checked = settings.vibrateOnExpire,
                    onCheckedChange = { viewModel.updateBooleanSetting(
                        { it.vibrateOnExpire },
                        { s, v -> s.copy(vibrateOnExpire = v) }
                    ) }
                )
            }

            // ===== SECCIÓN: VISUAL =====
            SettingsSection(title = "🎨 Visual") {
                SettingColorPicker(
                    label = "Color actiu",
                    color = settings.activeColor,
                    onColorChange = { viewModel.updateColor(SettingsViewModel.ColorType.ACTIVE, it) }
                )

                SettingColorPicker(
                    label = "Color d'avís",
                    color = settings.warningColor,
                    onColorChange = { viewModel.updateColor(SettingsViewModel.ColorType.WARNING, it) }
                )

                SettingColorPicker(
                    label = "Color d'acabat",
                    color = settings.expiredColor,
                    onColorChange = { viewModel.updateColor(SettingsViewModel.ColorType.EXPIRED, it) }
                )

                SettingColorPicker(
                    label = "Color de fons",
                    color = settings.backgroundColor,
                    onColorChange = { viewModel.updateColor(SettingsViewModel.ColorType.BACKGROUND, it) }
                )

                SettingToggle(
                    label = "Mantenir la pantalla encesa",
                    checked = settings.keepScreenOn,
                    onCheckedChange = { viewModel.updateKeepScreenOn(it) }
                )

                SettingToggle(
                    label = "Girar el comptador 180° (boca avall)",
                    checked = settings.forceReversePortrait,
                    onCheckedChange = { viewModel.updateForceReversePortrait(it) }
                )
            }

            // ===== SECCIÓN: COMPORTAMIENTO =====
            SettingsSection(title = "⚙️ Comportament") {
                SettingToggle(
                    label = "Iniciar automàticament en obrir",
                    checked = settings.autoStart,
                    onCheckedChange = { viewModel.updateAutoStart(it) }
                )
            }

            // ===== ACCIONES =====
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Restaurar valors per defecte", fontWeight = FontWeight.Medium)
            }

            // ===== ACERCA DE =====
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
            AboutCard(modifier = Modifier.padding(horizontal = 16.dp))
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
        }
    }
    }
}

private val SettingsColorScheme = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFF6EE08A),
    onPrimary = Color(0xFF003914),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFFB8B8B8),
    surfaceContainer = Color(0xFF1C1C1C),
    surfaceContainerLow = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    outlineVariant = Color(0xFF4A4A4A),
    errorContainer = Color(0xFF5C2323),
    onErrorContainer = Color(0xFFFFDAD6)
)

// ===== COMPONENTES DE SETTINGS =====

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
fun SettingRow(
    label: String,
    value: String,
    slider: @Composable () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 16.sp)
            Text(text = value, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
        slider()
    }
}

@Composable
fun SettingToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
    ) {
        Text(text = label, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(16.dp))
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}

@Composable
fun SettingColorPicker(
    label: String,
    color: Int,
    onColorChange: (Int) -> Unit
) {
    val composeColor = Color(color)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
            .clickable { /* TODO: Color picker dialog */ }
    ) {
        Text(text = label, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(16.dp))
        Box(
            modifier = Modifier
                .size(40.dp, 28.dp)
                .background(composeColor, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(end = 16.dp)
        )
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Color",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}