package com.chiptrack.app.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.chiptrack.app.R

/**
 * 英文与数字优先 Monaco，中文回退到华文楷体。
 * API 29+ 使用 [Typeface.CustomFallbackBuilder]；更低版本仅用楷体。
 */
object AppFonts {
    private const val KAI_ASSET = "fonts/huawen_kaiti.ttf"

    @Volatile
    private var mixed: Typeface? = null

    fun init(context: Context) {
        get(context)
    }

    fun get(context: Context): Typeface {
        mixed?.let { return it }
        return synchronized(this) {
            mixed ?: build(context.applicationContext).also { mixed = it }
        }
    }

    fun applyTree(root: View?) {
        if (root == null) return
        val typeface = get(root.context)
        applyTree(root, typeface)
    }

    private fun applyTree(view: View, typeface: Typeface) {
        if (view is TextView && view.typeface !== typeface) {
            view.typeface = typeface
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyTree(view.getChildAt(i), typeface)
            }
        }
    }

    private fun build(context: Context): Typeface {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val assets = context.assets
            val monaco = Font.Builder(context.resources, R.font.monaco).build()
            val kai = Font.Builder(assets, KAI_ASSET).build()
            return Typeface.CustomFallbackBuilder(FontFamily.Builder(monaco).build())
                .addCustomFallback(FontFamily.Builder(kai).build())
                .setSystemFallback("serif")
                .build()
        }
        return Typeface.createFromAsset(context.assets, KAI_ASSET)
            ?: ResourcesCompat.getFont(context, R.font.monaco)
            ?: Typeface.SERIF
    }
}
