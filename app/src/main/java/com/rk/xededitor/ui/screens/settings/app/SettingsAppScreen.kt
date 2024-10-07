package com.rk.xededitor.ui.screens.settings.app

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.rk.xededitor.Keys
import com.rk.xededitor.R
import com.rk.xededitor.SettingsData
import com.rk.xededitor.ui.components.BottomSheetContent
import kotlinx.coroutines.launch
import org.robok.engine.core.components.compose.preferences.base.PreferenceLayout
import org.robok.engine.core.components.compose.preferences.base.PreferenceTemplate
import org.robok.engine.core.components.compose.preferences.category.PreferenceCategory

@Composable
fun SettingsAppScreen() {
    PreferenceLayout(
        label = stringResource(id = R.string.app),
        backArrowVisible = true,
    ) {
        var isOled by remember { mutableStateOf(SettingsData.isOled()) }
        var isMonet by remember { mutableStateOf(SettingsData.isMonet()) }
        val showDayNightBottomSheet = remember { mutableStateOf(false) }
        val context = LocalContext.current
        
        PreferenceCategory(
            label = stringResource(id = R.string.oled),
            description = stringResource(id = R.string.oled_desc),
            iconResource = R.drawable.dark_mode,
            onNavigate = {
                 showDayNightBottomSheet.value = true
            }
        )
        
        PreferenceCategory(
            label = stringResource(id = R.string.oled),
            description = stringResource(id = R.string.oled_desc),
            iconResource = R.drawable.dark_mode,
            onNavigate = {
                 isOled = !isOled
                 SettingsData.setBoolean(Keys.OLED, isOled)
            },
            endWidget = {
                 Switch(
                    modifier = Modifier
                        .padding(12.dp)
                        .height(24.dp),
                    checked = isOled,
                    onCheckedChange = null
                 )
            }
        )
        PreferenceCategory(
            label = stringResource(id = R.string.monet),
            description = stringResource(id = R.string.monet_desc),
            iconResource = R.drawable.palette,
            enabled = !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S).not(),
            onNavigate = {
                 isMonet = !isMonet
                 SettingsData.setBoolean(Keys.MONET, isMonet)
            },
            endWidget = {
                 Switch(
                    modifier = Modifier
                        .padding(12.dp)
                        .height(24.dp),
                    checked = isMonet,
                    enabled = !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S).not(),
                    onCheckedChange = null
                 )
            }
        )
        if (showDayNightBottomSheet.value) DayNightDialog(showBottomSheet = showDayNightBottomSheet, context = context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayNightDialog(
    showBottomSheet: MutableState<Boolean>,
    context: Context
) {
    val btnSheetState = rememberModalBottomSheetState()
    val btnSheetScope = rememberCoroutineScope()

    var selectedMode by remember {
        mutableIntStateOf(
            SettingsData.getString(
                Keys.DEFAULT_NIGHT_MODE,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
            ).toInt()
        )
    }

    val modes = listOf(
        AppCompatDelegate.MODE_NIGHT_NO,
        AppCompatDelegate.MODE_NIGHT_YES,
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    )

    val modeLabels = listOf(
        context.getString(R.string.light_mode),
        context.getString(R.string.dark_mode),
        context.getString(R.string.auto_mode)
    )

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false },
            sheetState = btnSheetState
        ) {
            BottomSheetContent(
                title = { Text(text = stringResource(id = R.string.select_theme_mode)) },
                buttons = {
                    OutlinedButton(
                        onClick = {
                            btnSheetScope.launch {
                                btnSheetState.hide()
                                showBottomSheet.value = false
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                }
            ) {
                LazyColumn {
                    itemsIndexed(modes) { index, mode ->
                        PreferenceTemplate(
                            title = { Text(text = modeLabels[index]) },
                            modifier = Modifier.clickable {
                                selectedMode = mode
                                SettingsData.setString(Keys.DEFAULT_NIGHT_MODE, selectedMode.toString())
                                AppCompatDelegate.setDefaultNightMode(selectedMode)
                                btnSheetScope.launch {
                                    btnSheetState.hide()
                                    showBottomSheet.value = false
                                }
                            },
                            startWidget = {
                                RadioButton(
                                    selected = selectedMode == mode,
                                    onClick = null
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
