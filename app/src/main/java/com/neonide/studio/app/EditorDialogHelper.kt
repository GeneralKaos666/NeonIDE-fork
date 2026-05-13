package com.neonide.studio.app

import android.app.AlertDialog
import android.graphics.Typeface
import androidx.activity.result.ActivityResultLauncher
import com.neonide.studio.R
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.schemes.SchemeDarcula
import io.github.rosemoe.sora.widget.schemes.SchemeEclipse
import io.github.rosemoe.sora.widget.schemes.SchemeGitHub
import io.github.rosemoe.sora.widget.schemes.SchemeNotepadXX
import io.github.rosemoe.sora.widget.schemes.SchemeVS2019
import java.io.File

/**
 * Helper for showing various configuration dialogs in the editor.
 */
class EditorDialogHelper(
    private val activity: SoraEditorActivityK,
    private val editor: CodeEditor,
    private val loadTMTLauncher: ActivityResultLauncher<String>
) {

    fun chooseTypeface() {
        val fonts = arrayOf("JetBrains Mono", "Ubuntu Mono", "Google/Roboto Mono")
        val assetsPaths =
            arrayOf("JetBrainsMono-Regular.ttf", "UbuntuMono-Regular.ttf", "RobotoMono-Regular.ttf")
        AlertDialog.Builder(activity)
            .setTitle(android.R.string.dialog_alert_title)
            .setSingleChoiceItems(fonts, -1) { dialog, which ->
                if (which in assetsPaths.indices) {
                    runCatching {
                        editor.typefaceText =
                            Typeface.createFromAsset(activity.assets, assetsPaths[which])
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun ensureTextmateTheme() {
        val cs = editor.colorScheme
        if (cs !is TextMateColorScheme) {
            editor.colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
        }
    }

    fun chooseTheme() {
        val themes = arrayOf(
            "Default",
            "GitHub",
            "Eclipse",
            "Darcula",
            "VS2019",
            "NotepadXX",
            "QuietLight for TM(VSCode)",
            "Darcula for TM",
            "Ayu Dark for VSCode",
            "Solarized(Dark) for TM(VSCode)",
            "TM theme from file"
        )
        AlertDialog.Builder(activity)
            .setTitle(R.string.sora_color_scheme)
            .setSingleChoiceItems(themes, -1) { dialog, which ->
                when (which) {
                    0 -> editor.colorScheme = EditorColorScheme()

                    1 -> editor.colorScheme = SchemeGitHub()

                    2 -> editor.colorScheme = SchemeEclipse()

                    3 -> editor.colorScheme = SchemeDarcula()

                    4 -> editor.colorScheme = SchemeVS2019()

                    5 -> editor.colorScheme = SchemeNotepadXX()

                    6 -> {
                        ensureTextmateTheme()
                        ThemeRegistry.getInstance().setTheme("quietlight")
                    }

                    7 -> {
                        ensureTextmateTheme()
                        ThemeRegistry.getInstance().setTheme("darcula")
                    }

                    8 -> {
                        ensureTextmateTheme()
                        ThemeRegistry.getInstance().setTheme("ayu-dark")
                    }

                    9 -> {
                        ensureTextmateTheme()
                        ThemeRegistry.getInstance().setTheme("solarized_dark")
                    }

                    10 -> {
                        loadTMTLauncher.launch("*/*")
                    }
                }

                val cs = editor.colorScheme
                editor.colorScheme = cs

                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
