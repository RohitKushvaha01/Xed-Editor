package com.rk.xededitor.Settings

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.WindowManager
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jaredrummler.ktsh.Shell
import com.rk.xededitor.BaseActivity
import com.rk.xededitor.R
import com.rk.xededitor.Settings.SettingsData.getBoolean
import com.rk.xededitor.databinding.ActivitySettingsMainBinding
import com.rk.xededitor.rkUtils
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.onCheckedChange
import de.Maxr1998.modernpreferences.helpers.onClick
import de.Maxr1998.modernpreferences.helpers.onClickView
import de.Maxr1998.modernpreferences.helpers.pref
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.switch
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files

class TerminalSettings : BaseActivity() {
  private lateinit var recyclerView: RecyclerView
  private lateinit var binding: ActivitySettingsMainBinding
  lateinit var padapter: PreferencesAdapter
  lateinit var playoutManager: LinearLayoutManager


  private fun getRecyclerView(): RecyclerView {
    binding = ActivitySettingsMainBinding.inflate(layoutInflater)
    recyclerView = binding.recyclerView
    return recyclerView
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    padapter = PreferencesAdapter(getScreen())

    savedInstanceState?.getParcelable<PreferencesAdapter.SavedState>("padapter")
      ?.let(padapter::loadSavedState)

    playoutManager = LinearLayoutManager(this)
    getRecyclerView().apply {
      layoutManager = playoutManager
      adapter = padapter
      //layoutAnimation = AnimationUtils.loadLayoutAnimation(this@settings2, R.anim.preference_layout_fall_down)
    }

    edgeToEdge(window, binding.root)
    setContentView(binding.root)
    
    binding.toolbar.title = "Terminal"
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }
  
  private fun getScreen(): PreferenceScreen {
    return screen(this) {
      switch(Keys.FAIL_SAFE) {
        title = "Fail Safe"
        summary = "Use android shell instead of alpine"
        iconRes = R.drawable.android
      }
      pref(Keys.START_SHELL_PREF) {
        title = "Launch Shell"
        summary = "Executed when the terminal is launched."
        iconRes = R.drawable.terminal
        onClickView {

          val view = LayoutInflater.from(this@TerminalSettings).inflate(R.layout.popup_new, null)
          val edittext = view.findViewById<EditText>(R.id.name).apply {
            hint = "eg. /bin/sh"
            setText(
              SettingsData.getString(Keys.SHELL, "/bin/sh")
                .removePrefix(File(filesDir.parentFile, "rootfs/").absolutePath)
            )
          }
          MaterialAlertDialogBuilder(this@TerminalSettings).setTitle("Launch Shell").setView(view)
            .setNegativeButton("Cancel", null).setPositiveButton("Apply") { _, _ ->
              val shell = edittext.text.toString()

              if (shell.isEmpty()) {
                return@setPositiveButton
              }

              //verify shell exists
              val absoluteShell = File(filesDir.parentFile, "rootfs/$shell")
              if (absoluteShell.exists().not() && !Files.isSymbolicLink(absoluteShell.toPath())) {
                rkUtils.toast("File does not exist")
                return@setPositiveButton
              }

              SettingsData.setString(Keys.SHELL, absoluteShell.absolutePath)

              Shell.SH.run("echo \"$shell\" > ${filesDir.parentFile!!.absolutePath}/shell")


            }.show()
        }
      }
      pref(Keys.TERMINAL_TEXT_SIZE_PREF) {
        title = "Terminal text size"
        summary = "Set terminal text size"
        iconRes = R.drawable.terminal
        onClick {
          val view = LayoutInflater.from(this@TerminalSettings).inflate(R.layout.popup_new, null)
          val edittext = view.findViewById<EditText>(R.id.name).apply {
            hint = "Terminal Text size"
            setText(SettingsData.getString(Keys.TERMINAL_TEXT_SIZE, "14"))
            inputType =
              InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
          }
          MaterialAlertDialogBuilder(this@TerminalSettings).setTitle("Text Size").setView(view)
            .setNegativeButton("Cancel", null).setPositiveButton("Apply") { _, _ ->
              val text = edittext.text.toString()
              for (c in text) {
                if (!c.isDigit()) {
                  rkUtils.toast("invalid value")
                  return@setPositiveButton
                }
              }
              if (text.toInt() > 32) {
                rkUtils.toast("Value too large")
                return@setPositiveButton
              }

              if (text.toInt() < 8) {
                rkUtils.toast("Value too small")
                return@setPositiveButton
              }
              SettingsData.setString(Keys.TERMINAL_TEXT_SIZE, text)

            }.show()

          return@onClick true
        }
      }

      switch(Keys.LINK2SYMLINK) {
        title = "Simulate hard links"
        summary = "Create a symlink where a hardlink should be created"
        iconRes = R.drawable.terminal
        defaultValue = false
        onCheckedChange { checked ->
          return@onCheckedChange updateProotArgs(this@TerminalSettings)
        }
      }
      switch(Keys.ASHMEM_MEMFD) {
        title = "Ashmem_memfd"
        summary = "Simulate Ashmemfd on Android"
        iconRes = R.drawable.terminal
        defaultValue = true
        onCheckedChange { checked ->
          
          return@onCheckedChange updateProotArgs(this@TerminalSettings)
        }
      }
      switch(Keys.SYSVIPC) {
        title = "Sysvipc"
        summary = "Handle System V IPC syscall"
        iconRes = R.drawable.terminal
        defaultValue = true
        onCheckedChange { checked ->
          
          return@onCheckedChange updateProotArgs(this@TerminalSettings)
        }

      }
      switch(Keys.KILL_ON_EXIT) {
        title = "Kill on exit"
        summary = "Terminate all processes when exiting terminal"
        iconRes = R.drawable.terminal
        defaultValue = true
        onCheckedChange { checked ->
          return@onCheckedChange updateProotArgs(this@TerminalSettings)
        }
      }


    }
  }
  
  companion object{
    @OptIn(DelicateCoroutinesApi::class)
    fun updateProotArgs(context:Context):Boolean{
      GlobalScope.launch(Dispatchers.IO){
        val link2sym = getBoolean(Keys.LINK2SYMLINK,false)
        val ashmemfd = getBoolean(Keys.ASHMEM_MEMFD,true)
        val sysvipc = getBoolean(Keys.SYSVIPC,true)
        val killOnExit = getBoolean(Keys.KILL_ON_EXIT,true)

        val sb = StringBuilder()
        if (link2sym){
          sb.append(" --link2symlink")
        }
        if (ashmemfd){
          sb.append(" --ashmem-memfd")
        }

        if (sysvipc){
          sb.append(" --sysvipc")
        }

        if (killOnExit){
          sb.append(" --kill-on-exit ")
        }


        Shell.SH.apply {
          run("echo $sb > ${File(context.filesDir.parentFile,"proot_args").absolutePath}")
          shutdown()
        }

      }

      return true
    }
  }


  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // Save the padapter state as a parcelable into the Android-managed instance state
    outState.putParcelable("padapter", padapter.getSavedState())
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here
    val id = item.itemId
    if (id == android.R.id.home) {
      // Handle the back arrow click here
      onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }


}