package com.app.research.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit

val isPreviewMode: Boolean @Composable get() = LocalInspectionMode.current

val displaySizeMatrix: IntSize @Composable get() = LocalWindowInfo.current.containerSize


@Composable
fun Float.pxToDp(): Dp {
    val density = LocalDensity.current
    return with(density) { toDp() }
}

@Composable
fun Int.pxToDp(): Dp {
    val density = LocalDensity.current
    return with(density) { toDp() }
}

@Composable
fun Float.pxToSp(): TextUnit {
    val density = LocalDensity.current
    return with(density) { this@pxToSp.toSp() }
}