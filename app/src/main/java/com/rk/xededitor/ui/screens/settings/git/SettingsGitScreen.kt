package com.rk.xededitor.ui.screens.settings.git

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rk.settings.PreferencesData
import com.rk.settings.PreferencesKeys
import com.rk.xededitor.R
import com.rk.xededitor.rkUtils
import com.rk.xededitor.rkUtils.getString
import com.rk.xededitor.ui.components.InputDialog
import java.io.File
import org.robok.engine.core.components.compose.preferences.base.PreferenceLayout
import org.robok.engine.core.components.compose.preferences.category.PreferenceCategory

@Composable
fun SettingsGitScreen() {
    PreferenceLayout(label = stringResource(id = R.string.git), backArrowVisible = true) {
        val context = LocalContext.current
        var isDialogVisible by remember { mutableStateOf(false) }
        var dialogType by remember { mutableStateOf<DialogType?>(null) }
        var inputValue by remember { mutableStateOf("") }

        PreferenceCategory(
            label = stringResource(id = R.string.cred),
            description = stringResource(id = R.string.gitcred),
            iconResource = R.drawable.key,
            onNavigate = {
                inputValue = PreferencesData.getString(PreferencesKeys.GIT_CRED, "")
                dialogType = DialogType.CREDENTIALS
                isDialogVisible = true
            },
        )

        PreferenceCategory(
            label = stringResource(id = R.string.userdata),
            description = stringResource(id = R.string.userdatagit),
            iconResource = R.drawable.person,
            onNavigate = {
                inputValue = PreferencesData.getString(PreferencesKeys.GIT_USER_DATA, "")
                dialogType = DialogType.USER_DATA
                isDialogVisible = true
            },
        )

        PreferenceCategory(
            label = stringResource(id = R.string.repo_dir),
            description = stringResource(id = R.string.clone_dir),
            iconResource = R.drawable.outline_folder_24,
            onNavigate = {
                inputValue =
                    PreferencesData.getString(PreferencesKeys.GIT_REPO_DIR, "/storage/emulated/0")
                dialogType = DialogType.REPO_DIR
                isDialogVisible = true
            },
        )

        if (isDialogVisible && dialogType != null) {
            InputDialog(
                title =
                    when (dialogType) {
                        DialogType.CREDENTIALS -> stringResource(id = R.string.cred)
                        DialogType.USER_DATA -> stringResource(id = R.string.userdata)
                        DialogType.REPO_DIR -> stringResource(id = R.string.repo_dir)
                        else -> ""
                    },
                inputLabel =
                    when (dialogType) {
                        DialogType.CREDENTIALS -> stringResource(id = R.string.gitKeyExample)
                        DialogType.USER_DATA -> stringResource(id = R.string.gituserexample)
                        DialogType.REPO_DIR -> "/storage/emulated/0"
                        else -> ""
                    },
                inputValue = inputValue,
                onInputValueChange = { inputValue = it },
                onConfirm = {
                    when (dialogType) {
                        DialogType.CREDENTIALS ->
                            PreferencesData.setString(PreferencesKeys.GIT_CRED, inputValue)
                        DialogType.USER_DATA ->
                            PreferencesData.setString(PreferencesKeys.GIT_USER_DATA, inputValue)
                        DialogType.REPO_DIR -> {
                            if (File(inputValue).exists()) {
                                PreferencesData.setString(PreferencesKeys.GIT_REPO_DIR, inputValue)
                            } else {
                                rkUtils.toast(getString(R.string.dir_exist_not))
                            }
                        }
                        else -> {}
                    }
                },
                onDismiss = { isDialogVisible = false },
            )
        }
    }
}

private enum class DialogType {
    CREDENTIALS,
    USER_DATA,
    REPO_DIR,
}
