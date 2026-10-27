package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.ui.AppLanguage
import com.example.ui.EditorMode
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SealCrimson
import com.example.ui.theme.SkyAccent
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class DragTarget {
    NONE,
    NEW_SELECTION,
    MOVE_CROP_BOX,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    MOVE_NEW_TARGET,
    MOVE_TEXT_TARGET,
    TEXT_TOP_LEFT,
    TEXT_TOP_RIGHT,
    TEXT_BOTTOM_LEFT,
    TEXT_BOTTOM_RIGHT
}

@Composable
fun DocumentCanvasView(
    bitmap: Bitmap?,
    mode: EditorMode,
    language: AppLanguage,
    cropX: Float,
    cropY: Float,
    cropW: Float,
    cropH: Float,
    newX: Float,
    newY: Float,
    newW: Float,
    newH: Float,
    rotationDegrees: Float = 0f,
    // Text editing parameters
    textTargetX: Float = 0f,
    textTargetY: Float = 0f,
    textTargetW: Float = 300f,
    textTargetH: Float = 60f,
    replacementText: String = "",
    textFontSize: Float = 34f,
    textFontFamily: String = "serif",
    isTextBold: Boolean = true,
    isTextItalic: Boolean = false,
    textAlignment: String = "CENTER",
    textInkColorHex: String = "#111827",
    onCropChange: (x: Float, y: Float, w: Float, h: Float) -> Unit,
    onNewPositionChange: (x: Float, y: Float) -> Unit,
    onTextTargetChange: (x: Float, y: Float, w: Float, h: Float) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    if (bitmap == null) return

    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    // Pre-extract cropped stamp bitmap for live preview in MOVE mode
    val stampPreviewBitmap = remember(bitmap, cropX, cropY, cropW, cropH) {
        try {
            if (cropW > 10f && cropH > 10f) {
                val clampedX = cropX.toInt().coerceIn(0, bitmap.width - 1)
                val clampedY = cropY.toInt().coerceIn(0, bitmap.height - 1)
                val clampedW = cropW.toInt().coerceIn(1, bitmap.width - clampedX)
                val clampedH = cropH.toInt().coerceIn(1, bitmap.height - clampedY)
                Bitmap.createBitmap(bitmap, clampedX, clampedY, clampedW, clampedH).asImageBitmap()
            } else null
        } catch (_: Exception) {
            null
        }
    }

    var dragTarget by remember { mutableStateOf(DragTarget.NONE) }
    var dragStartScreenPos by remember { mutableStateOf(Offset.Zero) }
    var dragStartCropX by remember { mutableStateOf(cropX) }
    var dragStartCropY by remember { mutableStateOf(cropY) }
    var dragStartCropW by remember { mutableStateOf(cropW) }
    var dragStartCropH by remember { mutableStateOf(cropH) }

    var dragStartNewX by remember { mutableStateOf(newX) }
    var dragStartNewY by remember { mutableStateOf(newY) }

    var dragStartTextX by remember { mutableStateOf(textTargetX) }
    var dragStartTextY by remember { mutableStateOf(textTargetY) }
    var dragStartTextW by remember { mutableStateOf(textTargetW) }
    var dragStartTextH by remember { mutableStateOf(textTargetH) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("document_canvas_box")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("document_canvas")
                .pointerInput(mode, bitmap, cropX, cropY, cropW, cropH, newX, newY, newW, newH, textTargetX, textTargetY, textTargetW, textTargetH) {
                    detectTapGestures { tapOffset ->
                        val scale = min(
                            size.width.toFloat() / bitmap.width,
                            size.height.toFloat() / bitmap.height
                        )
                        val offsetX = (size.width.toFloat() - bitmap.width * scale) / 2f
                        val offsetY = (size.height.toFloat() - bitmap.height * scale) / 2f

                        val bmpX = (tapOffset.x - offsetX) / scale
                        val bmpY = (tapOffset.y - offsetY) / scale

                        when (mode) {
                            EditorMode.MOVE_STAMP -> {
                                val targetX = (bmpX - newW / 2f).coerceIn(0f, bitmap.width - newW)
                                val targetY = (bmpY - newH / 2f).coerceIn(0f, bitmap.height - newH)
                                onNewPositionChange(targetX, targetY)
                            }
                            EditorMode.EDIT_TEXT -> {
                                val targetX = (bmpX - textTargetW / 2f).coerceIn(0f, bitmap.width - textTargetW)
                                val targetY = (bmpY - textTargetH / 2f).coerceIn(0f, bitmap.height - textTargetH)
                                onTextTargetChange(targetX, targetY, textTargetW, textTargetH)
                            }
                            else -> { /* No tap action in select stamp mode */ }
                        }
                    }
                }
                .pointerInput(mode, bitmap, cropX, cropY, cropW, cropH, newX, newY, newW, newH, textTargetX, textTargetY, textTargetW, textTargetH) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            val scale = min(
                                size.width.toFloat() / bitmap.width,
                                size.height.toFloat() / bitmap.height
                            )
                            val offsetX = (size.width.toFloat() - bitmap.width * scale) / 2f
                            val offsetY = (size.height.toFloat() - bitmap.height * scale) / 2f

                            val bmpX = (startOffset.x - offsetX) / scale
                            val bmpY = (startOffset.y - offsetY) / scale

                            dragStartScreenPos = startOffset
                            dragStartCropX = cropX
                            dragStartCropY = cropY
                            dragStartCropW = cropW
                            dragStartCropH = cropH
                            dragStartNewX = newX
                            dragStartNewY = newY
                            dragStartTextX = textTargetX
                            dragStartTextY = textTargetY
                            dragStartTextW = textTargetW
                            dragStartTextH = textTargetH

                            when (mode) {
                                EditorMode.SELECT_STAMP -> {
                                    val handleRadius = 45f / scale
                                    val cropRight = cropX + cropW
                                    val cropBottom = cropY + cropH

                                    dragTarget = when {
                                        distance(bmpX, bmpY, cropX, cropY) < handleRadius -> DragTarget.TOP_LEFT
                                        distance(bmpX, bmpY, cropRight, cropY) < handleRadius -> DragTarget.TOP_RIGHT
                                        distance(bmpX, bmpY, cropX, cropBottom) < handleRadius -> DragTarget.BOTTOM_LEFT
                                        distance(bmpX, bmpY, cropRight, cropBottom) < handleRadius -> DragTarget.BOTTOM_RIGHT
                                        bmpX in cropX..cropRight && bmpY in cropY..cropBottom -> DragTarget.MOVE_CROP_BOX
                                        else -> {
                                            dragStartCropX = bmpX
                                            dragStartCropY = bmpY
                                            dragStartCropW = 10f
                                            dragStartCropH = 10f
                                            DragTarget.NEW_SELECTION
                                        }
                                    }
                                }
                                EditorMode.MOVE_STAMP -> {
                                    val targetRight = newX + newW
                                    val targetBottom = newY + newH
                                    dragTarget = DragTarget.MOVE_NEW_TARGET
                                    if (bmpX !in newX..targetRight || bmpY !in newY..targetBottom) {
                                        val startX = (bmpX - newW / 2f).coerceIn(0f, bitmap.width - newW)
                                        val startY = (bmpY - newH / 2f).coerceIn(0f, bitmap.height - newH)
                                        dragStartNewX = startX
                                        dragStartNewY = startY
                                        onNewPositionChange(startX, startY)
                                    }
                                }
                                EditorMode.EDIT_TEXT -> {
                                    val handleRadius = 45f / scale
                                    val textRight = textTargetX + textTargetW
                                    val textBottom = textTargetY + textTargetH

                                    dragTarget = when {
                                        distance(bmpX, bmpY, textTargetX, textTargetY) < handleRadius -> DragTarget.TEXT_TOP_LEFT
                                        distance(bmpX, bmpY, textRight, textTargetY) < handleRadius -> DragTarget.TEXT_TOP_RIGHT
                                        distance(bmpX, bmpY, textTargetX, textBottom) < handleRadius -> DragTarget.TEXT_BOTTOM_LEFT
                                        distance(bmpX, bmpY, textRight, textBottom) < handleRadius -> DragTarget.TEXT_BOTTOM_RIGHT
                                        bmpX in textTargetX..textRight && bmpY in textTargetY..textBottom -> DragTarget.MOVE_TEXT_TARGET
                                        else -> {
                                            // Relocate text target box center to start drag
                                            val startX = (bmpX - textTargetW / 2f).coerceIn(0f, bitmap.width - textTargetW)
                                            val startY = (bmpY - textTargetH / 2f).coerceIn(0f, bitmap.height - textTargetH)
                                            dragStartTextX = startX
                                            dragStartTextY = startY
                                            onTextTargetChange(startX, startY, textTargetW, textTargetH)
                                            DragTarget.MOVE_TEXT_TARGET
                                        }
                                    }
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val scale = min(
                                size.width.toFloat() / bitmap.width,
                                size.height.toFloat() / bitmap.height
                            )
                            val deltaX = (change.position.x - dragStartScreenPos.x) / scale
                            val deltaY = (change.position.y - dragStartScreenPos.y) / scale

                            when (mode) {
                                EditorMode.SELECT_STAMP -> {
                                    when (dragTarget) {
                                        DragTarget.NEW_SELECTION -> {
                                            val curBmpX = (change.position.x - (size.width.toFloat() - bitmap.width * scale) / 2f) / scale
                                            val curBmpY = (change.position.y - (size.height.toFloat() - bitmap.height * scale) / 2f) / scale
                                            val minX = min(dragStartCropX, curBmpX)
                                            val minY = min(dragStartCropY, curBmpY)
                                            val w = abs(curBmpX - dragStartCropX).coerceAtLeast(20f)
                                            val h = abs(curBmpY - dragStartCropY).coerceAtLeast(20f)
                                            onCropChange(minX, minY, w, h)
                                        }
                                        DragTarget.MOVE_CROP_BOX -> {
                                            val nextX = (dragStartCropX + deltaX).coerceIn(0f, bitmap.width - dragStartCropW)
                                            val nextY = (dragStartCropY + deltaY).coerceIn(0f, bitmap.height - dragStartCropH)
                                            onCropChange(nextX, nextY, dragStartCropW, dragStartCropH)
                                        }
                                        DragTarget.TOP_LEFT -> {
                                            val right = dragStartCropX + dragStartCropW
                                            val bottom = dragStartCropY + dragStartCropH
                                            val nextX = (dragStartCropX + deltaX).coerceIn(0f, right - 20f)
                                            val nextY = (dragStartCropY + deltaY).coerceIn(0f, bottom - 20f)
                                            onCropChange(nextX, nextY, right - nextX, bottom - nextY)
                                        }
                                        DragTarget.TOP_RIGHT -> {
                                            val bottom = dragStartCropY + dragStartCropH
                                            val nextW = (dragStartCropW + deltaX).coerceIn(20f, bitmap.width - dragStartCropX)
                                            val nextY = (dragStartCropY + deltaY).coerceIn(0f, bottom - 20f)
                                            onCropChange(dragStartCropX, nextY, nextW, bottom - nextY)
                                        }
                                        DragTarget.BOTTOM_LEFT -> {
                                            val right = dragStartCropX + dragStartCropW
                                            val nextX = (dragStartCropX + deltaX).coerceIn(0f, right - 20f)
                                            val nextH = (dragStartCropH + deltaY).coerceIn(20f, bitmap.height - dragStartCropY)
                                            onCropChange(nextX, dragStartCropY, right - nextX, nextH)
                                        }
                                        DragTarget.BOTTOM_RIGHT -> {
                                            val nextW = (dragStartCropW + deltaX).coerceIn(20f, bitmap.width - dragStartCropX)
                                            val nextH = (dragStartCropH + deltaY).coerceIn(20f, bitmap.height - dragStartCropY)
                                            onCropChange(dragStartCropX, dragStartCropY, nextW, nextH)
                                        }
                                        else -> {}
                                    }
                                }
                                EditorMode.MOVE_STAMP -> {
                                    val nextX = (dragStartNewX + deltaX).coerceIn(0f, bitmap.width - newW)
                                    val nextY = (dragStartNewY + deltaY).coerceIn(0f, bitmap.height - newH)
                                    onNewPositionChange(nextX, nextY)
                                }
                                EditorMode.EDIT_TEXT -> {
                                    when (dragTarget) {
                                        DragTarget.MOVE_TEXT_TARGET -> {
                                            val nextX = (dragStartTextX + deltaX).coerceIn(0f, bitmap.width - dragStartTextW)
                                            val nextY = (dragStartTextY + deltaY).coerceIn(0f, bitmap.height - dragStartTextH)
                                            onTextTargetChange(nextX, nextY, dragStartTextW, dragStartTextH)
                                        }
                                        DragTarget.TEXT_TOP_LEFT -> {
                                            val right = dragStartTextX + dragStartTextW
                                            val bottom = dragStartTextY + dragStartTextH
                                            val nextX = (dragStartTextX + deltaX).coerceIn(0f, right - 20f)
                                            val nextY = (dragStartTextY + deltaY).coerceIn(0f, bottom - 15f)
                                            onTextTargetChange(nextX, nextY, right - nextX, bottom - nextY)
                                        }
                                        DragTarget.TEXT_TOP_RIGHT -> {
                                            val bottom = dragStartTextY + dragStartTextH
                                            val nextW = (dragStartTextW + deltaX).coerceIn(20f, bitmap.width - dragStartTextX)
                                            val nextY = (dragStartTextY + deltaY).coerceIn(0f, bottom - 15f)
                                            onTextTargetChange(dragStartTextX, nextY, nextW, bottom - nextY)
                                        }
                                        DragTarget.TEXT_BOTTOM_LEFT -> {
                                            val right = dragStartTextX + dragStartTextW
                                            val nextX = (dragStartTextX + deltaX).coerceIn(0f, right - 20f)
                                            val nextH = (dragStartTextH + deltaY).coerceIn(15f, bitmap.height - dragStartTextY)
                                            onTextTargetChange(nextX, dragStartTextY, right - nextX, nextH)
                                        }
                                        DragTarget.TEXT_BOTTOM_RIGHT -> {
                                            val nextW = (dragStartTextW + deltaX).coerceIn(20f, bitmap.width - dragStartTextX)
                                            val nextH = (dragStartTextH + deltaY).coerceIn(15f, bitmap.height - dragStartTextY)
                                            onTextTargetChange(dragStartTextX, dragStartTextY, nextW, nextH)
                                        }
                                        else -> {}
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            dragTarget = DragTarget.NONE
                        },
                        onDragCancel = {
                            dragTarget = DragTarget.NONE
                        }
                    )
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            val scale = min(canvasW / bitmap.width, canvasH / bitmap.height)
            val offsetX = (canvasW - bitmap.width * scale) / 2f
            val offsetY = (canvasH - bitmap.height * scale) / 2f

            val scaledBmpW = (bitmap.width * scale).roundToInt()
            val scaledBmpH = (bitmap.height * scale).roundToInt()

            // 1. Draw base document image
            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
                dstSize = IntSize(scaledBmpW, scaledBmpH)
            )

            // Scaled coordinates for Crop box
            val sCropX = offsetX + cropX * scale
            val sCropY = offsetY + cropY * scale
            val sCropW = cropW * scale
            val sCropH = cropH * scale

            // Scaled coordinates for New Stamp target
            val sNewX = offsetX + newX * scale
            val sNewY = offsetY + newY * scale
            val sNewW = newW * scale
            val sNewH = newH * scale

            // Scaled coordinates for Text Target
            val sTextX = offsetX + textTargetX * scale
            val sTextY = offsetY + textTargetY * scale
            val sTextW = textTargetW * scale
            val sTextH = textTargetH * scale

            when (mode) {
                EditorMode.SELECT_STAMP -> {
                    // Dim area outside selection box
                    val cropRect = Rect(sCropX, sCropY, sCropX + sCropW, sCropY + sCropH)
                    drawDimmedMask(size, cropRect)

                    // Selection rectangle
                    drawRect(
                        color = SealCrimson,
                        topLeft = Offset(sCropX, sCropY),
                        size = Size(sCropW, sCropH),
                        style = Stroke(
                            width = 3.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )

                    // 4 Corner handles
                    val handleRadius = 12f
                    val corners = listOf(
                        Offset(sCropX, sCropY),
                        Offset(sCropX + sCropW, sCropY),
                        Offset(sCropX, sCropY + sCropH),
                        Offset(sCropX + sCropW, sCropY + sCropH)
                    )
                    for (corner in corners) {
                        drawCircle(color = SealCrimson, radius = handleRadius, center = corner)
                        drawCircle(color = Color.White, radius = handleRadius * 0.5f, center = corner)
                    }

                    // Grid lines inside crop (rule of thirds)
                    val thirdW = sCropW / 3f
                    val thirdH = sCropH / 3f
                    val gridColor = SealCrimson.copy(alpha = 0.4f)
                    drawLine(gridColor, Offset(sCropX + thirdW, sCropY), Offset(sCropX + thirdW, sCropY + sCropH), 1.5f)
                    drawLine(gridColor, Offset(sCropX + thirdW * 2, sCropY), Offset(sCropX + thirdW * 2, sCropY + sCropH), 1.5f)
                    drawLine(gridColor, Offset(sCropX, sCropY + thirdH), Offset(sCropX + sCropW, sCropY + thirdH), 1.5f)
                    drawLine(gridColor, Offset(sCropX, sCropY + thirdH * 2), Offset(sCropX + sCropW, sCropY + thirdH * 2), 1.5f)
                }

                EditorMode.MOVE_STAMP -> {
                    // Indicate original spot with a soft dotted grey box
                    drawRect(
                        color = Color.Gray.copy(alpha = 0.6f),
                        topLeft = Offset(sCropX, sCropY),
                        size = Size(sCropW, sCropH),
                        style = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    )

                    // Render real-time live preview of the stamp at the new location (with live rotation!)
                    withTransform({
                        rotate(
                            degrees = rotationDegrees,
                            pivot = Offset(sNewX + sNewW / 2f, sNewY + sNewH / 2f)
                        )
                    }) {
                        if (stampPreviewBitmap != null) {
                            drawImage(
                                image = stampPreviewBitmap,
                                dstOffset = IntOffset(sNewX.roundToInt(), sNewY.roundToInt()),
                                dstSize = IntSize(sNewW.roundToInt(), sNewH.roundToInt()),
                                alpha = 0.85f
                            )
                        }

                        // Vibrant green outline around new location
                        drawRect(
                            color = EmeraldSuccess,
                            topLeft = Offset(sNewX, sNewY),
                            size = Size(sNewW, sNewH),
                            style = Stroke(width = 4f)
                        )

                        // Center crosshair
                        val centerX = sNewX + sNewW / 2f
                        val centerY = sNewY + sNewH / 2f
                        val crossSize = 16f
                        drawLine(EmeraldSuccess, Offset(centerX - crossSize, centerY), Offset(centerX + crossSize, centerY), 3f)
                        drawLine(EmeraldSuccess, Offset(centerX, centerY - crossSize), Offset(centerX, centerY + crossSize), 3f)

                        // 4 corner marker tags
                        val handleRadius = 10f
                        val newCorners = listOf(
                            Offset(sNewX, sNewY),
                            Offset(sNewX + sNewW, sNewY),
                            Offset(sNewX, sNewY + sNewH),
                            Offset(sNewX + sNewW, sNewY + sNewH)
                        )
                        for (corner in newCorners) {
                            drawCircle(color = EmeraldSuccess, radius = handleRadius, center = corner)
                        }
                    }
                }

                EditorMode.EDIT_TEXT -> {
                    // 1. Draw patched translucent area over old text
                    drawRect(
                        color = Color.White.copy(alpha = 0.90f),
                        topLeft = Offset(sTextX, sTextY),
                        size = Size(sTextW, sTextH)
                    )

                    // 2. Draw active SkyAccent dashed border around the text box
                    drawRect(
                        color = SkyAccent,
                        topLeft = Offset(sTextX, sTextY),
                        size = Size(sTextW, sTextH),
                        style = Stroke(
                            width = 3.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    )

                    // 3. Draw 4 corner handles
                    val handleRadius = 10f
                    val textCorners = listOf(
                        Offset(sTextX, sTextY),
                        Offset(sTextX + sTextW, sTextY),
                        Offset(sTextX, sTextY + sTextH),
                        Offset(sTextX + sTextW, sTextY + sTextH)
                    )
                    for (corner in textCorners) {
                        drawCircle(color = SkyAccent, radius = handleRadius, center = corner)
                        drawCircle(color = Color.White, radius = handleRadius * 0.5f, center = corner)
                    }

                    // 4. Live WYSIWYG text preview directly inside the box
                    if (replacementText.isNotEmpty()) {
                        val baseTypeface = when (textFontFamily.lowercase()) {
                            "serif" -> Typeface.SERIF
                            "monospace" -> Typeface.MONOSPACE
                            else -> Typeface.SANS_SERIF
                        }
                        val tfStyle = when {
                            isTextBold && isTextItalic -> Typeface.BOLD_ITALIC
                            isTextBold -> Typeface.BOLD
                            isTextItalic -> Typeface.ITALIC
                            else -> Typeface.NORMAL
                        }
                        val parsedColor = try {
                            android.graphics.Color.parseColor(textInkColorHex)
                        } catch (_: Exception) {
                            android.graphics.Color.BLACK
                        }

                        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = parsedColor
                            textSize = textFontSize * scale
                            typeface = Typeface.create(baseTypeface, tfStyle)
                            textAlign = when (textAlignment) {
                                "LEFT" -> Paint.Align.LEFT
                                "RIGHT" -> Paint.Align.RIGHT
                                else -> Paint.Align.CENTER
                            }
                        }

                        val fontMetrics = paint.fontMetrics
                        val baselineY = sTextY + (sTextH - fontMetrics.bottom + fontMetrics.top) / 2f - fontMetrics.top
                        val drawX = when (textAlignment) {
                            "LEFT" -> sTextX + 10f * scale
                            "RIGHT" -> sTextX + sTextW - 10f * scale
                            else -> sTextX + sTextW / 2f
                        }

                        drawContext.canvas.nativeCanvas.drawText(replacementText, drawX, baselineY, paint)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawDimmedMask(canvasSize: Size, cropRect: Rect) {
    val dimColor = Color.Black.copy(alpha = 0.42f)
    // Top rect
    drawRect(dimColor, Offset.Zero, Size(canvasSize.width, cropRect.top.coerceAtLeast(0f)))
    // Bottom rect
    drawRect(dimColor, Offset(0f, cropRect.bottom), Size(canvasSize.width, (canvasSize.height - cropRect.bottom).coerceAtLeast(0f)))
    // Left rect
    drawRect(dimColor, Offset(0f, cropRect.top), Size(cropRect.left.coerceAtLeast(0f), cropRect.height))
    // Right rect
    drawRect(dimColor, Offset(cropRect.right, cropRect.top), Size((canvasSize.width - cropRect.right).coerceAtLeast(0f), cropRect.height))
}

private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x1 - x2
    val dy = y1 - y2
    return kotlin.math.sqrt(dx * dx + dy * dy)
}
