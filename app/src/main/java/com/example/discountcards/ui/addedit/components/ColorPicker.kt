package com.example.discountcards.ui.addedit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.discountcards.R

val CARD_COLOR_PALETTE = listOf(
    // Синие и фиолетовые
    0xFF1E88E5.toInt(), // Ocean Blue
    0xFF0000C3.toInt(), // Blue (my)
    // 0xFF00ACC1.toInt(), // Bright Cyan
    0xFF3949AB.toInt(), // Indigo Blue
    0xFF512DA8.toInt(), // Ultramarine
    0xFF8E24AA.toInt(), // Rich Purple
    0xFF880E4F.toInt(), // Burgundy Maroon

    // Красные и бордовые
    0xFFF00000.toInt(), // Red (my)
    0xFFE53935.toInt(), // Crimson Red
    0xFFD81B60.toInt(), // Hot Pink / Fuchsia

    // Оранжевые и жёлтые
    0xFFFF8000.toInt(), // Orange (my)
    // 0xFFFB8C00.toInt(), // Vibrant Orange
    0xFFFFB300.toInt(), // Amber Gold
    // 0xFFFFD100.toInt(), // Yellow (my)

    // Зелёные
    0xFF78FF0C.toInt(), // Green Lime (my)
    0xFF04A70A.toInt(), // Green (my)
    0xFF4CAF50.toInt(), // Emerald Green
    // 0xFF7CB342.toInt(), // Lime Green
    0xFF26A69A.toInt(), // Mint Green
    // 0xFF00897B.toInt(), // Deep Teal


    0xFF6D4C41.toInt(), // Warm Brown
    0xFFA1887F.toInt(), // Warm Bronze

    // Нейтральные и премиум
    0xFF212121.toInt(), // Obsidian Black
    0xFF455A64.toInt(), // Slate Charcoal
    0xFFB0BEC5.toInt(), // Silver Platinum
    0xFFECEFF1.toInt() // Pearl White

)

@Composable
fun ColorPicker(
    selectedColorInt: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomColorDialog by remember { mutableStateOf(false) }
    val isCustomColorSelected = selectedColorInt !in CARD_COLOR_PALETTE

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.card_color_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            items(CARD_COLOR_PALETTE) { colorInt ->
                val isSelected = colorInt == selectedColorInt
                val color = Color(colorInt)
                val contrastBorder = getContrastBorder(color)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color)
                        .then(
                            if (isSelected) {
                                Modifier.border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                            } else if (contrastBorder != null) {
                                Modifier.border(contrastBorder, CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onColorSelected(colorInt) }
                ) {
                    if (isSelected) {
                        val checkTint = if (color.luminance() > 0.5f) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = checkTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Custom Color Button
            item {
                val customColor = Color(selectedColorInt)
                val rainbowBrush = remember {
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF5252),
                            Color(0xFFFFD740),
                            Color(0xFF69F0AE),
                            Color(0xFF40C4FF),
                            Color(0xFFE040FB)
                        )
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .then(
                            if (isCustomColorSelected) {
                                Modifier
                                    .background(customColor)
                                    .border(
                                        width = 3.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                            } else {
                                Modifier.background(rainbowBrush)
                            }
                        )
                        .clickable { showCustomColorDialog = true }
                ) {
                    if (isCustomColorSelected) {
                        val checkTint = if (customColor.luminance() > 0.5f) Color.Black else Color.White
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.custom_color_button_desc),
                            tint = checkTint,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Palette,
                            contentDescription = stringResource(R.string.custom_color_button_desc),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCustomColorDialog) {
        CustomColorDialog(
            initialColorInt = selectedColorInt,
            onDismiss = { showCustomColorDialog = false },
            onColorConfirmed = { newColorInt ->
                onColorSelected(newColorInt)
                showCustomColorDialog = false
            }
        )
    }
}

@Composable
private fun getContrastBorder(color: Color): BorderStroke? {
    val isDarkTheme = isSystemInDarkTheme()
    val luminance = color.luminance()

    return when {
        // Светлый цвет на светлом фоне: тёмная/серая обводка
        !isDarkTheme && luminance > 0.60f -> {
            BorderStroke(1.5.dp, Color(0x33000000))
        }
        // Тёмный цвет на тёмном фоне: светлая/серая обводка
        isDarkTheme && luminance < 0.25f -> {
            BorderStroke(1.5.dp, Color(0x55FFFFFF))
        }
        else -> null
    }
}

@Composable
private fun CustomColorDialog(
    initialColorInt: Int,
    onDismiss: () -> Unit,
    onColorConfirmed: (Int) -> Unit
) {
    val initialHsv = remember(initialColorInt) {
        FloatArray(3).apply {
            android.graphics.Color.colorToHSV(initialColorInt, this)
        }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) } // 0f .. 360f
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) } // 0f .. 1f
    var value by remember { mutableFloatStateOf(initialHsv[2]) } // 0f .. 1f

    var hexInput by remember {
        mutableStateOf(String.format("%06X", (0xFFFFFF and initialColorInt)))
    }

    val currentColorInt = remember(hue, saturation, value) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }

    val previewColor = Color(currentColorInt)
    val contrastBorder = getContrastBorder(previewColor)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.custom_color_title),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Превью цвета + ввод HEX
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .then(
                                if (contrastBorder != null) {
                                    Modifier.border(contrastBorder, CircleShape)
                                } else {
                                    Modifier.border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                }
                            )
                    )

                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isLetterOrDigit() || it == '#' }.take(9)
                            hexInput = filtered.removePrefix("#")
                            parseHexColor(hexInput)?.let { parsed ->
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(parsed, hsv)
                                hue = hsv[0]
                                saturation = hsv[1]
                                value = hsv[2]
                            }
                        },
                        label = { Text(stringResource(R.string.custom_color_hex_label)) },
                        placeholder = { Text(stringResource(R.string.custom_color_hex_placeholder)) },
                        singleLine = true,
                        isError = parseHexColor(hexInput) == null && hexInput.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    )
                }

                // 2D Цветовой квадрат Photoshop (Saturation-Value)
                PhotoshopSatValSquare(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onSatValChanged = { s, v ->
                        saturation = s
                        value = v
                        val newInt = android.graphics.Color.HSVToColor(floatArrayOf(hue, s, v))
                        hexInput = String.format("%06X", (0xFFFFFF and newInt))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                // Полоса спектра оттенков Photoshop (Hue Slider)
                PhotoshopHueBar(
                    hue = hue,
                    onHueChanged = { h ->
                        hue = h
                        val newInt = android.graphics.Color.HSVToColor(floatArrayOf(h, saturation, value))
                        hexInput = String.format("%06X", (0xFFFFFF and newInt))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = parseHexColor(hexInput) != null,
                onClick = {
                    onColorConfirmed(currentColorInt)
                }
            ) {
                Text(stringResource(R.string.select_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}

@Composable
private fun PhotoshopSatValSquare(
    hue: Float,
    saturation: Float,
    value: Float,
    onSatValChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pureHueColor = remember(hue) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInput(hue) {
                detectTapGestures { offset ->
                    val s = (offset.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                    onSatValChanged(s, v)
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width).coerceIn(0f, 1f)
                    val v = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                    onSatValChanged(s, v)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Градиент от белого к чистому тону (Насыщенность X)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, pureHueColor)
                )
            )
            // 2. Вертикальный градиент от прозрачного к чёрному (Яркость Y)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
            )

            // 3. Маркер селектора (курсор)
            val selectorX = saturation * size.width
            val selectorY = (1f - value) * size.height

            // Внешнее чёрное кольцо
            drawCircle(
                color = Color.Black,
                radius = 10.dp.toPx(),
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 3.dp.toPx())
            )
            // Внутреннее белое кольцо
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = Offset(selectorX, selectorY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun PhotoshopHueBar(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val hueColors = remember {
        listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val h = ((offset.x / size.width) * 360f).coerceIn(0f, 360f)
                    onHueChanged(h)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val h = ((change.position.x / size.width) * 360f).coerceIn(0f, 360f)
                    onHueChanged(h)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Радужный спектр оттенков
            drawRect(
                brush = Brush.horizontalGradient(colors = hueColors)
            )

            // Позиция ползунка
            val thumbX = (hue / 360f) * size.width
            val centerY = size.height / 2f

            // Внешнее чёрное кольцо
            drawCircle(
                color = Color.Black,
                radius = 11.dp.toPx(),
                center = Offset(thumbX, centerY),
                style = Stroke(width = 3.dp.toPx())
            )
            // Внутреннее белое кольцо
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx(),
                center = Offset(thumbX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun parseHexColor(input: String): Int? {
    val clean = input.trim().removePrefix("#")
    if (clean.length == 6 || clean.length == 8) {
        return try {
            val fullHex = if (clean.length == 6) "FF$clean" else clean
            fullHex.toLong(16).toInt()
        } catch (_: Exception) {
            null
        }
    }
    return null
}
