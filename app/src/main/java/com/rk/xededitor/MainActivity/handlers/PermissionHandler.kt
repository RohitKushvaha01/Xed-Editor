package com.rk.xededitor.MainActivity.handlers

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.xededitor.R

object PermissionHandler {
    private const val REQUEST_CODE_STORAGE_PERMISSIONS = 1259
    private const val MANAGE_EXTERNAL_STORAGE = 98421

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        activity: Activity,
    ) {
        // check permission for old devices
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (
                !(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                // permission denied ask again
                verifyStoragePermission(activity)
            }
        }
    }

    fun verifyStoragePermission(activity: Activity) {
        activity.runOnUiThread {
            with(activity) {
                var shouldAsk = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (!Environment.isExternalStorageManager()) {
                        shouldAsk = true
                    }
                } else {
                    if (
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                        ) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        shouldAsk = true
                    }
                }

                if (shouldAsk) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.manage_storage))
                        .setMessage(getString(R.string.manage_storage_reason))
                        .setNegativeButton(getString(R.string.exit)) {
                            dialog: DialogInterface?,
                            which: Int ->
                            finishAffinity()
                        }
                        .setPositiveButton(getString(R.string.ok)) {
                            dialog: DialogInterface?,
                            which: Int ->
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
                                val intent =
                                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                intent.setData(Uri.parse("package:$packageName"))
                                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE)
                            } else {
                                // below 11
                                // Request permissions
                                val perms =
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    )
                                ActivityCompat.requestPermissions(
                                    this,
                                    perms,
                                    REQUEST_CODE_STORAGE_PERMISSIONS,
                                )
                            }
                        }
                        .setCancelable(false)
                        .show()
                }
            }
        }
    }
}
