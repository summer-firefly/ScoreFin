package com.chiptrack.app.ui.sticker

import androidx.annotation.DrawableRes
import com.chiptrack.app.R

enum class StickerMood(
    @DrawableRes val drawableRes: Int,
    val assetFileName: String,
    val caption: String
) {
    CUTE(R.drawable.sticker_cute, "sticker_cute.png", "卖萌待命"),
    LIKE(R.drawable.sticker_like, "sticker_like.png", "安排上了"),
    PROUD(R.drawable.sticker_proud, "sticker_proud.png", "这局不赖"),
    HAPPY(R.drawable.sticker_happy, "sticker_happy.png", "开心收工"),
    CRY(R.drawable.sticker_cry, "sticker_cry.png", "这局难受"),
    CONFUSED(R.drawable.sticker_confused, "sticker_confused.png", "账对不上？"),
    REFUSE(R.drawable.sticker_refuse, "sticker_refuse.png", "账目拒绝签字"),
    SAD(R.drawable.sticker_sad, "sticker_sad.png", "少还了呜呜");

    companion object {
        fun forSettle(totalProfit: Int, reconcileGap: Int): StickerMood {
            return when {
                reconcileGap > 0 -> CONFUSED
                reconcileGap < 0 -> REFUSE
                totalProfit > 0 -> PROUD
                totalProfit < 0 -> CRY
                else -> HAPPY
            }
        }

        fun forPlaying(): StickerMood = CUTE
    }
}
