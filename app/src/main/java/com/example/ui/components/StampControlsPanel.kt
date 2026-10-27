package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppLanguage
import com.example.ui.EditorMode
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.EmeraldSuccessDark
import com.example.ui.theme.SealCrimson
import com.example.ui.theme.SkyAccent
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StampControlsPanel(
    mode: EditorMode,
    language: AppLanguage,
    newW: Float,
    newH: Float,
    rotationDegrees: Float = 0f,
    lockAspectRatio: Boolean,
    matchPaperBackground: Boolean,
    transparentStampBackground: Boolean,
    // Text editing parameters
    replacementText: String = "",
    textFontSize: Float = 34f,
    textFontFamily: String = "serif",
    isTextBold: Boolean = true,
    isTextItalic: Boolean = false,
    textAlignment: String = "CENTER",
    textInkColorHex: String = "#111827",
    isProcessing: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onModeChange: (EditorMode) -> Unit,
    onSizeChange: (Float, Float) -> Unit,
    onRotationChange: (Float) -> Unit,
    onRotateBy: (Float) -> Unit,
    onLockAspectRatioToggle: (Boolean) -> Unit,
    onMatchPaperToggle: (Boolean) -> Unit,
    onTransparentStampToggle: (Boolean) -> Unit,
    onApplyEdit: () -> Unit,
    // Text editing callbacks
    onReplacementTextChange: (String) -> Unit = {},
    onFontSizeChange: (Float) -> Unit = {},
    onFontFamilyChange: (String) -> Unit = {},
    onBoldToggle: (Boolean) -> Unit = {},
    onItalicToggle: (Boolean) -> Unit = {},
    onAlignmentChange: (String) -> Unit = {},
    onInkColorChange: (String) -> Unit = {},
    onApplyTextReplacement: () -> Unit = {},
    onExportPdf: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAdvancedOptions by remember { mutableStateOf(false) }

    val inkColors = listOf(
        Pair("#111827", "Gurraacha (Black)"),
        Pair("#1E3A8A", "Cuquliisa (Navy)"),
        Pair("#991B1B", "Diimaa (Crimson)"),
        Pair("#92400E", "Warqee (Gold)"),
        Pair("#065F46", "Magaariisa (Green)")
    )

    Card(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("stamp_controls_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // 1. Primary Mode Selection Row (3 tabs: Crop, Relocate, Edit Text) + Undo/Redo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Chaappaa Filadhu
                Button(
                    onClick = { onModeChange(EditorMode.SELECT_STAMP) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == EditorMode.SELECT_STAMP) SealCrimson else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (mode == EditorMode.SELECT_STAMP) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_select_stamp")
                ) {
                    Icon(
                        imageVector = Icons.Default.Crop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.OROMO) "1. Filadhu" else "1. Select",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Tab 2: Bakka Haaraa Kaayi
                Button(
                    onClick = { onModeChange(EditorMode.MOVE_STAMP) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == EditorMode.MOVE_STAMP) EmeraldSuccess else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (mode == EditorMode.MOVE_STAMP) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("btn_move_stamp")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenWith,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.OROMO) "2. Kaayi" else "2. Place",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Tab 3: Maqaa / Barreeffama Jijjiiri
                Button(
                    onClick = { onModeChange(EditorMode.EDIT_TEXT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == EditorMode.EDIT_TEXT) SkyAccent else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (mode == EditorMode.EDIT_TEXT) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .testTag("btn_edit_text_mode")
                ) {
                    Icon(
                        imageVector = Icons.Default.TextFields,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.OROMO) "3. Maqaa" else "3. Edit Name",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Undo & Redo buttons
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_undo")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("btn_redo")
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // CONDITIONAL PANEL CONTENT: TEXT vs STAMP
            // ==========================================
            if (mode == EditorMode.EDIT_TEXT) {
                // --- TEXT REPLACEMENT CONTROLS ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("text_controls_section")
                ) {
                    // Input TextField for the new name / text
                    OutlinedTextField(
                        value = replacementText,
                        onValueChange = onReplacementTextChange,
                        label = {
                            Text(
                                if (language == AppLanguage.OROMO) "Maqaa / Barreeffama Haaraa Galchaa" else "Enter New Name / Text",
                                fontSize = 12.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_replacement_text")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Font Family Selector Chips (Serif, Sans-Serif, Monospace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.OROMO) "Font:" else "Font:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        FilterChip(
                            selected = textFontFamily.lowercase() == "serif",
                            onClick = { onFontFamilyChange("serif") },
                            label = { Text("Serif (Aadaa)", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("chip_font_serif")
                        )

                        FilterChip(
                            selected = textFontFamily.lowercase() == "sans-serif",
                            onClick = { onFontFamilyChange("sans-serif") },
                            label = { Text("Sans", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("chip_font_sans")
                        )

                        FilterChip(
                            selected = textFontFamily.lowercase() == "monospace",
                            onClick = { onFontFamilyChange("monospace") },
                            label = { Text("Mono", fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("chip_font_mono")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Font Size Slider with direct display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.OROMO) {
                                "Guddina (Size): ${textFontSize.roundToInt()}px"
                            } else {
                                "Font Size: ${textFontSize.roundToInt()}px"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { onFontSizeChange((textFontSize - 2f).coerceAtLeast(14f)) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .size(width = 36.dp, height = 28.dp)
                                    .testTag("btn_size_minus")
                            ) {
                                Text("-", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedButton(
                                onClick = { onFontSizeChange((textFontSize + 2f).coerceAtMost(80f)) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .size(width = 36.dp, height = 28.dp)
                                    .testTag("btn_size_plus")
                            ) {
                                Text("+", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Slider(
                        value = textFontSize,
                        onValueChange = onFontSizeChange,
                        valueRange = 14f..80f,
                        colors = SliderDefaults.colors(
                            thumbColor = SkyAccent,
                            activeTrackColor = SkyAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("slider_text_font_size")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Style Toggles: Bold, Italic, Alignment, and Ink Color
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bold & Italic buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onBoldToggle(!isTextBold) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (isTextBold) SkyAccent.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isTextBold) SkyAccent else Color.LightGray,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("toggle_bold")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatBold,
                                    contentDescription = "Bold",
                                    tint = if (isTextBold) SkyAccent else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onItalicToggle(!isTextItalic) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (isTextItalic) SkyAccent.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isTextItalic) SkyAccent else Color.LightGray,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("toggle_italic")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatItalic,
                                    contentDescription = "Italic",
                                    tint = if (isTextItalic) SkyAccent else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Alignment (Left, Center, Right)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onAlignmentChange("LEFT") },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (textAlignment == "LEFT") SkyAccent.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("align_left")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatAlignLeft,
                                    contentDescription = "Left",
                                    tint = if (textAlignment == "LEFT") SkyAccent else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onAlignmentChange("CENTER") },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (textAlignment == "CENTER") SkyAccent.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("align_center")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatAlignCenter,
                                    contentDescription = "Center",
                                    tint = if (textAlignment == "CENTER") SkyAccent else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onAlignmentChange("RIGHT") },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(
                                        if (textAlignment == "RIGHT") SkyAccent.copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .testTag("align_right")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatAlignRight,
                                    contentDescription = "Right",
                                    tint = if (textAlignment == "RIGHT") SkyAccent else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Ink Color Swatches
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            inkColors.forEach { (hex, _) ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = textInkColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(color, CircleShape)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                            shape = CircleShape
                                        )
                                        .clickable { onInkColorChange(hex) }
                                        .testTag("color_$hex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Text Action Buttons: Apply Text & Download PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onApplyTextReplacement,
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkyAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("btn_apply_text")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.OROMO) "Waraqaa Irratti Barreessi" else "Apply Text",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = onExportPdf,
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("btn_download_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.OROMO) "📥 PDF Download" else "📥 Download PDF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            } else {
                // --- STAMP RELOCATION & ROTATION CONTROLS ---
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Size Controls ("Size Haaraa: W / H")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.OROMO) "Size Haaraa (Chaappaa):" else "Stamp Size (Target):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onLockAspectRatioToggle(!lockAspectRatio) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (lockAspectRatio) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Lock Ratio",
                                    tint = if (lockAspectRatio) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "W: ${newW.roundToInt()} | H: ${newH.roundToInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Slider(
                        value = newW,
                        onValueChange = { onSizeChange(it, newH) },
                        valueRange = 40f..500f,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldSuccess,
                            activeTrackColor = EmeraldSuccess
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("slider_width")
                    )

                    // Quick presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(80f, 120f, 160f, 220f).forEach { size ->
                            FilledTonalButton(
                                onClick = { onSizeChange(size, size) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                            ) {
                                Text("${size.roundToInt()}", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Rotation Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.OROMO) "Naannessi:" else "Rotate:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${rotationDegrees.roundToInt()}°",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SkyAccent
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onRotateBy(-90f) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_rotate_ccw_90")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateLeft,
                                    contentDescription = "Rotate -90°",
                                    tint = SkyAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRotateBy(90f) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_rotate_cw_90")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RotateRight,
                                    contentDescription = "Rotate +90°",
                                    tint = SkyAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = { onRotationChange(0f) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_reset_rotation")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Rotation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Slider(
                        value = rotationDegrees,
                        onValueChange = onRotationChange,
                        valueRange = -180f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = SkyAccent,
                            activeTrackColor = SkyAccent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("slider_rotation")
                    )

                    // Toggle Advanced Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showAdvancedOptions = !showAdvancedOptions },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("btn_toggle_advanced")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAdvancedOptions) {
                                    if (language == AppLanguage.OROMO) "Filannoowwan Dhoksi" else "Hide Options"
                                } else {
                                    if (language == AppLanguage.OROMO) "Filannoowwan Biroo" else "More Options"
                                },
                                fontSize = 10.sp
                            )
                        }
                    }

                    AnimatedVisibility(visible = showAdvancedOptions) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Halluu Waraqaa Walsimsiisi" else "Auto-match Paper Color",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Bakka duraanii halluu waraqaatiin uwwisa" else "Seamlessly blends the cut area with paper tone",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = matchPaperBackground,
                                    onCheckedChange = onMatchPaperToggle,
                                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Chaappaa Iftoomina Qabu" else "Transparent Stamp Ink",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (language == AppLanguage.OROMO) "Waraqaa adii chaappaa irraa dhabamsiisa" else "Removes white paper from seal so ink stamps cleanly",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = transparentStampBackground,
                                    onCheckedChange = onTransparentStampToggle,
                                    colors = SwitchDefaults.colors(checkedThumbColor = SkyAccent)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Stamp Action Buttons: Apply Relocation & Download PDF
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onApplyEdit,
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SkyAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                                .testTag("btn_apply_edit")
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.OROMO) "Waraqaa Hojiirra Olchi" else "Apply Changes",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = onExportPdf,
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldSuccess,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("btn_download_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.OROMO) "📥 PDF Download" else "📥 Download PDF",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
