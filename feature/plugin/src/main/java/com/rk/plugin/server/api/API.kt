package com.rk.plugin.server.api

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.plugin.R
import com.rk.plugin.server.PluginError
import com.rk.plugin.server.api.PluginLifeCycle.ActivityEvent
import com.rk.plugin.server.api.PluginLifeCycle.LifeCycleType
import dalvik.system.DexClassLoader
import java.lang.ref.WeakReference

// This class will be available to every plugin
@Keep
object API {
    init {
        onActivityResume(
            "apiGetCurrentActivity",
            object : ActivityEvent {
                override @Keep fun onEvent(id: String, activity: Activity) {
                    ActivityContext = WeakReference(activity)
                }
            },
        )
    }

    var application: Application? = null
    private var ActivityContext = WeakReference<Activity?>(null)
    val handler = Handler(Looper.getMainLooper())

    // not for plugin use
    @Keep
    fun getInstance(): Any? {
        return try {
            val instanceField =
                this::class.java.getDeclaredField("INSTANCE").apply { isAccessible = true }
            instanceField.get(null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Keep
    fun getActivityContext(): Activity? {
        var activity = ActivityContext.get()
        if (activity == null) {
            activity = getMainActivity()
        }
        return activity
    }

    @Keep
    fun setActivityContext(activity: Activity?) {
        ActivityContext = WeakReference(activity)
    }

    @Keep
    fun getMainActivity(): Activity? {
        val companionField =
            Class.forName("com.rk.xededitor.BaseActivity").getDeclaredField("Companion").apply {
                isAccessible = true
            }
        val companionObject = companionField.get(null)

        val getActivityMethod =
            companionObject::class.java.getDeclaredMethod("getActivity", Class::class.java)

        return getActivityMethod.invoke(
            companionObject,
            Class.forName("com.rk.xededitor.MainActivity.MainActivity"),
        ) as Activity?
    }

    @Keep
    fun toast(message: String) {
        runOnUiThread { Toast.makeText(application, message, Toast.LENGTH_SHORT).show() }
    }

    @Keep
    fun runOnUiThread(runnable: Runnable?) {
        runnable?.let { handler.post(it) }
    }

    private val dexMap = HashMap<String, DexClassLoader>()

    @Keep
    fun loadDex(id: String, dexPath: String): DexClassLoader? {
        try {
            if (dexMap.containsKey(id)) {
                return dexMap[id]!!
            }
            return DexClassLoader(
                    dexPath,
                    application!!.codeCacheDir.absolutePath,
                    null,
                    application!!.classLoader,
                )
                .also { dexMap[id] = it }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @Keep
    fun unloadDex(id: String) {
        dexMap.remove(id)
    }

    @Keep
    fun popup(title: String, message: String): AlertDialog? {
        var popup: AlertDialog? = null
        runOnUiThread {
            getActivityContext()?.let {
                popup =
                    MaterialAlertDialogBuilder(it)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
            }
        }
        return popup
    }

    interface InputInterface {
        @Keep fun onInputOK(input: String)
    }

    @Keep
    fun input(title: String, message: String, inputInterface: InputInterface) {
        runOnUiThread {
            val popupView: View =
                LayoutInflater.from(getActivityContext()).inflate(R.layout.popup_new, null)
            val editText = popupView.findViewById<EditText>(R.id.name)
            editText.setHint("Input")

            MaterialAlertDialogBuilder(getActivityContext()!!)
                .setTitle(title)
                .setMessage(message)
                .setView(popupView)
                .setPositiveButton("OK") { _, _ ->
                    val text = editText.text.toString()
                    Thread { inputInterface.onInputOK(text) }.start()
                }
                .show()
        }
    }

    @Keep
    fun error(error: String) {
        PluginError.showError(Exception(error))
    }

    @Keep
    fun onActivityCreate(id: String, activityEvent: ActivityEvent) {
        PluginLifeCycle.registerLifeCycle(id, LifeCycleType.CREATE, activityEvent)
    }

    @Keep
    fun onActivityDestroy(id: String, activityEvent: ActivityEvent) {
        PluginLifeCycle.registerLifeCycle(id, LifeCycleType.DESTROY, activityEvent)
    }

    @Keep
    fun onActivityPause(id: String, activityEvent: ActivityEvent) {
        PluginLifeCycle.registerLifeCycle(id, LifeCycleType.PAUSED, activityEvent)
    }

    @Keep
    fun onActivityResume(id: String, activityEvent: ActivityEvent) {
        PluginLifeCycle.registerLifeCycle(id, LifeCycleType.RESUMED, activityEvent)
    }

    @Keep
    fun unregisterEvent(id: String) {
        PluginLifeCycle.unregisterActivityEvent(id)
    }
}
