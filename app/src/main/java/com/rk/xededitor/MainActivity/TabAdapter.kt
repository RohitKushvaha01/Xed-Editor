package com.rk.xededitor.MainActivity

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.rk.xededitor.MainActivity.file.getFragmentType
import com.rk.xededitor.MainActivity.tabs.core.FragmentType
import com.rk.xededitor.MainActivity.tabs.editor.EditorFragment
import com.rk.xededitor.R
import com.rk.xededitor.rkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.ref.WeakReference

class Kee(val file: File) {
    override fun equals(other: Any?): Boolean {
        if (other !is Kee) {
            return false
        }
        return other.file.absolutePath == file.absolutePath
    }
    
    override fun hashCode(): Int {
        return file.absolutePath.hashCode()
    }
}

private var nextItemId = 0L
const val tabLimit = 20
var currentTab: WeakReference<TabLayout.Tab?> = WeakReference(null)

class TabAdapter(private val mainActivity: MainActivity) : FragmentStateAdapter(mainActivity.supportFragmentManager, mainActivity.lifecycle) {
    
    val tabFragments = HashMap<Kee, WeakReference<TabFragment>>()
    
    // this is hell
    fun getCurrentFragment(): TabFragment? {
        if (mainActivity.tabLayout!!.selectedTabPosition == -1 || mainActivity.tabViewModel.fragmentFiles.isEmpty()) {
            tabFragments.clear()
            return null
        }
        
        if (currentTab.get()?.position == -1) {
            return null
        }
        
        currentTab.get()?.let { tab ->
            tabFragments[Kee(mainActivity.tabViewModel.fragmentFiles[tab.position])]?.get()?.let {
                return it
            }
        }
        val f = tabFragments[Kee(
            mainActivity.tabViewModel.fragmentFiles[mainActivity.tabLayout!!.selectedTabPosition]
        )]
        return f?.get()
    }
    
    private val itemIds = mutableMapOf<Int, Long>()
    
    override fun getItemCount(): Int = mainActivity.tabViewModel.fragmentFiles.size
    
    override fun createFragment(position: Int): Fragment {
        val file = mainActivity.tabViewModel.fragmentFiles[position]
        val type = mainActivity.tabViewModel.fragmentTypes[position]
        return TabFragment.newInstance(file, type).apply { tabFragments[Kee(file)] = WeakReference(this) }
    }
    
    override fun getItemId(position: Int): Long {
        if (!itemIds.containsKey(position)) {
            itemIds[position] = nextItemId++
        }
        return itemIds[position]!!
    }
    
    override fun containsItem(itemId: Long): Boolean {
        return itemIds.containsValue(itemId)
    }
    
    fun notifyItemRemovedX(position: Int) {
        // Shift all items after the removed position
        for (i in position until itemIds.size - 1) {
            itemIds[i] = itemIds[i + 1]!!
        }
        // Remove the last item
        itemIds.remove(itemIds.size - 1)
        notifyItemRemoved(position)
    }
    
    fun notifyItemInsertedX(position: Int) {
        // Shift all items from the inserted position
        for (i in itemIds.size - 1 downTo position) {
            itemIds[i + 1] = itemIds[i]!!
        }
        // Add new item ID
        itemIds[position] = nextItemId++
        notifyItemInserted(position)
    }
    
    @SuppressLint("NotifyDataSetChanged")
    fun clearAllFragments() {
        with(mainActivity) {
            tabViewModel.fileSet.clear()
            tabFragments.values.forEach { pointer -> pointer.get()?.fragment?.onClosed() }
            tabViewModel.fragmentFiles.clear()
            tabViewModel.fragmentTypes.clear()
            tabFragments.clear()
            tabViewModel.fragmentTitles.clear()
            (viewPager?.adapter as? TabAdapter)?.notifyDataSetChanged()
            binding!!.tabs.visibility = View.GONE
            binding!!.mainView.visibility = View.GONE
            binding!!.openBtn.visibility = View.VISIBLE
        }
    }
    
    fun removeFragment(position: Int, askUser: Boolean = true) {
        with(mainActivity) {
            if (position >= 0 && position < tabViewModel.fragmentFiles.size) {
                
                fun close() {
                    tabFragments.remove(Kee(mainActivity.tabViewModel.fragmentFiles[position]))
                    tabViewModel.fileSet.remove(tabViewModel.fragmentFiles[position].absolutePath)
                    
                    synchronized(EditorFragment.set) {
                        EditorFragment.set.remove(tabViewModel.fragmentFiles[position].name)
                    }
                    
                    tabViewModel.fragmentFiles.removeAt(position)
                    tabViewModel.fragmentTitles.removeAt(position)
                    tabViewModel.fragmentTypes.removeAt(position)
                    
                    (viewPager?.adapter as? TabAdapter)?.apply { notifyItemRemovedX(position) }
                    
                }
                
                
                
                tabFragments[Kee(mainActivity.tabViewModel.fragmentFiles[position])]!!.get()?.fragment?.let {
                    
                    if (askUser.not()) {
                        it.onClosed()
                        close()
                    } else if (it is EditorFragment && it.isModified()) {
                        askClose(
                            title = "Unsaved File",
                            message = "Are you sure you want to discard this unsaved document?",
                            onCancel = {},
                            onClose = {
                            it.onClosed()
                            close()
                        })
                    }
                    
                }
                
            }
            if (tabViewModel.fragmentFiles.isEmpty()) {
                binding!!.tabs.visibility = View.GONE
                binding!!.mainView.visibility = View.GONE
                binding!!.openBtn.visibility = View.VISIBLE
            }
        }
    }
    
    fun clearAllFragmentsExceptSelected() {
        mainActivity.lifecycleScope.launch(Dispatchers.Main) {
            val selectedTabPosition = mainActivity.tabLayout?.selectedTabPosition
            var shouldAsk = false
            
            tabFragments.values.forEach { p -> p.get()?.fragment?.apply {
                if (this is EditorFragment){
                    if (isModified()){
                        shouldAsk = true
                        return@forEach
                    }
                }
            } }
            
            fun close(){
                // Iterate backwards to avoid index shifting issues when removing fragments
                for (i in mainActivity.tabLayout!!.tabCount - 1 downTo 0) {
                    if (i != selectedTabPosition) {
                        removeFragment(i, false)
                    }
                }
            }
            
            if (shouldAsk){
                askClose(
                    title = "Unsaved Files",
                    message = "Some files are not saved",
                    onCancel = {},
                    onClose = {
                        close()
                    })
            }else{
                close()
            }
            
            
        }
    }
    
    fun addFragment(file: File, fragmentType: FragmentType? = null) {
        val type = fragmentType ?: file.getFragmentType()
        if ((type == FragmentType.EDITOR) && (file.length() / (1024.0 * 1024.0)) > 10) {
            rkUtils.toast(rkUtils.getString(R.string.file_too_large))
            return
        }
        
        with(mainActivity) {
            if (tabViewModel.fileSet.contains(file.absolutePath)) {
                rkUtils.toast(getString(R.string.already_opened))
                return
            }
            if (tabViewModel.fragmentFiles.size >= tabLimit) {
                rkUtils.toast(
                    "${getString(R.string.open_cant)} $tabLimit ${getString(R.string.files)}"
                )
                return
            }
            tabViewModel.fileSet.add(file.absolutePath)
            tabViewModel.fragmentFiles.add(file)
            tabViewModel.fragmentTitles.add(file.name)
            tabViewModel.fragmentTypes.add(type)
            
            (viewPager?.adapter as? TabAdapter)?.notifyItemInsertedX(
                tabViewModel.fragmentFiles.size - 1
            )
            if (tabViewModel.fragmentFiles.size > 1) viewPager?.setCurrentItem(tabViewModel.fragmentFiles.size - 1, false)
            binding!!.tabs.visibility = View.VISIBLE
            binding!!.mainView.visibility = View.VISIBLE
            binding!!.openBtn.visibility = View.GONE
        }
    }
    
    
    private fun askClose(onCancel: () -> Unit, onClose: () -> Unit,title:String,message:String) {
        MaterialAlertDialogBuilder(mainActivity).setTitle(title).setMessage(message)
            .setNegativeButton("Cancel") { _, _ ->
                onCancel.invoke()
            }.setPositiveButton("Close") { _, _ ->
                onClose.invoke()
            }.show()
    }
}
