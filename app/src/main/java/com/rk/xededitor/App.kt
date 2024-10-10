package com.rk.xededitor

import android.app.Application
import android.content.Context
import com.rk.libcommons.After
import com.rk.plugin.server.Loader
import com.rk.settings.PreferencesData
import com.rk.xededitor.CrashHandler.CrashHandler
import com.rk.xededitor.MainActivity.handlers.VersionChangeHandler
import com.rk.xededitor.ui.screens.settings.terminal.updateProotArgs
import java.io.File
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : Application() {

    companion object {
        lateinit var app: Application

        fun Context.getTempDir(): File {
            val tmp = File(filesDir.parentFile, "tmp")
            if (!tmp.exists()) {
                tmp.mkdir()
            }
            return tmp
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        app = this
        super.onCreate()

        // create temp folder
        GlobalScope.launch(Dispatchers.IO) {
            val tmp = File(filesDir.parentFile, "tmp")
            if (!tmp.exists()) {
                tmp.mkdir()
            } else {
                tmp.deleteRecursively()
                tmp.mkdir()
            }
        }

        // create crash handler
        CrashHandler.INSTANCE.init(this).let {
            // initialize shared preferences
            PreferencesData.initPref(this).let {
                // handle version change
                // blocking code
                VersionChangeHandler.handle(this)
            }
        }

        // start plugin loader
        After(2000) {
            val pluginLoader = Loader(this)
            pluginLoader.start()
        }

        SetupEditor.init(this)
        updateProotArgs(this)
    }
}
