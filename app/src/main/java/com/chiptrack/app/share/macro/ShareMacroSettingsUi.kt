package com.chiptrack.app.share.macro

import android.content.Context
import android.view.LayoutInflater
import com.chiptrack.app.R
import com.chiptrack.app.databinding.DialogShareMacroSettingsBinding
import com.chiptrack.app.ui.showWithAppFonts
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object ShareMacroSettingsUi {

    fun show(context: Context) {
        val store = ShareMacroStore(context)
        val binding = DialogShareMacroSettingsBinding.inflate(LayoutInflater.from(context))
        bindStatus(context, binding, store)

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.share_macro_settings_title)
            .setView(binding.root)
            .setPositiveButton(R.string.share_macro_settings_done, null)
            .create()

        binding.toggleFeatureButton.setOnClickListener {
            store.featureEnabled = !store.featureEnabled
            bindStatus(context, binding, store)
        }
        binding.openA11yButton.setOnClickListener {
            ShareMacroController.openAccessibilitySettings(context)
            dialog.dismiss()
        }
        binding.clearMacroButton.setOnClickListener {
            ShareMacroController.clearMacro(context)
            bindStatus(context, binding, store)
        }

        dialog.showWithAppFonts()
    }

    private fun bindStatus(
        context: Context,
        binding: DialogShareMacroSettingsBinding,
        store: ShareMacroStore
    ) {
        val enabled = store.featureEnabled
        binding.featureStatusText.text = if (enabled) {
            context.getString(R.string.share_macro_feature_on)
        } else {
            context.getString(R.string.share_macro_feature_off)
        }
        binding.a11yStatusText.text = if (ShareMacroController.isServiceEnabled(context)) {
            context.getString(R.string.share_macro_a11y_on)
        } else {
            context.getString(R.string.share_macro_a11y_off)
        }
        val steps = store.steps.size
        binding.stepsStatusText.text = if (steps > 0) {
            context.getString(R.string.share_macro_steps_count, steps)
        } else {
            context.getString(R.string.share_macro_steps_empty)
        }
        binding.toggleFeatureButton.setText(
            if (enabled) R.string.share_macro_action_disable
            else R.string.share_macro_action_enable
        )
        binding.clearMacroButton.isEnabled = steps > 0
    }
}
