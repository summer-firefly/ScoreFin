package com.chiptrack.app.ui

import android.content.Context
import androidx.core.content.ContextCompat
import com.chiptrack.app.R

object ProfitFormat {
    fun text(context: Context, profit: Int): String = when {
        profit > 0 -> context.getString(R.string.profit_positive, profit)
        profit < 0 -> context.getString(R.string.profit_negative, profit)
        else -> context.getString(R.string.profit_zero)
    }

    fun color(context: Context, profit: Int): Int {
        val res = when {
            profit > 0 -> R.color.profit_positive
            profit < 0 -> R.color.profit_negative
            else -> R.color.text_secondary
        }
        return ContextCompat.getColor(context, res)
    }
}
