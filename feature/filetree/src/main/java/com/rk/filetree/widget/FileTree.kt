package com.rk.filetree.widget

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rk.filetree.adapters.FileTreeAdapter
import com.rk.filetree.interfaces.FileClickListener
import com.rk.filetree.interfaces.FileIconProvider
import com.rk.filetree.interfaces.FileLongClickListener
import com.rk.filetree.interfaces.FileObject
import com.rk.filetree.model.Node
import com.rk.filetree.provider.DefaultFileIconProvider
import com.rk.filetree.util.Sorter

class FileTree : RecyclerView {
    private var fileTreeAdapter: FileTreeAdapter
    private lateinit var rootFileObject: FileObject

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    init {
        setItemViewCacheSize(100)
        layoutManager = LinearLayoutManager(context)
        fileTreeAdapter = FileTreeAdapter(context, this)
    }

    fun getRootFileObject(): FileObject {
        return rootFileObject
    }

    fun setIconProvider(fileIconProvider: FileIconProvider) {
        fileTreeAdapter.iconProvider = fileIconProvider
    }

    fun setOnFileClickListener(clickListener: FileClickListener) {
        fileTreeAdapter.onClickListener = clickListener
    }

    fun setOnFileLongClickListener(longClickListener: FileLongClickListener) {
        fileTreeAdapter.onLongClickListener = longClickListener
    }

    private var init = false
    private var showRootNode: Boolean = true

    fun showRootNode(): Boolean {
        return showRootNode
    }

    fun loadFiles(file: FileObject, showRootNodeX: Boolean? = null) {
        rootFileObject = file

        showRootNodeX?.let { showRootNode = it }

        val nodes: List<Node<FileObject>> =
            if (showRootNode) {
                mutableListOf<Node<FileObject>>().apply { add(Node(file)) }
            } else {
                Sorter.sort(file)
            }

        if (init.not()) {
            if (fileTreeAdapter.iconProvider == null) {
                fileTreeAdapter.iconProvider = DefaultFileIconProvider(context)
            }
            adapter = fileTreeAdapter
            init = true
        }
        fileTreeAdapter.submitList(nodes)
        if (showRootNode) {
            fileTreeAdapter.expandNode(nodes[0])
        }
    }

    fun reloadFileTree() {
        val nodes: List<Node<FileObject>> =
            if (showRootNode) {
                mutableListOf<Node<FileObject>>().apply { add(Node(rootFileObject)) }
            } else {
                Sorter.sort(rootFileObject)
            }
        fileTreeAdapter.submitList(nodes)
    }

    fun onFileAdded(file: FileObject) {
        fileTreeAdapter.newFile(file)
    }

    fun onFileRemoved(file: FileObject) {
        fileTreeAdapter.removeFile(file)
    }

    fun onFileRenamed(file: FileObject, newFileObject: FileObject) {
        fileTreeAdapter.renameFile(file, newFileObject)
    }
}
