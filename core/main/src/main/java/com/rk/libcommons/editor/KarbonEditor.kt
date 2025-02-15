package com.rk.libcommons.editor

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.rk.libcommons.application
import com.rk.libcommons.toastCatching
import com.rk.settings.Settings
import com.rk.settings.SettingsKey
import io.github.rosemoe.sora.text.ContentIO
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset


@Suppress("NOTHING_TO_INLINE")
class KarbonEditor : CodeEditor {
    constructor(context: Context) : super(context)
    
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    
    constructor(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)
    

    init{
        val darkTheme: Boolean = when (Settings.getString(
            SettingsKey.DEFAULT_NIGHT_MODE, "-1"
        ).toInt()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> Settings.isDarkMode(context)
        }

        val color = if (darkTheme){Color.BLACK}else{
            Color.WHITE
        }

        colorScheme.setColor(EditorColorScheme.WHOLE_BACKGROUND, color)
        colorScheme.setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, color)
        colorScheme.setColor(EditorColorScheme.LINE_DIVIDER, color)
    }
    
    
    suspend fun loadFile(inputStream: InputStream,encoding:Charset){
        withContext(Dispatchers.IO) {
            toastCatching {
                val content = ContentIO.createFrom(inputStream,encoding)
                inputStream.close()
                withContext(Dispatchers.Main) { setText(content) }
            }
        }
    }
    
    
    
    fun showSuggestions(yes: Boolean) {
        inputType = if (yes) {
            InputType.TYPE_TEXT_VARIATION_NORMAL
        } else {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
    }
    
    inline fun isShowSuggestion(): Boolean {
        return inputType != InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    }
    
    suspend fun saveToFile(outputStream:OutputStream,encoding:Charset){
        try {
            withContext(Dispatchers.IO){
                val content = withContext(Dispatchers.Main) { text }
                ContentIO.writeTo(content, outputStream,encoding, true)
            }
        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(application!!,e.message,Toast.LENGTH_SHORT).show()
            }
        }
        
    }
    private var isSearching: Boolean = false
    
    fun isSearching(): Boolean {
        return isSearching
    }
    
    fun setSearching(s: Boolean) {
        isSearching = s
    }
}
