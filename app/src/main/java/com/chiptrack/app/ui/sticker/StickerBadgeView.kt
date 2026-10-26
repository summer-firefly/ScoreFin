package com.chiptrack.app.ui.sticker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.chiptrack.app.databinding.ViewStickerBadgeBinding

class StickerBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStickerBadgeBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        clipChildren = false
        clipToPadding = false
    }

    fun bind(
        mood: StickerMood,
        sizeDp: Int = 112,
        rotation: Float = -4f
    ) {
        val density = resources.displayMetrics.density
        val px = (sizeDp * density).toInt()
        binding.stickerCard.layoutParams = binding.stickerCard.layoutParams.apply {
            width = px
            height = px
        }
        binding.stickerCard.rotation = rotation
        binding.stickerImage.setImageResource(mood.drawableRes)
        contentDescription = mood.caption
    }
}
