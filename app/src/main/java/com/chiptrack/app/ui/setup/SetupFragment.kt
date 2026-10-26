package com.chiptrack.app.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.chiptrack.app.GameSessionViewModel
import com.chiptrack.app.R
import com.chiptrack.app.data.NamedPlayersPreset
import com.chiptrack.app.data.PlayersPresetRepository
import com.chiptrack.app.databinding.DialogCreatePresetBinding
import com.chiptrack.app.databinding.FragmentSetupBinding
import com.chiptrack.app.databinding.ItemSetupPlayerBinding
import com.chiptrack.app.model.SessionPhase
import com.chiptrack.app.share.macro.ShareMacroSettingsUi
import com.chiptrack.app.ui.navigateOnce
import com.chiptrack.app.ui.showWithAppFonts
import com.chiptrack.app.ui.sticker.StickerMood
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class SetupFragment : Fragment() {
    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameSessionViewModel by activityViewModels()
    private val nameInputs = mutableListOf<TextInputEditText>()
    private lateinit var presetRepo: PlayersPresetRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presetRepo = PlayersPresetRepository(requireContext())
        binding.heroSticker.bind(StickerMood.CUTE, sizeDp = 88, rotation = -3f)
        showEmptySticker(StickerMood.CUTE, getString(R.string.sticker_empty_caption))

        updateEmptyHint()
        binding.importPresetButton.setOnClickListener { showImportPresetDialog() }
        binding.createPresetButton.setOnClickListener { showCreatePresetDialog() }
        binding.deletePresetButton.setOnClickListener { showDeletePresetDialog() }
        binding.shareMacroSettingsButton.setOnClickListener {
            ShareMacroSettingsUi.show(requireContext())
        }
        binding.addPlayerButton.setOnClickListener { addPlayerRow() }
        binding.startButton.setOnClickListener { startGame() }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                viewModel.consumeMessage()
            }
        }
        viewModel.session.observe(viewLifecycleOwner) { session ->
            when (session.phase) {
                SessionPhase.PLAYING -> navigateOnce(R.id.action_setup_to_table)
                SessionPhase.SETTLED -> navigateOnce(R.id.action_setup_to_settle)
                SessionPhase.SETUP -> Unit
            }
        }
    }

    private fun showImportPresetDialog() {
        val presets = presetRepo.list()
        if (presets.isEmpty()) {
            Toast.makeText(requireContext(), R.string.preset_empty, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = presets.map {
            getString(R.string.preset_item_label, it.name, it.players.size)
        }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preset_import_title)
            .setItems(labels) { _, which -> applyPreset(presets[which]) }
            .setNegativeButton(R.string.dialog_cancel, null)
            .showWithAppFonts()
    }

    private fun showDeletePresetDialog() {
        val presets = presetRepo.list()
        if (presets.isEmpty()) {
            Toast.makeText(requireContext(), R.string.preset_empty, Toast.LENGTH_SHORT).show()
            return
        }
        val labels = presets.map {
            getString(R.string.preset_item_label, it.name, it.players.size)
        }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preset_delete_title)
            .setItems(labels) { _, which -> confirmDeletePreset(presets[which]) }
            .setNegativeButton(R.string.dialog_cancel, null)
            .showWithAppFonts()
    }

    private fun confirmDeletePreset(preset: NamedPlayersPreset) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preset_delete_confirm_title)
            .setMessage(getString(R.string.preset_delete_confirm_message, preset.name))
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                if (presetRepo.delete(preset.id)) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.preset_deleted, preset.name),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .showWithAppFonts()
    }

    private fun showCreatePresetDialog() {
        val dialogBinding = DialogCreatePresetBinding.inflate(layoutInflater)
        dialogBinding.presetNameInput.setText(suggestPresetName())
        dialogBinding.presetNameInput.setSelection(dialogBinding.presetNameInput.text?.length ?: 0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.preset_create_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val presetName = dialogBinding.presetNameInput.text?.toString().orEmpty().trim()
                val players = dialogBinding.presetPlayersInput.text?.toString().orEmpty()
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                when {
                    presetName.isEmpty() ->
                        Toast.makeText(requireContext(), R.string.preset_name_required, Toast.LENGTH_SHORT)
                            .show()
                    players.isEmpty() ->
                        Toast.makeText(requireContext(), R.string.preset_players_required, Toast.LENGTH_SHORT)
                            .show()
                    else -> {
                        val buyIn = binding.buyInInput.text?.toString()?.toIntOrNull()
                        val saved = presetRepo.save(
                            name = presetName,
                            players = players,
                            defaultBuyIn = buyIn
                        )
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.preset_created, saved.name),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .showWithAppFonts()
    }

    private fun suggestPresetName(): String {
        val existing = presetRepo.list().map { it.name }.toSet()
        if ("常用名单" !in existing) return "常用名单"
        var index = 2
        while ("常用名单$index" in existing) index++
        return "常用名单$index"
    }

    private fun applyPreset(preset: NamedPlayersPreset) {
        clearPlayerRows()
        preset.players.forEachIndexed { index, name ->
            addPlayerRow(name, requestFocus = index == preset.players.lastIndex)
        }
        preset.defaultBuyIn?.takeIf { it > 0 }?.let {
            binding.buyInInput.setText(it.toString())
        }
        binding.heroSticker.bind(StickerMood.LIKE, sizeDp = 88, rotation = -3f)
        showEmptySticker(StickerMood.LIKE, getString(R.string.sticker_import_caption))
        Toast.makeText(
            requireContext(),
            getString(R.string.preset_players_loaded, preset.name, preset.players.size),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEmptySticker(mood: StickerMood, caption: String) {
        binding.emptySticker.bind(mood, sizeDp = 128, rotation = -6f)
        binding.emptyHint.text = caption
    }

    private fun currentPlayerNames(): List<String> =
        nameInputs.map { it.text?.toString()?.trim().orEmpty() }

    private fun clearPlayerRows() {
        binding.playerNamesContainer.removeAllViews()
        nameInputs.clear()
        updateEmptyHint()
    }

    private fun addPlayerRow(prefill: String = "", requestFocus: Boolean = true) {
        val rowBinding = ItemSetupPlayerBinding.inflate(
            layoutInflater,
            binding.playerNamesContainer,
            false
        )
        if (prefill.isNotBlank()) {
            rowBinding.nameInput.setText(prefill)
        }
        rowBinding.removeButton.setOnClickListener {
            binding.playerNamesContainer.removeView(rowBinding.root)
            nameInputs.remove(rowBinding.nameInput)
            updateEmptyHint()
        }
        binding.playerNamesContainer.addView(rowBinding.root)
        nameInputs += rowBinding.nameInput
        updateEmptyHint()
        if (requestFocus) {
            rowBinding.nameInput.requestFocus()
            scrollRowIntoView(rowBinding.root)
        }
    }

    private fun scrollRowIntoView(target: View) {
        binding.setupScroll.post {
            val childOffset = IntArray(2)
            val scrollOffset = IntArray(2)
            target.getLocationOnScreen(childOffset)
            binding.setupScroll.getLocationOnScreen(scrollOffset)
            val y = childOffset[1] - scrollOffset[1] + binding.setupScroll.scrollY - 24
            binding.setupScroll.smoothScrollTo(0, y.coerceAtLeast(0))
        }
    }

    private fun updateEmptyHint() {
        val empty = nameInputs.isEmpty()
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        if (empty && binding.emptyHint.text.isNullOrBlank()) {
            showEmptySticker(StickerMood.CUTE, getString(R.string.sticker_empty_caption))
        }
    }

    private fun startGame() {
        val buyIn = binding.buyInInput.text?.toString()?.toIntOrNull()
        if (buyIn == null || buyIn <= 0) {
            Toast.makeText(requireContext(), R.string.error_invalid_amount, Toast.LENGTH_SHORT).show()
            return
        }
        val names = currentPlayerNames()
        if (names.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_need_players, Toast.LENGTH_SHORT).show()
            return
        }
        if (names.any { it.isBlank() }) {
            Toast.makeText(requireContext(), R.string.error_empty_name, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.startGame(names, buyIn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        nameInputs.clear()
    }
}
