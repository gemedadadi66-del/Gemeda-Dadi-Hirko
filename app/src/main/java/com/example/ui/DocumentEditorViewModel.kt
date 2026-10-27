package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.EditedDocument
import com.example.data.model.User
import com.example.domain.processor.DocumentImageProcessor
import com.example.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

enum class EditorMode {
    SELECT_STAMP, // 1. Chaappaa Filadhu
    MOVE_STAMP,   // 2. Bakka Haaraa Kaayi
    EDIT_TEXT     // 3. Maqaa / Barreeffama Jijjiiri
}

enum class AppLanguage {
    OROMO,
    ENGLISH
}

data class EditorUiState(
    val language: AppLanguage = AppLanguage.OROMO,
    val currentBitmap: Bitmap? = null,
    val activeDocumentTitle: String = "Waraqaa Fakkeenyaa 1",
    val currentSourceUri: String = "",
    val mode: EditorMode = EditorMode.SELECT_STAMP,
    // Pixel coordinates on original bitmap
    val cropX: Float = 0f,
    val cropY: Float = 0f,
    val cropW: Float = 0f,
    val cropH: Float = 0f,
    // Destination coordinates on original bitmap
    val newX: Float = 0f,
    val newY: Float = 0f,
    val newW: Float = 150f,
    val newH: Float = 150f,
    val rotationDegrees: Float = 0f,
    val lockAspectRatio: Boolean = true,
    val matchPaperBackground: Boolean = true,
    val transparentStampBackground: Boolean = false,
    // Text editing & name replacement state
    val textTargetX: Float = 100f,
    val textTargetY: Float = 200f,
    val textTargetW: Float = 360f,
    val textTargetH: Float = 60f,
    val replacementText: String = "Gemedadadi Dadi",
    val textFontSize: Float = 34f,
    val textFontFamily: String = "serif", // "serif", "sans-serif", "monospace"
    val isTextBold: Boolean = true,
    val isTextItalic: Boolean = false,
    val textAlignment: String = "CENTER", // "CENTER", "LEFT", "RIGHT"
    val textInkColorHex: String = "#111827",
    val statusMessage: String = "Waraqaan fe'ameera. Chaappaa irra maawusiin/qubaan harkisaa.",
    val isProcessing: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val exportedPdfFile: File? = null,
    val showSuccessExportDialog: Boolean = false,
    val showSampleSheet: Boolean = false,
    val showAuthDialog: Boolean = false,
    val authErrorMessage: String? = null,
    val currentUser: User? = null,
    val savedDocuments: List<EditedDocument> = emptyList()
)

class DocumentEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val documentDao = db.documentDao()
    private val userDao = db.userDao()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = mutableListOf<Bitmap>()
    private val redoStack = mutableListOf<Bitmap>()
    private var documentCollectionJob: Job? = null

    init {
        // Initialize default user and document observer
        viewModelScope.launch {
            val count = userDao.getUserCount()
            if (count == 0) {
                // Seed initial default official user
                val defaultUser = User(
                    username = "admin",
                    email = "gemedadadi66@gmail.com",
                    passwordHash = SecurityUtils.hashPassword("password123"),
                    fullName = "Gemedadadi Dadi",
                    organization = "Biiroo Bulchiinsaa & Galmeessaa",
                    roleTitle = "Gulaalaa Waraqaa Aadaa"
                )
                val id = userDao.registerUser(defaultUser)
                val loggedIn = defaultUser.copy(id = id)
                _uiState.update { it.copy(currentUser = loggedIn) }
                observeDocumentsForUser(id)
            } else {
                val adminUser = userDao.findUserByIdentifier("admin")
                if (adminUser != null) {
                    _uiState.update { it.copy(currentUser = adminUser) }
                    observeDocumentsForUser(adminUser.id)
                } else {
                    observeAllDocumentsFallback()
                }
            }
        }

        // Load initial default sample certificate
        loadSampleDocument(1)
    }

    private fun observeDocumentsForUser(userId: Long) {
        documentCollectionJob?.cancel()
        documentCollectionJob = viewModelScope.launch {
            documentDao.getDocumentsForUser(userId).collect { docs ->
                _uiState.update { it.copy(savedDocuments = docs) }
            }
        }
    }

    private fun observeAllDocumentsFallback() {
        documentCollectionJob?.cancel()
        documentCollectionJob = viewModelScope.launch {
            documentDao.getAllDocuments().collect { docs ->
                _uiState.update { it.copy(savedDocuments = docs) }
            }
        }
    }

    // --- Authentication Actions ---

    fun login(identifier: String, password: String) {
        viewModelScope.launch {
            val user = userDao.findUserByIdentifier(identifier)
            if (user == null) {
                _uiState.update {
                    it.copy(
                        authErrorMessage = if (it.language == AppLanguage.OROMO) {
                            "Fayyadamaan hin argamne! Maaloo galmaa'aa."
                        } else {
                            "User not found! Please register."
                        }
                    )
                }
                return@launch
            }

            if (!SecurityUtils.verifyPassword(password, user.passwordHash)) {
                _uiState.update {
                    it.copy(
                        authErrorMessage = if (it.language == AppLanguage.OROMO) {
                            "Jechi iccitii dogoggora! Irra deebiaa yaalaa."
                        } else {
                            "Incorrect password! Please try again."
                        }
                    )
                }
                return@launch
            }

            // Login successful
            _uiState.update {
                it.copy(
                    currentUser = user,
                    authErrorMessage = null,
                    showAuthDialog = false,
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Baga nagaan dhufte, ${user.fullName}!"
                    } else {
                        "Welcome back, ${user.fullName}!"
                    }
                )
            }
            observeDocumentsForUser(user.id)
        }
    }

    fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        organization: String,
        role: String
    ) {
        viewModelScope.launch {
            // Check if username or email already exists
            val existing = userDao.findUserByIdentifier(username) ?: userDao.findUserByIdentifier(email)
            if (existing != null) {
                _uiState.update {
                    it.copy(
                        authErrorMessage = if (it.language == AppLanguage.OROMO) {
                            "Username ykn Email kun duraan galmaa'eera!"
                        } else {
                            "Username or Email already registered!"
                        }
                    )
                }
                return@launch
            }

            val newUser = User(
                username = username,
                email = email,
                passwordHash = SecurityUtils.hashPassword(password),
                fullName = fullName.ifEmpty { username },
                organization = organization.ifEmpty { "Oromia Gov" },
                roleTitle = role.ifEmpty { "Gulaalaa Waraqaa" }
            )

            val newId = userDao.registerUser(newUser)
            val registeredUser = newUser.copy(id = newId)

            _uiState.update {
                it.copy(
                    currentUser = registeredUser,
                    authErrorMessage = null,
                    showAuthDialog = false,
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Galmeen milkaa'eera! Baga nagaan dhufte, ${registeredUser.fullName}."
                    } else {
                        "Account registered successfully! Welcome, ${registeredUser.fullName}."
                    }
                )
            }
            observeDocumentsForUser(newId)
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                currentUser = null,
                showAuthDialog = false,
                statusMessage = if (it.language == AppLanguage.OROMO) {
                    "Akkaawuntii irraa baatanittu."
                } else {
                    "Signed out successfully."
                }
            )
        }
        observeAllDocumentsFallback()
    }

    fun setShowAuthDialog(show: Boolean) {
        _uiState.update { it.copy(showAuthDialog = show, authErrorMessage = null) }
    }

    // --- Language & Editor Modes ---

    fun toggleLanguage() {
        val nextLang = if (_uiState.value.language == AppLanguage.OROMO) AppLanguage.ENGLISH else AppLanguage.OROMO
        _uiState.update {
            it.copy(
                language = nextLang,
                statusMessage = getLocalizedStatus(nextLang, it.mode, it.cropW > 0)
            )
        }
    }

    fun setMode(mode: EditorMode) {
        _uiState.update { state ->
            val lang = state.language
            val msg = when (mode) {
                EditorMode.MOVE_STAMP -> {
                    if (lang == AppLanguage.OROMO) {
                        "Bakka haaraa irratti quba qabaa ykn harkisaa."
                    } else {
                        "Tap or drag to position stamp at the new location."
                    }
                }
                EditorMode.EDIT_TEXT -> {
                    if (lang == AppLanguage.OROMO) {
                        "Qubaan saanduqa maqaa harkisaa, maqaa haaraa barreessaa 'Waraqaa Irratti Barreessi' tuqaa."
                    } else {
                        "Drag text box over old name, type replacement, and tap 'Apply Text'."
                    }
                }
                else -> {
                    if (lang == AppLanguage.OROMO) {
                        "Chaappaa waraqaa irraa filachuuf qubaan qabadhuutii harkisi."
                    } else {
                        "Drag selection box over the stamp/seal to crop."
                    }
                }
            }
            state.copy(mode = mode, statusMessage = msg)
        }
    }

    fun updateCrop(x: Float, y: Float, w: Float, h: Float) {
        val state = _uiState.value
        val bmp = state.currentBitmap ?: return
        val clampedX = x.coerceIn(0f, bmp.width.toFloat())
        val clampedY = y.coerceIn(0f, bmp.height.toFloat())
        val clampedW = w.coerceIn(10f, (bmp.width - clampedX).coerceAtLeast(10f))
        val clampedH = h.coerceIn(10f, (bmp.height - clampedY).coerceAtLeast(10f))

        _uiState.update { current ->
            current.copy(
                cropX = clampedX,
                cropY = clampedY,
                cropW = clampedW,
                cropH = clampedH,
                newW = if (current.newW == 150f && clampedW > 20f) clampedW else current.newW,
                newH = if (current.newH == 150f && clampedH > 20f) clampedH else current.newH,
                statusMessage = if (current.language == AppLanguage.OROMO) {
                    "Chaappaan filatameera (${clampedW.roundToInt()}x${clampedH.roundToInt()}px). Gara 'Bakka Haaraa Kaayi' jijjiiraa."
                } else {
                    "Stamp selected (${clampedW.roundToInt()}x${clampedH.roundToInt()}px). Switch to 'Place New Location'."
                }
            )
        }
    }

    fun updateNewPosition(x: Float, y: Float) {
        val state = _uiState.value
        val bmp = state.currentBitmap ?: return
        val clampedX = x.coerceIn(0f, (bmp.width - state.newW).coerceAtLeast(0f))
        val clampedY = y.coerceIn(0f, (bmp.height - state.newH).coerceAtLeast(0f))

        _uiState.update { current ->
            current.copy(
                newX = clampedX,
                newY = clampedY,
                statusMessage = if (current.language == AppLanguage.OROMO) {
                    "Bakki haaraan filatameera! Amma button 'Waraqaa Hojiirra Olchi' tuqaa."
                } else {
                    "New position set! Tap 'Apply Changes' to relocate stamp."
                }
            )
        }
    }

    fun setNewSize(w: Float, h: Float) {
        val state = _uiState.value
        val bmp = state.currentBitmap ?: return
        val maxW = bmp.width.toFloat()
        val maxH = bmp.height.toFloat()

        val clampedW = w.coerceIn(20f, maxW)
        val finalH = if (state.lockAspectRatio && state.cropW > 0f) {
            (clampedW * (state.cropH / state.cropW)).coerceIn(20f, maxH)
        } else {
            h.coerceIn(20f, maxH)
        }

        _uiState.update { current ->
            current.copy(
                newW = clampedW,
                newH = finalH
            )
        }
    }

    fun setRotation(degrees: Float) {
        _uiState.update { it.copy(rotationDegrees = degrees) }
    }

    fun rotateBy(delta: Float) {
        _uiState.update {
            var newRot = it.rotationDegrees + delta
            while (newRot > 180f) newRot -= 360f
            while (newRot < -180f) newRot += 360f
            it.copy(rotationDegrees = newRot)
        }
    }

    fun setLockAspectRatio(lock: Boolean) {
        _uiState.update { it.copy(lockAspectRatio = lock) }
    }

    fun setMatchPaperBackground(match: Boolean) {
        _uiState.update { it.copy(matchPaperBackground = match) }
    }

    fun setTransparentStampBackground(transparent: Boolean) {
        _uiState.update { it.copy(transparentStampBackground = transparent) }
    }

    fun applyEdit() {
        val state = _uiState.value
        val bmp = state.currentBitmap ?: return
        if (state.cropW <= 5 || state.cropH <= 5) {
            _uiState.update {
                it.copy(
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Dura chaappaa waraqaa irraa filadhaa!"
                    } else {
                        "Please select a stamp/seal first!"
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val edited = withContext(Dispatchers.Default) {
                DocumentImageProcessor.cropAndRelocateStamp(
                    sourceBitmap = bmp,
                    cropX = state.cropX.roundToInt(),
                    cropY = state.cropY.roundToInt(),
                    cropW = state.cropW.roundToInt(),
                    cropH = state.cropH.roundToInt(),
                    newX = state.newX.roundToInt(),
                    newY = state.newY.roundToInt(),
                    newW = state.newW.roundToInt(),
                    newH = state.newH.roundToInt(),
                    rotationDegrees = state.rotationDegrees,
                    matchPaperBackground = state.matchPaperBackground,
                    transparentStampBackground = state.transparentStampBackground
                )
            }

            // Save to undo stack
            undoStack.add(bmp)
            redoStack.clear()

            _uiState.update {
                it.copy(
                    currentBitmap = edited,
                    isProcessing = false,
                    canUndo = true,
                    canRedo = false,
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Waraqaan milkiin sirraa'eera! Amma '📥 PDF Download Godhadhu' tuqi."
                    } else {
                        "Document successfully edited! Now tap 'Download PDF'."
                    }
                )
            }
        }
    }

    fun updateTextTarget(x: Float, y: Float, w: Float, h: Float) {
        val state = _uiState.value
        val bmp = state.currentBitmap ?: return
        val clampedX = x.coerceIn(0f, bmp.width.toFloat())
        val clampedY = y.coerceIn(0f, bmp.height.toFloat())
        val clampedW = w.coerceIn(20f, (bmp.width - clampedX).coerceAtLeast(20f))
        val clampedH = h.coerceIn(15f, (bmp.height - clampedY).coerceAtLeast(15f))
        _uiState.update {
            it.copy(
                textTargetX = clampedX,
                textTargetY = clampedY,
                textTargetW = clampedW,
                textTargetH = clampedH
            )
        }
    }

    fun setReplacementText(text: String) {
        _uiState.update { it.copy(replacementText = text) }
    }

    fun setTextFontSize(size: Float) {
        _uiState.update { it.copy(textFontSize = size) }
    }

    fun setTextFontFamily(family: String) {
        _uiState.update { it.copy(textFontFamily = family) }
    }

    fun setTextBold(bold: Boolean) {
        _uiState.update { it.copy(isTextBold = bold) }
    }

    fun setTextItalic(italic: Boolean) {
        _uiState.update { it.copy(isTextItalic = italic) }
    }

    fun setTextAlignment(alignment: String) {
        _uiState.update { it.copy(textAlignment = alignment) }
    }

    fun setTextInkColorHex(colorHex: String) {
        _uiState.update { it.copy(textInkColorHex = colorHex) }
    }

    fun applyTextReplacement() {
        val state = _uiState.value
        val bmp = state.currentBitmap ?: return
        if (state.replacementText.isBlank()) {
            _uiState.update {
                it.copy(
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Maaloo maqaa ykn barreeffama haaraa galchaa!"
                    } else {
                        "Please enter replacement text first!"
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val parsedColor = try {
                android.graphics.Color.parseColor(state.textInkColorHex)
            } catch (_: Exception) {
                android.graphics.Color.BLACK
            }

            val align = when (state.textAlignment) {
                "LEFT" -> android.graphics.Paint.Align.LEFT
                "RIGHT" -> android.graphics.Paint.Align.RIGHT
                else -> android.graphics.Paint.Align.CENTER
            }

            val edited = withContext(Dispatchers.Default) {
                DocumentImageProcessor.replaceTextOnDocument(
                    sourceBitmap = bmp,
                    rectX = state.textTargetX.roundToInt(),
                    rectY = state.textTargetY.roundToInt(),
                    rectW = state.textTargetW.roundToInt(),
                    rectH = state.textTargetH.roundToInt(),
                    newText = state.replacementText,
                    fontSizePx = state.textFontSize,
                    fontFamily = state.textFontFamily,
                    isBold = state.isTextBold,
                    isItalic = state.isTextItalic,
                    textColor = parsedColor,
                    alignment = align,
                    matchPaperBackground = state.matchPaperBackground
                )
            }

            undoStack.add(bmp)
            redoStack.clear()

            _uiState.update {
                it.copy(
                    currentBitmap = edited,
                    isProcessing = false,
                    canUndo = true,
                    canRedo = false,
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Maqaan/barreeffamni haaraan milkiin barreeffameera! '📥 PDF Download Godhadhu' tuqaa."
                    } else {
                        "New text written successfully! Now tap 'Download PDF'."
                    }
                )
            }
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.size - 1)
            val current = _uiState.value.currentBitmap
            if (current != null) {
                redoStack.add(current)
            }
            _uiState.update {
                it.copy(
                    currentBitmap = prev,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = true,
                    statusMessage = if (it.language == AppLanguage.OROMO) "Tarkaanfiin duraatti deebi'eera." else "Action undone."
                )
            }
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            val current = _uiState.value.currentBitmap
            if (current != null) {
                undoStack.add(current)
            }
            _uiState.update {
                it.copy(
                    currentBitmap = next,
                    canUndo = true,
                    canRedo = redoStack.isNotEmpty(),
                    statusMessage = if (it.language == AppLanguage.OROMO) "Tarkaanfiin deebifameera." else "Action redone."
                )
            }
        }
    }

    fun exportPdf() {
        val bmp = _uiState.value.currentBitmap ?: return
        val currentUserId = _uiState.value.currentUser?.id ?: 1L

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }

            val pdfFile = DocumentImageProcessor.exportToPdf(
                context = getApplication(),
                bitmap = bmp,
                filenamePrefix = "waraqaa_gulaalame"
            )

            // Also save image copy for record
            val imageFile = DocumentImageProcessor.saveBitmapToFile(
                context = getApplication(),
                bitmap = bmp,
                filenamePrefix = "waraqaa_edited"
            )

            // Save record to Room Database associated with the logged-in user
            val docRecord = EditedDocument(
                userId = currentUserId,
                title = _uiState.value.activeDocumentTitle,
                sourceUri = _uiState.value.currentSourceUri,
                editedImagePath = imageFile.absolutePath,
                pdfPath = pdfFile.absolutePath,
                dateModified = System.currentTimeMillis(),
                stampWidth = _uiState.value.newW.roundToInt(),
                stampHeight = _uiState.value.newH.roundToInt()
            )
            documentDao.insertDocument(docRecord)

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    exportedPdfFile = pdfFile,
                    showSuccessExportDialog = true,
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "PDF uumameera: ${pdfFile.name}"
                    } else {
                        "PDF Generated: ${pdfFile.name}"
                    }
                )
            }
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(showSuccessExportDialog = false) }
    }

    fun setShowSampleSheet(show: Boolean) {
        _uiState.update { it.copy(showSampleSheet = show) }
    }

    fun loadSampleDocument(sampleIndex: Int) {
        val context = getApplication<Application>()
        val (resId, title) = if (sampleIndex == 1) {
            Pair(R.drawable.sample_cert, "Waraqaa Ragaa (Certificate)")
        } else {
            Pair(R.drawable.sample_letter, "Xalayaa Mootummaa (Official Letter)")
        }

        viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                DocumentImageProcessor.loadBitmapFromResource(context, resId)
            }

            val initialCropX: Float
            val initialCropY: Float
            val initialCropW: Float
            val initialCropH: Float
            val initialNewX: Float
            val initialNewY: Float
            val initialTextX: Float
            val initialTextY: Float
            val initialTextW: Float
            val initialTextH: Float
            val sampleDefaultName: String

            if (sampleIndex == 1) {
                initialCropX = (bitmap.width * 0.65f)
                initialCropY = (bitmap.height * 0.72f)
                initialCropW = (bitmap.width * 0.26f)
                initialCropH = (bitmap.width * 0.26f)
                initialNewX = (bitmap.width * 0.12f)
                initialNewY = (bitmap.height * 0.72f)
                // Center line on Certificate: "[Recipient's Full Name]"
                initialTextX = (bitmap.width * 0.14f)
                initialTextY = (bitmap.height * 0.435f)
                initialTextW = (bitmap.width * 0.72f)
                initialTextH = (bitmap.height * 0.08f)
                sampleDefaultName = "Gemedadadi Dadi"
            } else {
                initialCropX = (bitmap.width * 0.44f)
                initialCropY = (bitmap.height * 0.70f)
                initialCropW = (bitmap.width * 0.28f)
                initialCropH = (bitmap.width * 0.28f)
                initialNewX = (bitmap.width * 0.10f)
                initialNewY = (bitmap.height * 0.16f)
                // Salutation / Doctor name on Official Letter
                initialTextX = (bitmap.width * 0.10f)
                initialTextY = (bitmap.height * 0.27f)
                initialTextW = (bitmap.width * 0.60f)
                initialTextH = (bitmap.height * 0.06f)
                sampleDefaultName = "Dr. Alistair Finch"
            }

            undoStack.clear()
            redoStack.clear()

            _uiState.update {
                it.copy(
                    currentBitmap = bitmap,
                    activeDocumentTitle = title,
                    currentSourceUri = "sample_$sampleIndex",
                    mode = EditorMode.SELECT_STAMP,
                    cropX = initialCropX,
                    cropY = initialCropY,
                    cropW = initialCropW,
                    cropH = initialCropH,
                    newX = initialNewX,
                    newY = initialNewY,
                    newW = initialCropW,
                    newH = initialCropH,
                    rotationDegrees = 0f,
                    textTargetX = initialTextX,
                    textTargetY = initialTextY,
                    textTargetW = initialTextW,
                    textTargetH = initialTextH,
                    replacementText = sampleDefaultName,
                    canUndo = false,
                    canRedo = false,
                    showSampleSheet = false,
                    statusMessage = if (it.language == AppLanguage.OROMO) {
                        "Waraqaan fakkeenyaa fe'ameera. Chaappaa filadhu, bakka jijjiiri, ykn maqaa gulaali."
                    } else {
                        "Sample document loaded. Select stamp, relocate, or edit text."
                    }
                )
            }
        }
    }

    fun loadBitmapFromUri(uri: Uri) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                            inMutable = true
                        }
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                undoStack.clear()
                redoStack.clear()

                val defW = (bitmap.width * 0.25f).coerceAtLeast(50f)
                val defH = defW

                _uiState.update {
                    it.copy(
                        currentBitmap = bitmap,
                        activeDocumentTitle = "Waraqaa Ol-fe'ame",
                        currentSourceUri = uri.toString(),
                        mode = EditorMode.SELECT_STAMP,
                        cropX = (bitmap.width * 0.6f).coerceAtLeast(0f),
                        cropY = (bitmap.height * 0.7f).coerceAtLeast(0f),
                        cropW = defW,
                        cropH = defH,
                        newX = (bitmap.width * 0.1f),
                        newY = (bitmap.height * 0.7f),
                        newW = defW,
                        newH = defH,
                        rotationDegrees = 0f,
                        textTargetX = (bitmap.width * 0.15f),
                        textTargetY = (bitmap.height * 0.45f),
                        textTargetW = (bitmap.width * 0.70f),
                        textTargetH = (bitmap.height * 0.06f).coerceAtLeast(40f),
                        isProcessing = false,
                        canUndo = false,
                        canRedo = false,
                        statusMessage = if (it.language == AppLanguage.OROMO) {
                            "Waraqaan milkiin fe'ameera! Chaappaa irra maawusiin/qubaan harkisaa."
                        } else {
                            "Document loaded! Drag over the stamp to crop."
                        }
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        statusMessage = if (it.language == AppLanguage.OROMO) "Dogoggora: Waraqaan hin argamne" else "Error: Could not load document"
                    )
                }
            }
        }
    }

    fun deleteDocumentRecord(doc: EditedDocument) {
        viewModelScope.launch {
            documentDao.deleteDocument(doc)
            try {
                File(doc.editedImagePath).delete()
                doc.pdfPath?.let { File(it).delete() }
            } catch (_: Exception) {}
        }
    }

    private fun getLocalizedStatus(lang: AppLanguage, mode: EditorMode, hasSelection: Boolean): String {
        return if (lang == AppLanguage.OROMO) {
            when {
                mode == EditorMode.MOVE_STAMP -> "Bakka haaraa irratti quba qabaa ykn harkisaa."
                hasSelection -> "Chaappaan filatameera. Gara 'Bakka Haaraa Kaayi' jijjiiraa."
                else -> "Waraqaan fe'ameera. Chaappaa irra maawusiin/qubaan harkisaa."
            }
        } else {
            when {
                mode == EditorMode.MOVE_STAMP -> "Tap or drag to position stamp at the new location."
                hasSelection -> "Stamp selected. Switch to 'Place New Location'."
                else -> "Document loaded. Drag selection box over the stamp to crop."
            }
        }
    }
}
