package com.example

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.DocumentEditorViewModel
import com.example.ui.EditorMode
import com.example.ui.components.AuthDialog
import com.example.ui.components.DocumentCanvasView
import com.example.ui.components.DocumentHistorySheet
import com.example.ui.components.ExportSuccessDialog
import com.example.ui.components.SampleDocumentPickerSheet
import com.example.ui.components.StampControlsPanel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SealCrimson
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.io.File

class MainActivity : ComponentActivity() {

    private val viewModel: DocumentEditorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: DocumentEditorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showHistorySheet by remember { mutableStateOf(false) }

    // Android Photo Picker (zero-permission media picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadBitmapFromUri(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Waraqaa Gulaalaa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val badgeBg = when (uiState.mode) {
                                EditorMode.SELECT_STAMP -> SealCrimson.copy(alpha = 0.15f)
                                EditorMode.MOVE_STAMP -> EmeraldSuccess.copy(alpha = 0.15f)
                                EditorMode.EDIT_TEXT -> SkyAccent.copy(alpha = 0.15f)
                            }
                            val badgeColor = when (uiState.mode) {
                                EditorMode.SELECT_STAMP -> SealCrimson
                                EditorMode.MOVE_STAMP -> EmeraldSuccess
                                EditorMode.EDIT_TEXT -> SkyAccent
                            }
                            val badgeText = when (uiState.mode) {
                                EditorMode.SELECT_STAMP -> "1. Filadhu"
                                EditorMode.MOVE_STAMP -> "2. Kaayi"
                                EditorMode.EDIT_TEXT -> "3. Maqaa"
                            }
                            Box(
                                modifier = Modifier
                                    .background(color = badgeBg, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }
                        Text(
                            text = uiState.activeDocumentTitle,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    // Language Switcher Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable { viewModel.toggleLanguage() }
                            .padding(end = 6.dp)
                            .testTag("btn_toggle_lang")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                modifier = Modifier.size(14.dp),
                                tint = SkyAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.language == AppLanguage.OROMO) "Oromoo" else "English",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Choose / Upload Document button
                    IconButton(
                        onClick = { viewModel.setShowSampleSheet(true) },
                        modifier = Modifier.testTag("btn_open_doc_picker")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Choose Document",
                            tint = SkyAccent
                        )
                    }

                    // History button with count badge
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("btn_open_history")
                    ) {
                        if (uiState.savedDocuments.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = EmeraldSuccess) {
                                        Text("${uiState.savedDocuments.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Saved Documents History"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Saved Documents History"
                            )
                        }
                    }

                    // Account profile / Authentication icon button
                    IconButton(
                        onClick = { viewModel.setShowAuthDialog(true) },
                        modifier = Modifier.testTag("btn_open_account")
                    ) {
                        if (uiState.currentUser != null) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(EmeraldSuccess, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.currentUser!!.fullName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account & Login"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            StampControlsPanel(
                mode = uiState.mode,
                language = uiState.language,
                newW = uiState.newW,
                newH = uiState.newH,
                rotationDegrees = uiState.rotationDegrees,
                lockAspectRatio = uiState.lockAspectRatio,
                matchPaperBackground = uiState.matchPaperBackground,
                transparentStampBackground = uiState.transparentStampBackground,
                // Text editing parameters
                replacementText = uiState.replacementText,
                textFontSize = uiState.textFontSize,
                textFontFamily = uiState.textFontFamily,
                isTextBold = uiState.isTextBold,
                isTextItalic = uiState.isTextItalic,
                textAlignment = uiState.textAlignment,
                textInkColorHex = uiState.textInkColorHex,
                isProcessing = uiState.isProcessing,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                onModeChange = { viewModel.setMode(it) },
                onSizeChange = { w, h -> viewModel.setNewSize(w, h) },
                onRotationChange = { viewModel.setRotation(it) },
                onRotateBy = { viewModel.rotateBy(it) },
                onLockAspectRatioToggle = { viewModel.setLockAspectRatio(it) },
                onMatchPaperToggle = { viewModel.setMatchPaperBackground(it) },
                onTransparentStampToggle = { viewModel.setTransparentStampBackground(it) },
                onApplyEdit = { viewModel.applyEdit() },
                // Text editing callbacks
                onReplacementTextChange = { viewModel.setReplacementText(it) },
                onFontSizeChange = { viewModel.setTextFontSize(it) },
                onFontFamilyChange = { viewModel.setTextFontFamily(it) },
                onBoldToggle = { viewModel.setTextBold(it) },
                onItalicToggle = { viewModel.setTextItalic(it) },
                onAlignmentChange = { viewModel.setTextAlignment(it) },
                onInkColorChange = { viewModel.setTextInkColorHex(it) },
                onApplyTextReplacement = { viewModel.applyTextReplacement() },
                onExportPdf = { viewModel.exportPdf() },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF1F5F9))
            ) {
                // Status banner matching prompt's <h4 id="status">Haala jiru: ...</h4>
                val bannerColor = when (uiState.mode) {
                    EditorMode.SELECT_STAMP -> SealCrimson
                    EditorMode.MOVE_STAMP -> EmeraldSuccess
                    EditorMode.EDIT_TEXT -> SkyAccent
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = bannerColor.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Interactive Document Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    DocumentCanvasView(
                        bitmap = uiState.currentBitmap,
                        mode = uiState.mode,
                        language = uiState.language,
                        cropX = uiState.cropX,
                        cropY = uiState.cropY,
                        cropW = uiState.cropW,
                        cropH = uiState.cropH,
                        newX = uiState.newX,
                        newY = uiState.newY,
                        newW = uiState.newW,
                        newH = uiState.newH,
                        rotationDegrees = uiState.rotationDegrees,
                        // Text editing parameters
                        textTargetX = uiState.textTargetX,
                        textTargetY = uiState.textTargetY,
                        textTargetW = uiState.textTargetW,
                        textTargetH = uiState.textTargetH,
                        replacementText = uiState.replacementText,
                        textFontSize = uiState.textFontSize,
                        textFontFamily = uiState.textFontFamily,
                        isTextBold = uiState.isTextBold,
                        isTextItalic = uiState.isTextItalic,
                        textAlignment = uiState.textAlignment,
                        textInkColorHex = uiState.textInkColorHex,
                        onCropChange = { x, y, w, h -> viewModel.updateCrop(x, y, w, h) },
                        onNewPositionChange = { x, y -> viewModel.updateNewPosition(x, y) },
                        onTextTargetChange = { x, y, w, h -> viewModel.updateTextTarget(x, y, w, h) }
                    )
                }
            }
        }
    )

    // Sample Picker Sheet
    if (uiState.showSampleSheet) {
        SampleDocumentPickerSheet(
            language = uiState.language,
            onDismiss = { viewModel.setShowSampleSheet(false) },
            onSelectSample = { sampleIndex -> viewModel.loadSampleDocument(sampleIndex) },
            onUploadClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    // Export Success Dialog
    if (uiState.showSuccessExportDialog) {
        ExportSuccessDialog(
            pdfFile = uiState.exportedPdfFile,
            language = uiState.language,
            onDismiss = { viewModel.dismissExportDialog() }
        )
    }

    // History Sheet
    if (showHistorySheet) {
        DocumentHistorySheet(
            documents = uiState.savedDocuments,
            language = uiState.language,
            onDismiss = { showHistorySheet = false },
            onSelectDocument = { doc ->
                val file = File(doc.editedImagePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        viewModel.loadBitmapFromUri(android.net.Uri.fromFile(file))
                    }
                }
            },
            onDeleteDocument = { doc ->
                viewModel.deleteDocumentRecord(doc)
            }
        )
    }

    // User Authentication & Registration Dialog
    if (uiState.showAuthDialog) {
        AuthDialog(
            currentUser = uiState.currentUser,
            language = uiState.language,
            userDocumentCount = uiState.savedDocuments.size,
            errorMessage = uiState.authErrorMessage,
            onDismiss = { viewModel.setShowAuthDialog(false) },
            onLogin = { id, pw -> viewModel.login(id, pw) },
            onRegister = { u, e, p, fn, org, r -> viewModel.register(u, e, p, fn, org, r) },
            onLogout = { viewModel.logout() }
        )
    }
}
