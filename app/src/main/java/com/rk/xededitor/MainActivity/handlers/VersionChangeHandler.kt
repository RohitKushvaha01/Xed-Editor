package com.rk.xededitor.MainActivity.handlers

import android.os.Build
import com.rk.xededitor.App
import com.rk.xededitor.Keys
import com.rk.xededitor.R
import com.rk.xededitor.SettingsData
import com.rk.xededitor.rkUtils
import com.rk.xededitor.rkUtils.getString

//blocking code
object VersionChangeHandler {
    fun handle(app: App) {
        val previousVersionCode = SettingsData.getString(Keys.VERSION_CODE, "")

        val pkgInfo = app.packageManager.getPackageInfo(app.packageName, 0)

        val currentVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION") pkgInfo.versionCode.toLong()
        }

        if (previousVersionCode.isEmpty()) {
            //user maybe updating from 2.6.0
            //clear data
            app.filesDir.parentFile?.deleteRecursively()
            SettingsData.setString(Keys.VERSION_CODE,currentVersionCode.toString())
            rkUtils.toast(getString(R.string.data_clear))
            return
        }else if (previousVersionCode.toLong() != currentVersionCode){
            //user updated the app
            SettingsData.setString(Keys.VERSION_CODE,currentVersionCode.toString())
        }
    }
}