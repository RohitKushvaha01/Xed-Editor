package com.rk.xededitor.Settings

import android.graphics.Color
import android.os.Bundle
import android.os.Build
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup

import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.dialog.MaterialAlertDialogBuilder

import com.rk.libcommons.After
import com.rk.xededitor.BaseActivity
import com.rk.libcommons.LoadingPopup
import com.rk.xededitor.R
import com.rk.xededitor.databinding.ActivitySettingsMainBinding
import com.rk.xededitor.ui.theme.ThemeManager

import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.onCheckedChange
import de.Maxr1998.modernpreferences.helpers.onClickView
import de.Maxr1998.modernpreferences.helpers.pref
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.switch

class SettingsApp : BaseActivity() {
  private lateinit var recyclerView: RecyclerView
  private lateinit var binding: ActivitySettingsMainBinding
  private lateinit var padapter: PreferencesAdapter
  private lateinit var playoutManager: LinearLayoutManager

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

    setContentView(binding.root)
    binding.toggleButton.visibility = View.VISIBLE
    binding.toolbar.title = getString(R.string.app)
    setSupportActionBar(binding.toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    if (SettingsData.isDarkMode(this) && SettingsData.isOled()) {
      binding.root.setBackgroundColor(Color.BLACK)
      binding.toolbar.setBackgroundColor(Color.BLACK)
      binding.appbar.setBackgroundColor(Color.BLACK)
      window.navigationBarColor = Color.BLACK
      val window = window
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      window.statusBarColor = Color.BLACK
      window.navigationBarColor = Color.BLACK
    } else if (SettingsData.isDarkMode(this)) {
      val window = window
      window.navigationBarColor = Color.parseColor("#141118")
    }

    fun getCheckedBtnIdFromSettings(): Int {
      val settingDefaultNightMode = SettingsData.getString(Keys.DEFAULT_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
      ).toInt()

      return when (settingDefaultNightMode) {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> binding.auto.id
        AppCompatDelegate.MODE_NIGHT_NO -> binding.light.id
        AppCompatDelegate.MODE_NIGHT_YES -> binding.dark.id
        else -> throw RuntimeException("Illegal default night mode state")
      }
    }

    binding.toggleButton.check(getCheckedBtnIdFromSettings())

    val listener = View.OnClickListener {
      when (binding.toggleButton.checkedButtonId) {
        binding.auto.id -> {
          LoadingPopup(this@SettingsApp, 200)
            After(300) {
                SettingsData.setString(
                    Keys.DEFAULT_NIGHT_MODE,
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString()
                )

                runOnUiThread {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }

        binding.light.id -> {
          LoadingPopup(this@SettingsApp, 200)
            After(300) {
                SettingsData.setString(
                    Keys.DEFAULT_NIGHT_MODE,
                    AppCompatDelegate.MODE_NIGHT_NO.toString()
                )

                runOnUiThread {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        binding.dark.id -> {
          LoadingPopup(this@SettingsApp, 200)
            After(300) {
                SettingsData.setString(
                    Keys.DEFAULT_NIGHT_MODE,
                    AppCompatDelegate.MODE_NIGHT_YES.toString()
                )

                runOnUiThread {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
      }
    }

    binding.light.setOnClickListener(listener)
    binding.dark.setOnClickListener(listener)
    binding.auto.setOnClickListener(listener)
  }

  private fun getScreen(): PreferenceScreen {
    return screen(this) {
      switch(Keys.OLED) {
        titleRes = R.string.oled
        summary = getString(R.string.oled_desc)
        iconRes = R.drawable.dark_mode
        defaultValue = false
        onCheckedChange {
          LoadingPopup(this@SettingsApp, 180)
          //getActivity(MainActivity::class.java)?.recreate()
          return@onCheckedChange true
        }
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
         switch(Keys.MONEY) {
              titleRes = R.string.monet
              summary = getString(R.string.monet_desc)
              iconRes = R.drawable.palette
              defaultValue = false
              onCheckedChange {
                   LoadingPopup(this@SettingsApp, 180)
                   getActivity(SettingsApp::class.java)?.recreate()
                   return@onCheckedChange true
              }
         }
      }
      pref(Keys.THEMES) {
        title = getString(R.string.themes)
        summary = getString(R.string.change_theme)
        iconRes = R.drawable.palette
        onClickView {
          val themes = ThemeManager.getThemes(this@SettingsApp)

          val linearLayout = LinearLayout(this@SettingsApp).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20.dp, 8.dp, 0, 0)
          }

          val radioGroup = RadioGroup(this@SettingsApp).apply {
            orientation = RadioGroup.VERTICAL
          }

          themes.forEach { theme ->
            val radioButton = RadioButton(this@SettingsApp).apply {
              text = theme.first
            }
            radioGroup.addView(radioButton)
          }

          linearLayout.addView(radioGroup)
          val selectedThemeName = ThemeManager.getSelectedTheme()
          val selectedThemeIndex = themes.indexOfFirst { it.first == selectedThemeName }
          if (selectedThemeIndex != -1) {
            radioGroup.check(radioGroup.getChildAt(selectedThemeIndex).id)
          } else {
            radioGroup.check(radioGroup.getChildAt(0).id)
          }

          var checkID = radioGroup.checkedRadioButtonId

          radioGroup.setOnCheckedChangeListener { _, checkedId ->
            checkID = checkedId
          }

          val dialog =
            MaterialAlertDialogBuilder(this@SettingsApp)
              .setView(linearLayout)
              .setTitle(getString(R.string.themes))
              .setNegativeButton(getString(R.string.cancel), null)
              .setPositiveButton(getString(R.string.apply)) { _, _ ->
                val loading = LoadingPopup(this@SettingsApp, null).show()

                val selectedTheme = themes[radioGroup.indexOfChild(radioGroup.findViewById(checkID))]
                ThemeManager.setSelectedTheme(selectedTheme.first)

                activityMap.values.forEach { activityRef ->
                  activityRef?.get()?.recreate()
                }

                loading.hide()
              }.show()

          dialog.window?.setLayout(
            resources.getDimensionPixelSize(R.dimen.dialog_width), // Set your desired width here
            ViewGroup.LayoutParams.WRAP_CONTENT
          )
        }
      }

    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // Save the padapter state as a parcelable into the Android-managed instance state
    outState.putParcelable("padapter", padapter.getSavedState())
  }

  val Int.dp: Int
    get() = TypedValue.applyDimension(
      TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics
    ).toInt()

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    // Handle action bar item clicks here
    val id = item.itemId
    if (id == android.R.id.home) {
      // Handle the back arrow click here
      onBackPressedDispatcher.onBackPressed()
      return true
    }
    return super.onOptionsItemSelected(item)
  }
}
