package com.rk.xededitor.settings

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rk.libcommons.After
import com.rk.libcommons.LoadingPopup
import com.rk.xededitor.BaseActivity
import com.rk.xededitor.R
import com.rk.xededitor.databinding.ActivitySettingsMainBinding
import com.rk.xededitor.pluginClient.ManagePlugins
import com.rk.xededitor.rkUtils
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.onCheckedChange
import de.Maxr1998.modernpreferences.helpers.onClickView
import de.Maxr1998.modernpreferences.helpers.pref
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.switch

class SettingsPlugins : BaseActivity() {
  private lateinit var recyclerView: RecyclerView
  private lateinit var binding: ActivitySettingsMainBinding
  private lateinit var padapter: PreferencesAdapter
  private lateinit var playoutManager: LinearLayoutManager

  fun get_recycler_view(): RecyclerView {
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
    get_recycler_view().apply {
      layoutManager = playoutManager
      adapter = padapter
    }
    
    edgeToEdge(binding.root)
    setContentView(binding.root)
    
    binding.toolbar.title = rkUtils.getString(R.string.plugin)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  fun getScreen(): PreferenceScreen {
    return screen(this) {
      switch(Keys.ENABLE_PLUGINS) {
        titleRes = R.string.enable_plugins
        summaryRes = R.string.enable_plugins
        iconRes = R.drawable.extension
        onCheckedChange { isChecked ->
          LoadingPopup(this@SettingsPlugins, 200)
          After(230) {
            runOnUiThread {
              this@SettingsPlugins.recreate()
            }
          }
          return@onCheckedChange true
        }
      }
      if (SettingsData.getBoolean(Keys.ENABLE_PLUGINS, false)) {
        pref(Keys.MANAGE_PLUGINS) {
          titleRes = R.string.manage_plugins
          summaryRes = R.string.manage_plugins
          iconRes = R.drawable.extension
          onClickView {
            startActivity(Intent(this@SettingsPlugins, ManagePlugins::class.java))
          }
        }
      }
    }
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
