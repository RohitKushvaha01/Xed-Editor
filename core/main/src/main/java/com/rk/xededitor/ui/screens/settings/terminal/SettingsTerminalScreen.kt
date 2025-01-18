package com.rk.xededitor.ui.screens.settings.terminal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.rk.karbon_exec.isTermuxCompatible
import com.rk.karbon_exec.isTermuxInstalled
import com.rk.karbon_exec.testExecPermission
import com.rk.libcommons.DefaultScope
import com.rk.resources.strings
import com.rk.settings.PreferencesData
import com.rk.settings.PreferencesKeys
import com.rk.xededitor.ui.components.BottomSheetContent
import com.rk.xededitor.ui.components.SettingsToggle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.robok.engine.core.components.compose.preferences.base.PreferenceGroup
import org.robok.engine.core.components.compose.preferences.base.PreferenceLayout
import org.robok.engine.core.components.compose.preferences.base.PreferenceTemplate


@Composable
fun SettingsTerminalScreen(navController: NavController) {
    PreferenceLayout(label = stringResource(id = strings.terminal), backArrowVisible = true) {
        val context = LocalContext.current
        val isInstalled = isTermuxInstalled() && isTermuxCompatible()
        val showDayBottomSheet = remember { mutableStateOf(false) }

        val execAllowed = remember { mutableStateOf(false) }
        val errorMessage = remember { mutableStateOf("") }

        val result = testExecPermission()
        execAllowed.value = result.first
        errorMessage.value = result.second?.message.toString()


        PreferenceGroup {
            SettingsToggle(label = stringResource(strings.termux_exec),
                description = if (execAllowed.value.not()) {
                    errorMessage.value
                } else {
                    "Termux-Exec"
                },
                default = execAllowed.value,
                isSwitchLocked = true,
                isEnabled = isInstalled,
                sideEffect = {
                    if (execAllowed.value.not()) {
                        val intent =
                            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        context.startActivity(intent)
                    }
                })


            SettingsToggle(label = stringResource(strings.termux_exec_guide),
                description = stringResource(strings.termux_exec_guide_desc),
                showSwitch = false,
                sideEffect = {
                    val url = if (isTermuxInstalled()) {
                        if (isTermuxCompatible()) {
                            "https:github.com/Xed-Editor/Xed-Editor/blob/main/docs/termux/SETUP_TERMUX.md"
                        } else {
                            "https:github.com/Xed-Editor/Xed-Editor/blob/main/docs/termux/GOOGLE_PLAY_TERMUX.md"
                        }
                    } else {
                        "https:github.com/Xed-Editor/Xed-Editor/blob/main/docs/termux/INSTALL_TERMUX.md"
                    }

                    DefaultScope.launch {
                        delay(100)
                        withContext(Dispatchers.Main) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                })

            SettingsToggle(
                label = "Terminal Runtime",
                description = "Select Terminal Runtime",
                showSwitch = false,
                sideEffect = {
                    showDayBottomSheet.value = !showDayBottomSheet.value
                }
            )

            if (showDayBottomSheet.value){
                TerminalRuntime(modifier = Modifier,showDayBottomSheet, LocalContext.current)
            }
        }
    }
}


sealed class RuntimeType(val type: String) {
    data object ALPINE : RuntimeType("Alpine")
    data object TERMUX : RuntimeType("Termux")
    data object ANDROID : RuntimeType("Android")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalRuntime(
    modifier: Modifier = Modifier, showBottomSheet: MutableState<Boolean>, context: Context
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    var selectedType by remember {
        mutableStateOf(
            PreferencesData.getString(PreferencesKeys.TERMINAL_RUNTIME, RuntimeType.ALPINE.type)
        )
    }

    val types = listOf(
        RuntimeType.ALPINE.type, RuntimeType.TERMUX.type, RuntimeType.ANDROID.type
    )

    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet.value = false }, sheetState = bottomSheetState
        ) {
            BottomSheetContent(title = { Text(text = "Terminal Runtime") },
                buttons = {
                    OutlinedButton(onClick = {
                        coroutineScope.launch {
                            bottomSheetState.hide(); showBottomSheet.value = false
                        }
                    }) {
                        Text(text = stringResource(id = strings.cancel))
                    }
                }) {
                LazyColumn {
                    itemsIndexed(types) { index, mode ->
                        PreferenceTemplate(title = { Text(text = mode) },
                            modifier = Modifier.clickable {
                                selectedType = mode
                                PreferencesData.setString(
                                    PreferencesKeys.TERMINAL_RUNTIME, selectedType
                                )
                                coroutineScope.launch {
                                    bottomSheetState.hide(); showBottomSheet.value = false;
                                }
                            },
                            startWidget = {
                                RadioButton(
                                    selected = selectedType == mode, onClick = null
                                )
                            })
                    }
                }
            }
        }
    }
}