package com.chiptrack.app.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chiptrack.app.R
import com.chiptrack.app.model.GameSession
import com.chiptrack.app.ui.showWithAppFonts
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * 不接微信 SDK：优先直达微信；记住上次目标 App；必要时再走系统选择器。
 */
object ShareHelper {
    private const val WECHAT_PACKAGE = "com.tencent.mm"

    fun shareSessionTable(
        fragment: Fragment,
        session: GameSession
    ) {
        val context = fragment.requireContext()
        if (session.players.isEmpty()) {
            Toast.makeText(context, R.string.share_empty, Toast.LENGTH_SHORT).show()
            return
        }
        val activity = fragment.requireActivity()
        val snapshotStore = ShareSnapshotStore(context)
        val deltas = snapshotStore.deltasFor(session)
        Toast.makeText(context, R.string.share_generating, Toast.LENGTH_SHORT).show()

        ScoreboardImageExporter.capture(activity, session, deltas) { tableResult ->
            tableResult
                .onSuccess { tableUri ->
                    snapshotStore.saveFromSession(session)
                    launchShare(fragment, listOf(tableUri))
                }
                .onFailure {
                    Toast.makeText(context, R.string.share_failed, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun launchShare(fragment: Fragment, uris: List<Uri>) {
        if (uris.isEmpty()) return
        val context = fragment.requireContext()
        val prefs = ShareTargetStore(context)
        val preferred = prefs.lastPackage
        val wechatInstalled = isPackageInstalled(context, WECHAT_PACKAGE)

        if (!preferred.isNullOrBlank() && canShareTo(context, preferred, uris)) {
            if (shareToPackage(fragment, uris, preferred)) return
        }

        if (wechatInstalled && canShareTo(context, WECHAT_PACKAGE, uris)) {
            if (shareToPackage(fragment, uris, WECHAT_PACKAGE)) {
                prefs.lastPackage = WECHAT_PACKAGE
                return
            }
        }

        showSharePicker(fragment, uris, wechatInstalled, prefs)
    }

    private fun showSharePicker(
        fragment: Fragment,
        uris: List<Uri>,
        wechatInstalled: Boolean,
        prefs: ShareTargetStore
    ) {
        val context = fragment.requireContext()
        val options = buildList {
            if (wechatInstalled) add(context.getString(R.string.share_to_wechat))
            add(context.getString(R.string.share_to_other))
        }.toTypedArray()

        if (options.size == 1) {
            openSystemChooser(fragment, uris)
            return
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.share_chooser_title)
            .setItems(options) { _, which ->
                val label = options[which]
                if (label == context.getString(R.string.share_to_wechat)) {
                    if (shareToPackage(fragment, uris, WECHAT_PACKAGE)) {
                        prefs.lastPackage = WECHAT_PACKAGE
                    } else {
                        Toast.makeText(context, R.string.share_wechat_failed, Toast.LENGTH_SHORT).show()
                        openSystemChooser(fragment, uris)
                    }
                } else {
                    openSystemChooser(fragment, uris)
                }
            }
            .setNegativeButton(R.string.dialog_cancel, null)
            .showWithAppFonts()
    }

    private fun shareToPackage(
        fragment: Fragment,
        uris: List<Uri>,
        packageName: String
    ): Boolean {
        val context = fragment.requireContext()
        return runCatching {
            uris.forEach { uri ->
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            val send = buildSendIntent(context, uris).apply {
                setPackage(packageName)
            }
            fragment.startActivity(send)
            true
        }.getOrElse {
            false
        }
    }

    private fun openSystemChooser(fragment: Fragment, uris: List<Uri>) {
        val context = fragment.requireContext()
        val send = buildSendIntent(context, uris)
        val chooser = Intent.createChooser(send, context.getString(R.string.share_chooser_title))
        chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        fragment.startActivity(chooser)
    }

    private fun buildSendIntent(context: Context, uris: List<Uri>): Intent {
        val caption = context.getString(R.string.share_text)
        return if (uris.size <= 1) {
            val uri = uris.first()
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, caption)
                clipData = ClipData.newUri(context.contentResolver, "share", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/png"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, caption)
                val clip = ClipData.newUri(context.contentResolver, "share", uris.first())
                uris.drop(1).forEach { clip.addItem(ClipData.Item(it)) }
                clipData = clip
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)

    private fun canShareTo(
        context: Context,
        packageName: String,
        uris: List<Uri>
    ): Boolean {
        if (!isPackageInstalled(context, packageName)) return false
        val intent = buildSendIntent(context, uris).apply { setPackage(packageName) }
        return intent.resolveActivity(context.packageManager) != null
    }
}

/** 记住上次直达分享的目标包名 */
class ShareTargetStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var lastPackage: String?
        get() = prefs.getString(KEY_PACKAGE, null)
        set(value) {
            prefs.edit().putString(KEY_PACKAGE, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "chiptrack_share_target"
        private const val KEY_PACKAGE = "last_package"
    }
}
