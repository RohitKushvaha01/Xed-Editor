package com.rk.xededitor.TabActivity.file

import android.content.Context
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.rk.filetree.widget.DiagonalScrollView
import com.rk.filetree.widget.FileTree
import com.rk.xededitor.Settings.Keys
import com.rk.xededitor.Settings.SettingsData
import kotlin.properties.Delegates


object FileTreeScrollViewManager {
    private fun dpToPx(dp: Int, density: Float): Int {
        return (dp * density).toInt()
    }

    var fileTreeViewId by Delegates.notNull<Int>()

    fun getFileTreeParentScrollView(context: Context, fileTree: FileTree?) : ViewGroup {
        fileTree?.let {
            fileTreeViewId = it.id
        }
        val density = context.resources.displayMetrics.density
        val isDiagonalScroll = SettingsData.getBoolean(Keys.DIAGONAL_SCROLL, false)
        val linearLayout = LinearLayout(context)


        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        val scrollView = if (isDiagonalScroll) {
            DiagonalScrollView(context).apply {
                layoutParams = params.apply {
                    setMargins(0, dpToPx(10, density), 0, 0)
                }
            }
        } else {
            HorizontalScrollView(context).apply {
                layoutParams = params
                isHorizontalScrollBarEnabled = false
            }
        }


        fileTree?.let {
            it.layoutParams = params
            (it.layoutParams as ViewGroup.MarginLayoutParams).apply {
                if (isDiagonalScroll) {
                    setMargins(0, dpToPx(3, density), 0, 0)
                } else {
                    setMargins(0, dpToPx(3, density), 0, 0)
                }
            }
            linearLayout.addView(fileTree)

        }

        linearLayout.apply {
            layoutParams = params

            if (isDiagonalScroll) {
                setPadding(0, 0, dpToPx(54, density), dpToPx(60, density))
            } else {
                setPadding(0, 0, dpToPx(54, density), dpToPx(5, density))
            }
            scrollView.addView(this)
        }



        return scrollView
    }


}