package com.chiptrack.app.ui

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Dialog.showWithAppFonts() {
    setOnShowListener { AppFonts.applyTree(window?.decorView) }
    show()
}

fun MaterialAlertDialogBuilder.showWithAppFonts(): AlertDialog {
    val dialog = create()
    dialog.showWithAppFonts()
    return dialog
}
