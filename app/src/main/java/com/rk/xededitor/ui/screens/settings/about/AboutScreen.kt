package com.rk.xededitor.ui.screens.settings.about

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.pm.PackageInfoCompat
import com.rk.resources.strings
import com.rk.xededitor.BuildConfig
import com.rk.xededitor.update.UpdateManager
import org.robok.engine.core.components.compose.preferences.base.PreferenceGroup
import org.robok.engine.core.components.compose.preferences.base.PreferenceLayout
import org.robok.engine.core.components.compose.preferences.base.PreferenceTemplate

@Composable
fun AboutScreen() {

    val packageInfo =
        LocalContext.current.packageManager.getPackageInfo(LocalContext.current.packageName, 0)
    val versionName = packageInfo.versionName
    val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
    val context = LocalContext.current

    PreferenceLayout(label = stringResource(id = strings.about), backArrowVisible = true) {
        PreferenceGroup(heading = stringResource(strings.app_name)) {
            PreferenceTemplate(
                title = { Text(text = stringResource(id = strings.version), style = MaterialTheme.typography.titleMedium) },
                description = {
                    Text(text = versionName, style = MaterialTheme.typography.titleSmall)
                },
            )

            PreferenceTemplate(
                title = {
                    Text(text = stringResource(id = strings.version_code), style = MaterialTheme.typography.titleMedium)
                },
                description = {
                    Text(text = versionCode.toString(), style = MaterialTheme.typography.titleSmall)
                },
            )
            
            
            PreferenceTemplate(
                title = { Text(text = stringResource(id = strings.git_commit), style = MaterialTheme.typography.titleMedium) },
                description = {
                    Text(
                        text = BuildConfig.GIT_SHORT_COMMIT_HASH,
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
            )

            PreferenceTemplate(
                title = { Text(text = stringResource(id = strings.github), style = MaterialTheme.typography.titleMedium) },
                description = {
                    Text(
                        text = stringResource(id = strings.github_desc),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                endWidget = {
                    Button(
                        onClick = {
                            val url = "https://github.com/Xed-Editor/Xed-Editor"
                            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
                            context.startActivity(intent)
                        }
                    ) {
                        Text(stringResource(id = strings.github))
                    }
                },
            )
            
            

            PreferenceTemplate(
                title = {
                    Text(text = stringResource(id = strings.telegram), style = MaterialTheme.typography.titleMedium)
                },
                description = {
                    Text(
                        text = stringResource(id = strings.telegram_desc),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                endWidget = {
                    Button(
                        onClick = {
                            val url = "https://t.me/Xed_Editor"
                            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }
                            context.startActivity(intent)
                        }
                    ) {
                        Text(stringResource(id = strings.join))
                    }
                },
            )
        }
    }
}
