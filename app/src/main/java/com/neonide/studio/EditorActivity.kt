package com.neonide.studio

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.neonide.studio.R
import com.neonide.studio.app.EditorGradleManager
import com.neonide.studio.app.bottomsheet.EditorBottomSheetContent
import com.neonide.studio.app.bottomsheet.model.BottomSheetViewModel
import com.neonide.studio.app.buildoutput.BuildOutputBuffer
import com.neonide.studio.filetree.FileTreeDrawer
import com.neonide.studio.ui.theme.AppTheme
import io.github.rosemoe.sora.langs.java.JavaLanguage
import io.github.rosemoe.sora.widget.CodeEditor
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorActivity : ComponentActivity() {
    private val filePathState = mutableStateOf<String?>(null)
    private val bottomSheetVm: BottomSheetViewModel by viewModels()
    private val gradleManager: EditorGradleManager by lazy {
        EditorGradleManager(this, bottomSheetVm)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val projectPath = File(intent.getStringExtra("extra_project_dir") ?: return)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Add observer for build output
        lifecycleScope.launch {
            BuildOutputBuffer.output.collectLatest { output ->
                bottomSheetVm.setBuildOutput(output)
            }
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val mainContent = findViewById<ComposeView>(R.id.main_content)
        val drawerView = findViewById<ComposeView>(R.id.file_tree_drawer_view)

        mainContent.setContent {
            AppTheme {
                val scaffoldState = rememberBottomSheetScaffoldState()
                val scope = rememberCoroutineScope()
                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        EditorBottomSheetContent(
                            viewModel = bottomSheetVm
                        )
                    },
                    sheetPeekHeight = 30.dp,
                    topBar = {
                        AppTopBar(
                            onNavigationClick = { drawerLayout.openDrawer(Gravity.START) },
                            onBuildClick = {
                                gradleManager.onQuickRunOrCancel(projectPath)
                                scope.launch { scaffoldState.bottomSheetState.expand() }
                            }
                        )
                    }
                ) { padding ->
                    Editor(
                        modifier = Modifier.padding(padding),
                        filePath = filePathState.value
                    )
                }
            }
        }

        drawerView.setContent {
            AppTheme {
                FileTreeDrawer(
                    rootPath = projectPath.path,
                    onFileClick = { path ->
                        if (!path.endsWith(".apk", ignoreCase = true)) {
                            filePathState.value = path
                        }
                        drawerLayout.closeDrawer(Gravity.START)
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gradleManager.onDestroy()
    }
}

@Composable
fun AppTopBar(onNavigationClick: () -> Unit, onBuildClick: () -> Unit) {
    TopAppBar(
        modifier = Modifier.height(50.dp),
        title = {},
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onBuildClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Build/Run"
                )
            }
        }
    )
}

@Composable
fun Editor(modifier: Modifier = Modifier, filePath: String?) {
    var editor by remember {
        mutableStateOf<CodeEditor?>(null)
    }
    LaunchedEffect(filePath) {
        filePath?.let { path ->

            val content = withContext(Dispatchers.IO) {
                runCatching {
                    java.io.File(path).readText()
                }.getOrNull()
            }

            content?.let {
                editor?.setText(it)
            }
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            CodeEditor(context).apply {
                editor = this
                layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                typefaceText = Typeface.MONOSPACE
                setEditorLanguage(JavaLanguage())
                props.stickyScroll = true
                props.overScrollEnabled = true
                isCursorAnimationEnabled = true
                nonPrintablePaintingFlags =
                    CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or
                    CodeEditor.FLAG_DRAW_LINE_SEPARATOR or
                    CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION or
                    CodeEditor.FLAG_DRAW_SOFT_WRAP
            }
        },
        onRelease = { editor ->
            editor.release()
        }
    )
}
