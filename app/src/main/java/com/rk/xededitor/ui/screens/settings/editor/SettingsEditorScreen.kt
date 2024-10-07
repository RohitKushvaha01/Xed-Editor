package com.rk.xededitor.ui.screens.settings.editor

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.xededitor.BaseActivity.Companion.getActivity
import com.rk.xededitor.MainActivity.MainActivity
import com.rk.xededitor.MainActivity.editor.AutoSaver
import com.rk.xededitor.R
import com.rk.xededitor.rkUtils
import com.rk.xededitor.rkUtils.getString
import com.rk.xededitor.settings.Keys
import com.rk.xededitor.settings.SettingsData
import org.robok.engine.core.components.compose.preferences.base.PreferenceLayout
import org.robok.engine.core.components.compose.preferences.category.PreferenceCategory

@Composable
fun SettingsEditorScreen() {
  PreferenceLayout(
    label = stringResource(id = R.string.editor),
    backArrowVisible = true,
  ) {

    var smoothTabs by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.VIEWPAGER_SMOOTH_SCROLL, true
        )
      )
    }
    var wordwrap by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.WORD_WRAP_ENABLED, false
        )
      )
    }
    var drawerLock by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.KEEP_DRAWER_LOCKED, false
        )
      )
    }
    var diagonalScroll by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.DIAGONAL_SCROLL, false
        )
      )
    }
    var cursorAnimation by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.CURSOR_ANIMATION_ENABLED, true
        )
      )
    }
    var showLineNumber by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.SHOW_LINE_NUMBERS, true
        )
      )
    }
    var pinLineNumber by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.PIN_LINE_NUMBER, false
        )
      )
    }
    var showArrowKeys by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.SHOW_ARROW_KEYS, false
        )
      )
    }
    var autoSave by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.AUTO_SAVE, false
        )
      )
    }
    var editorFont by remember {
      mutableStateOf(
        SettingsData.getBoolean(
          Keys.EDITOR_FONT, false
        )
      )
    }

    val context = LocalContext.current

    PreferenceCategory(label = stringResource(id = R.string.smooth_tabs),
      description = stringResource(id = R.string.smooth_tab_desc),
      iconResource = R.drawable.animation,
      onNavigate = {
        smoothTabs = !smoothTabs
        SettingsData.setBoolean(Keys.VIEWPAGER_SMOOTH_SCROLL, smoothTabs)
        MainActivity.activityRef.get()?.smoothTabs = smoothTabs
      },
      endWidget = {
        Switch(modifier = Modifier
          .padding(12.dp)
          .height(24.dp),
          checked = smoothTabs,
          onCheckedChange = null)
      })

    PreferenceCategory(label = stringResource(id = R.string.ww),
      description = stringResource(id = R.string.ww_desc),
      iconResource = R.drawable.reorder,
      onNavigate = {
        wordwrap = !wordwrap
        SettingsData.setBoolean(Keys.WORD_WRAP_ENABLED, wordwrap)
        MainActivity.activityRef.get()?.adapter?.tabFragments?.forEach { f ->
          f.value.get()?.editor?.isWordwrap = wordwrap
        }
      },
      endWidget = {
        Switch(modifier = Modifier
          .padding(12.dp)
          .height(24.dp),
          checked = wordwrap,
          onCheckedChange = null)
      })



    PreferenceCategory(label = stringResource(id = R.string.keepdl),
      description = stringResource(id = R.string.drawer_lock_desc),
      iconResource = R.drawable.lock,
      onNavigate = {
        drawerLock = !drawerLock
        SettingsData.setBoolean(Keys.KEEP_DRAWER_LOCKED, drawerLock)
      },
      endWidget = {
        Switch(
          modifier = Modifier
            .padding(12.dp)
            .height(24.dp),
          checked = drawerLock,
          onCheckedChange = null
        )
      })


    PreferenceCategory(label = stringResource(id = R.string.diagonal_scroll),
      description = stringResource(id = R.string.diagonal_scroll_desc),
      iconResource = R.drawable.diagonal_scroll,
      onNavigate = {
        diagonalScroll = !diagonalScroll
        SettingsData.setBoolean(Keys.DIAGONAL_SCROLL, diagonalScroll)
        rkUtils.toast(getString(R.string.rr))
      },
      endWidget = {
        Switch(
          modifier = Modifier
            .padding(12.dp)
            .height(24.dp),
          checked = diagonalScroll,
          onCheckedChange = null
        )
      })



    PreferenceCategory(label = stringResource(id = R.string.cursor_anim),
      description = stringResource(id = R.string.cursor_anim_desc),
      iconResource = R.drawable.animation,
      onNavigate = {
        cursorAnimation = !cursorAnimation
        SettingsData.setBoolean(Keys.CURSOR_ANIMATION_ENABLED, cursorAnimation)
        getActivity(MainActivity::class.java)?.let {
          (it as MainActivity).adapter.tabFragments.forEach { f ->
            f.value.get()?.editor?.isCursorAnimationEnabled = cursorAnimation
          }
        }
      },
      endWidget = {
        Switch(modifier = Modifier
          .padding(12.dp)
          .height(24.dp),
          checked = cursorAnimation,
          onCheckedChange = null)
      })

    PreferenceCategory(label = stringResource(id = R.string.show_line_number),
      description = stringResource(id = R.string.show_line_number),
      iconResource = R.drawable.linenumbers,
      onNavigate = {
        showLineNumber = !showLineNumber
        SettingsData.setBoolean(Keys.CURSOR_ANIMATION_ENABLED, showLineNumber)
        getActivity(MainActivity::class.java)?.let {
          (it as MainActivity).adapter.tabFragments.forEach { f ->
            f.value.get()?.editor?.isLineNumberEnabled = showLineNumber
          }
        }
      },
      endWidget = {
        Switch(modifier = Modifier
          .padding(12.dp)
          .height(24.dp),
          checked = showLineNumber,
          onCheckedChange = null)
      })

    PreferenceCategory(label = stringResource(id = R.string.pin_line_number),
      description = stringResource(id = R.string.pin_line_number),
      iconResource = R.drawable.linenumbers,
      onNavigate = {
        pinLineNumber = !pinLineNumber
        SettingsData.setBoolean(Keys.PIN_LINE_NUMBER, pinLineNumber)
        getActivity(MainActivity::class.java)?.let {
          (it as MainActivity).adapter.tabFragments.forEach { f ->
            f.value.get()?.editor?.setPinLineNumber(pinLineNumber)
          }
        }
      },
      endWidget = {
        Switch(modifier = Modifier
          .padding(12.dp)
          .height(24.dp),
          checked = pinLineNumber,
          onCheckedChange = null)
      })

    PreferenceCategory(label = stringResource(id = R.string.extra_keys),
      description = stringResource(id = R.string.extra_keys_desc),
      iconResource = R.drawable.double_arrows,
      onNavigate = {
        showArrowKeys = !showArrowKeys
        SettingsData.setBoolean(Keys.SHOW_ARROW_KEYS, showArrowKeys)
        MainActivity.activityRef.get()?.let { activity ->
          if (activity.tabViewModel.fragmentFiles.isEmpty()) {
            return@let
          }
          if (showArrowKeys) {
            activity.binding.apply {
              divider.visibility = View.VISIBLE
              mainBottomBar.visibility = View.VISIBLE
            }
          } else {
            activity.binding.apply {
              divider.visibility = View.GONE
              mainBottomBar.visibility = View.GONE
            }
          }

          val viewpager = activity.binding.viewpager2
          val layoutParams = viewpager.layoutParams as RelativeLayout.LayoutParams
          layoutParams.bottomMargin = rkUtils.dpToPx(
            if (showArrowKeys) {
              40f
            } else {
              0f
            }, activity
          )
          viewpager.setLayoutParams(layoutParams)

        }
      },
      endWidget = {
        Switch(modifier = Modifier
          .padding(12.dp)
          .height(24.dp),
          checked = showArrowKeys,
          onCheckedChange = null)
      })

    PreferenceCategory(label = stringResource(id = R.string.auto_save),
      description = stringResource(id = R.string.auto_save_desc),
      iconResource = R.drawable.save,
      onNavigate = {
        autoSave = !autoSave
        SettingsData.setBoolean(Keys.AUTO_SAVE, autoSave)
      },
      endWidget = {
        Switch(
          modifier = Modifier
            .padding(12.dp)
            .height(24.dp),
          checked = autoSave,
          onCheckedChange = null
        )
      })

    PreferenceCategory(label = stringResource(id = R.string.auto_save_time),
      description = stringResource(id = R.string.auto_save_time_desc),
      iconResource = R.drawable.save,
      onNavigate = {
        val view = LayoutInflater.from(context).inflate(R.layout.popup_new, null)
        val edittext = view.findViewById<EditText>(R.id.name).apply {
          hint = getString(R.string.intervalinMs)
          setText(SettingsData.getString(Keys.AUTO_SAVE_TIME_VALUE, "10000"))
          inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        MaterialAlertDialogBuilder(context).setTitle(getString(R.string.auto_save_time))
          .setView(view).setNegativeButton(getString(R.string.cancel), null)
          .setPositiveButton(getString(R.string.apply)) { _, _ ->
            val text = edittext.text.toString()
            for (c in text) {
              if (!c.isDigit()) {
                rkUtils.toast(getString(R.string.inavalid_v))
                return@setPositiveButton
              }
            }
            if (text.toInt() < 1000) {
              rkUtils.toast(getString(R.string.v_small))
              return@setPositiveButton
            }


            SettingsData.setString(Keys.AUTO_SAVE_TIME_VALUE, text)
            AutoSaver.delayTime = text.toLong()

          }.show()
      })



    PreferenceCategory(label = stringResource(id = R.string.text_size),
      description = stringResource(id = R.string.text_size_desc),
      iconResource = R.drawable.reorder,
      onNavigate = {

        val view = LayoutInflater.from(context).inflate(R.layout.popup_new, null)
        val edittext = view.findViewById<EditText>(R.id.name).apply {
          hint = getString(R.string.text_size)
          setText(SettingsData.getString(Keys.TEXT_SIZE, "14"))
          inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        MaterialAlertDialogBuilder(context).setTitle(getString(R.string.text_size)).setView(view)
          .setNegativeButton(getString(R.string.cancel), null)
          .setPositiveButton(getString(R.string.apply)) { _, _ ->
            val text = edittext.text.toString()
            for (c in text) {
              if (!c.isDigit()) {
                rkUtils.toast(getString(R.string.inavalid_v))
                return@setPositiveButton
              }
            }
            if (text.toInt() > 32) {
              rkUtils.toast(getString(R.string.v_large))
              return@setPositiveButton
            }
            if (text.toInt() < 8) {
              rkUtils.toast(getString(R.string.v_small))
              return@setPositiveButton
            }
            SettingsData.setString(Keys.TEXT_SIZE, text)
            MainActivity.activityRef.get()?.adapter?.tabFragments?.forEach { f ->
              f.value.get()?.editor?.setTextSize(text.toFloat())
            }

          }.show()

      })



    PreferenceCategory(label = stringResource(id = R.string.tab_size),
      description = stringResource(id = R.string.tab_size_desc),
      iconResource = R.drawable.double_arrows,
      onNavigate = {

        val view = LayoutInflater.from(context).inflate(R.layout.popup_new, null)
        val edittext = view.findViewById<EditText>(R.id.name).apply {
          hint = "Tab Size"
          setText(SettingsData.getString(Keys.TAB_SIZE, "4"))
          inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        MaterialAlertDialogBuilder(context).setTitle(getString(R.string.tab_size)).setView(view)
          .setNegativeButton(getString(R.string.cancel), null)
          .setPositiveButton(getString(R.string.apply)) { _, _ ->
            val text = edittext.text.toString()
            for (c in text) {
              if (!c.isDigit()) {
                rkUtils.toast(getString(R.string.inavalid_v))
                return@setPositiveButton
              }
            }
            if (text.toInt() > 16) {
              rkUtils.toast(getString(R.string.v_large))
              return@setPositiveButton
            }

            SettingsData.setString(Keys.TAB_SIZE, text)

            MainActivity.activityRef.get()?.adapter?.tabFragments?.forEach { f ->
              f.value.get()?.editor?.tabWidth = text.toInt()
            }

          }.show()

      })


    PreferenceCategory(label = stringResource(id = R.string.editor_font),
      description = stringResource(id = R.string.editor_font_desc),
      iconResource = R.drawable.baseline_font_download_24,
      onNavigate = {
        editorFont = !editorFont
        SettingsData.setBoolean(Keys.EDITOR_FONT, editorFont)
      },
      endWidget = {
        Switch(
          modifier = Modifier
            .padding(12.dp)
            .height(24.dp),
          checked = editorFont,
          onCheckedChange = null
        )
      })
  }
}