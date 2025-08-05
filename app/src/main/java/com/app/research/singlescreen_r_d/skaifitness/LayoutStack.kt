package com.app.research.singlescreen_r_d.skaifitness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun HStack(
    spaceBy: Dp,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(spaceBy),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}

@Composable
fun VStack(
    spaceBy: Dp,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(spaceBy),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}


@Composable
fun FullSizeCenterBox(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.fillMaxSize(), content = content)
}