package com.neonide.studio.app.bottomsheet

import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.termux.shared.termux.TermuxConstants
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

@Composable
fun TerminalTab(projectPath: String) {
    AndroidView(
        factory = { context ->
            var textSize = 30

            TerminalView(context, null).apply {
                isFocusable = true
                isFocusableInTouchMode = true
                setTextSize(textSize)

                setTerminalViewClient(object :
                    TerminalViewClient by DefaultTerminalView {
                    override fun onSingleTapUp(e: MotionEvent) {
                        requestFocus()
                        val imm = context.getSystemService(
                            Context.INPUT_METHOD_SERVICE
                        ) as InputMethodManager
                        imm.showSoftInput(
                            this@apply,
                            InputMethodManager.SHOW_IMPLICIT
                        )
                    }

                    override fun onScale(scale: Float): Float {
                        if (scale < 0.9f || scale > 1.1f) {
                            textSize =
                                if (scale >
                                    1f
                                ) {
                                    textSize + 5
                                } else {
                                    (textSize - 5).coerceAtLeast(10)
                                }
                            setTextSize(textSize)
                            return 1.0f
                        }
                        return scale
                    }
                })

                attachSession(
                    TerminalSession(
                        "${TermuxConstants.TERMUX_PREFIX_DIR_PATH}/bin/bash",
                        projectPath,
                        arrayOf(),
                        arrayOf(
                            "TERM=xterm-256color",
                            "HOME=${TermuxConstants.TERMUX_HOME_DIR_PATH}"
                        ),
                        TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                        object : TerminalSessionClient by DefaultTerminalSession {
                            override fun onTextChanged(session: TerminalSession) {
                                post { onScreenUpdated() }
                            }
                        }
                    )
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
