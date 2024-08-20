package com.rk.xededitor.MainActivity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.view.KeyEvent
import android.view.Surface
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import com.blankj.utilcode.util.KeyboardUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.rk.libcommons.After
import com.rk.librunner.Runner
import com.rk.xededitor.Assets
import com.rk.xededitor.MainActivity.StaticData.mTabLayout
import com.rk.xededitor.MainActivity.treeview2.TreeView
import com.rk.xededitor.R
import com.rk.xededitor.Settings.SettingsData
import com.rk.xededitor.SetupEditor
import com.rk.xededitor.rkUtils
import java.io.File

class MainActivityAsync(activity: MainActivity) {
	init {
		
		with(activity){
			if (!SettingsData.isDarkMode(this)) {
				//light mode
				window.navigationBarColor = Color.parseColor("#FEF7FF")
				val decorView = window.decorView
				var flags = decorView.systemUiVisibility
				flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
				decorView.systemUiVisibility = flags
				window.statusBarColor = Color.parseColor("#FEF7FF")
			} else if (SettingsData.isDarkMode(this)) {
				if (SettingsData.isOled()) {
					binding!!.drawerLayout.setBackgroundColor(Color.BLACK)
					binding!!.navView.setBackgroundColor(Color.BLACK)
					binding!!.main.setBackgroundColor(Color.BLACK)
					binding!!.appbar.setBackgroundColor(Color.BLACK)
					binding!!.toolbar.setBackgroundColor(Color.BLACK)
					binding!!.tabs.setBackgroundColor(Color.BLACK)
					binding!!.mainView.setBackgroundColor(Color.BLACK)
					val window = window
					window.navigationBarColor = Color.BLACK
					window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
					window.statusBarColor = Color.BLACK
				} else {
					val window = window
					window.navigationBarColor = Color.parseColor("#141118")
					
				}
				
			}
		}
		
		Thread {
			Thread.currentThread().priority = 10
			with(activity) {
				
				SetupEditor.init(this)
				PermissionManager.verifyStoragePermission(this)
				
				
				
				mTabLayout.setOnTabSelectedListener(object : OnTabSelectedListener {
					override fun onTabSelected(tab: TabLayout.Tab) {
						viewPager?.setCurrentItem(tab.position)
						val fragment = StaticData.fragments[mTabLayout.selectedTabPosition]
						fragment.updateUndoRedo()
						StaticData.menu?.findItem(R.id.run)?.setVisible(fragment.file != null && Runner.isRunnable(fragment.file!!))
						
						if (!fragment.isSearching) {
							MenuClickHandler.hideSearchMenuItems()
						} else {
							//show search buttons
							MenuClickHandler.showSearchMenuItems()
						}
						
						
					}
					
					override fun onTabUnselected(tab: TabLayout.Tab) {}
					override fun onTabReselected(tab: TabLayout.Tab) {
						val popupMenu = PopupMenu(activity, tab.view)
						popupMenu.menuInflater.inflate(R.menu.tab_menu, popupMenu.menu)
						
						popupMenu.setOnMenuItemClickListener { item ->
							val id = item.itemId
							when (id) {
								R.id.close_this -> {
									adapter?.removeFragment(mTabLayout.selectedTabPosition)
								}
								
								R.id.close_others -> {
									adapter?.closeOthers(viewPager!!.currentItem)
								}
								
								R.id.close_all -> {
									adapter?.clear()
									StaticData.menu?.findItem(R.id.run)?.setVisible(false)
									
								}
							}
							
							
							for (i in 0 until mTabLayout.tabCount) {
								mTabLayout.getTabAt(i)?.setText(StaticData.fragments[i].fileName)
							}
							
							
							if (mTabLayout.tabCount < 1) {
								binding!!.tabs.visibility = View.GONE
								binding!!.mainView.visibility = View.GONE
								binding!!.openBtn.visibility = View.VISIBLE
							}
							MainActivity.updateMenuItems()
							true
						}
						popupMenu.show()
					}
				})
				
				//open last opened path
				val lastOpenedPath = SettingsData.getString(SettingsData.Keys.LAST_OPENED_PATH, "")
				if (lastOpenedPath.isNotEmpty()) {
					runOnUiThread {
						binding?.let {
							with(it) {
								mainView.visibility = View.VISIBLE
								safbuttons.visibility = View.GONE
								maindrawer.visibility = View.VISIBLE
								drawerToolbar.visibility = View.VISIBLE
							}
						}
					}
					
					StaticData.rootFolder = File(lastOpenedPath)
					runOnUiThread { TreeView(this, StaticData.rootFolder) }
					
					binding?.rootDirLabel?.text = StaticData.rootFolder.name.let {
						if (it.length > 18) it.substring(0, 15) + "..." else it
					}
				}
				
			}
			
			After(1000) {
				rkUtils.runOnUiThread {
					activity.onBackPressedDispatcher.addCallback(activity, object : OnBackPressedCallback(true) {
						override fun handleOnBackPressed() {
							
							
							//close drawer if opened
							if (activity.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
								activity.drawerLayout!!.closeDrawer(GravityCompat.START)
								return
							}
							
							
							var shouldExit = true
							
							var isModified = false
							if (StaticData.fragments != null) {
								for (fragment in StaticData.fragments) {
									
									if (fragment.isModified) {
										isModified = true
									}
								}
								if (isModified) {
									shouldExit = false
									val dialog: MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(activity).setTitle(activity.getString(R.string.unsaved))
										.setMessage(activity.getString(R.string.unsavedfiles)).setNegativeButton(activity.getString(R.string.cancel), null)
										.setPositiveButton(activity.getString(R.string.exit)) { dialogInterface: DialogInterface?, i: Int -> activity.finish() }
									
									
									dialog.setNeutralButton(activity.getString(R.string.saveexit)) { xdialog: DialogInterface?, which: Int ->
										activity.onOptionsItemSelected(StaticData.menu.findItem(R.id.action_all))
										activity.finish()
									}
									
									
									
									dialog.show()
								}
							}
							if (shouldExit) {
								activity.finish()
							}
						}
					})
				}
			}
			
			val intent: Intent = activity.intent
			val type = intent.type
			
			if (Intent.ACTION_SEND == intent.action && type != null) {
				if (type.startsWith("text")) {
					val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
					if (sharedText != null) {
						val file = File(activity.externalCacheDir, "newfile.txt")
						
						rkUtils.runOnUiThread {
							activity.newEditor(file, sharedText)
						}
						
						
						com.rk.libcommons.After(150) {
							rkUtils.runOnUiThread { activity.adapter?.onNewEditor() }
						}
					}
				}
			}
			
			
			rkUtils.runOnUiThread {
				val arrows = activity.binding!!.childs
				for (i in 0 until arrows.childCount) {
					val button = arrows.getChildAt(i)
					button.setOnClickListener { v ->
						val fragment = StaticData.fragments[mTabLayout.selectedTabPosition]
						val cursor = fragment.editor.cursor
						when (v.id) {
							R.id.left_arrow -> {
								if (cursor.leftColumn - 1 >= 0) {
									fragment.editor.setSelection(cursor.leftLine, cursor.leftColumn - 1)
								}
							}
							
							R.id.right_arrow -> {
								val lineNumber = cursor.leftLine
								val line = fragment.content!!.getLine(lineNumber)
								
								if (cursor.leftColumn < line.length) {
									fragment.editor.setSelection(cursor.leftLine, cursor.leftColumn + 1)
									
								}
								
							}
							
							R.id.up_arrow -> {
								if (cursor.leftLine - 1 >= 0) {
									val upline = cursor.leftLine - 1
									val uplinestr = fragment.content!!.getLine(upline)
									
									var columm = 0
									
									if (uplinestr.length < cursor.leftColumn) {
										columm = uplinestr.length
									} else {
										columm = cursor.leftColumn
									}
									
									
									fragment.editor.setSelection(cursor.leftLine - 1, columm)
								}
								
							}
							
							R.id.down_arrow -> {
								if (cursor.leftLine + 1 < fragment.content!!.lineCount) {
									
									val dnline = cursor.leftLine + 1
									val dnlinestr = fragment.content!!.getLine(dnline)
									
									var columm = 0
									
									if (dnlinestr.length < cursor.leftColumn) {
										columm = dnlinestr.length
									} else {
										columm = cursor.leftColumn
									}
									
									fragment.editor.setSelection(cursor.leftLine + 1, columm)
								}
							}
							
							R.id.tab -> {
								val tabsize = SettingsData.getString(SettingsData.Keys.TAB_SIZE, "4").toInt()
								val useSpaces = SettingsData.getBoolean(SettingsData.Keys.USE_SPACE_INTABS, true)
								
								if (useSpaces) {
									val sb = StringBuilder()
									for (i in 0 until tabsize) {
										sb.append(" ")
									}
									fragment.editor.insertText(sb.toString(), tabsize)
								} else {
									val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB)
									fragment.editor.dispatchKeyEvent(keyEvent)
								}
								
							}
							
							
							R.id.untab -> {
								if (cursor.leftColumn == 0) {
									return@setOnClickListener
								}
								
								val tabSize = SettingsData.getString(SettingsData.Keys.TAB_SIZE, "4").toInt()
								if (cursor.leftColumn >= tabSize) {
									fragment.editor.deleteText()
								}
								
							}
							
							R.id.home -> {
								fragment.editor.setSelection(cursor.leftLine, 0)
							}
							
							R.id.end -> {
								fragment.editor.setSelection(cursor.leftLine, fragment.content?.getLine(cursor.leftLine)?.length ?: 0)
							}
						}
					}
				}
			}
			
			
			rkUtils.runOnUiThread {
				val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
				val rotation = windowManager.defaultDisplay.rotation
				activity.binding!!.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
					override fun onGlobalLayout() {
						val r = Rect()
						activity.binding!!.root.getWindowVisibleDisplayFrame(r)
						val screenHeight = activity.binding!!.root.rootView.height
						val keypadHeight = screenHeight - r.bottom
						
						if (keypadHeight > screenHeight * 0.30) {
							if (rotation != Surface.ROTATION_0 && rotation != Surface.ROTATION_180) {
								KeyboardUtils.hideSoftInput(activity)
								rkUtils.toast(activity, "can't open keyboard in horizontal mode")
							}
						}
					}
				})
			}
			
		}.start()
	}
}