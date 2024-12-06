package com.app.researchanddevelopment.singlescreen_r_d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

val background = Color(0xFF252525)

@Composable
fun WeeklyProgressScreen(modifier: Modifier = Modifier) {
    WeeklyProgressChartComponent(
        data = SkyFitnessWeeklyProgress(),
        modifier = modifier
    )
}

@Composable
fun WeeklyProgressChartComponent(
    data: SkyFitnessWeeklyProgress,
    modifier: Modifier = Modifier,
) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
    ) {

        val width = size.width
        val height = size.height

        drawRoundRect(
            color = Color.Black,
            size = size,
            cornerRadius = CornerRadius(x = data.cornerRadius.toPx(), y = data.cornerRadius.toPx())
        )

        /**Drawing Title of graph*/
        data.titleValues.apply {

            val textLayoutResult = textMeasurer.measure(
                text = data.titleValues.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = textStyle,
            )

            val titleOffset = Offset(
                (width - textLayoutResult.size.width) / 2,
                20.dp.toPx()
            )

            drawRoundRect(
                color = backGroundColor,
                topLeft = titleOffset.copy(x = titleOffset.x - (backgroundPadding.width.toPx() / 2), y = titleOffset.y - (backgroundPadding.height.toPx() / 2)),
                size = Size(
                    textLayoutResult.size.width.toFloat() + backgroundPadding.width.toPx(),
                    textLayoutResult.size.height.toFloat() + backgroundPadding.height.toPx()
                ),
                cornerRadius = CornerRadius(x = backgroundCornerRadius.toPx(), y = backgroundCornerRadius.toPx())
            )

            drawText(
                textLayoutResult,
                topLeft = titleOffset,
            )
        }

        /** Drawing Header Values */
        data.valuesHeader.apply {

            //Calculate max width of item
            val textSizes = items.map { item ->
                val textTitleLayoutResult = textMeasurer.measure(text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                val textValueLayoutResult = textMeasurer.measure(text = item.value.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                textValueLayoutResult.size.width.coerceAtLeast(textTitleLayoutResult.size.width)
            }

            // Calculate the total width of all text elements
            val totalTextWidth = textSizes.sum()

            val spacing = ((width - totalTextWidth) / (textSizes.size + 1))

            var currentX = spacing

            items.forEachIndexed { index, item ->

                val textTitleLayoutResult = textMeasurer.measure(text = item.title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                val textValueLayoutResult = textMeasurer.measure(text = item.value.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                val titleWidth = textTitleLayoutResult.size.width
                val valueWidth = textValueLayoutResult.size.width

                val shouldApplyExtraPaddingToTitle = titleWidth < valueWidth //we are toggling boolean value // Highest < 278 = false

                drawText(
                    textLayoutResult = textValueLayoutResult,
                    topLeft = Offset(
                        x = if (shouldApplyExtraPaddingToTitle) currentX else currentX + (textSizes[index] - textValueLayoutResult.size.width) / 2,
                        y = marginTop.toPx()
                    )
                )

                drawText(
                    textLayoutResult = textTitleLayoutResult,
                    topLeft = Offset(x = if (shouldApplyExtraPaddingToTitle) currentX + (textSizes[index] - textTitleLayoutResult.size.width) / 2 else currentX, y = marginTop.toPx() + marginCenter.toPx())
                )

                // Update the currentX position for the next element
                currentX += textSizes[index] + spacing
            }
        }

        /**Drawing Graph Bars*/
        data.bars.apply {

            //Calculate max width of item
            val maxBarItemWidth = bars.map { item ->
                val textDateLayoutResult = textMeasurer.measure(text = item.date, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                val textMonthLayoutResult = textMeasurer.measure(text = item.month.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                textMonthLayoutResult.size.width.coerceAtLeast(textDateLayoutResult.size.width).coerceAtLeast(barWidth.toPx().toInt())
            }

            // Calculate the total width of all text elements
            val totalTextWidth = maxBarItemWidth.sum()

            val spacing = ((width - totalTextWidth) / (maxBarItemWidth.size + 1))

            var currentX = spacing

            val maxScore = bars.maxOf { it.score }

            val graphMarginTop = data.valuesHeader.marginTop.toPx() + marginTop.toPx()

            bars.forEachIndexed { index, bar ->

                val textDateLayoutResult = textMeasurer.measure(bar.date, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                val textMonthLayoutResult = textMeasurer.measure(bar.month, maxLines = 1, overflow = TextOverflow.Ellipsis, style = textStyle)
                val textScoreLayoutResult = textMeasurer.measure(bar.score.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis, style = if (bar.score == maxScore) textStyle.copy(color = bar.textColor) else textStyle)

                val dateWidth = textDateLayoutResult.size.width
                val monthWidth = textMonthLayoutResult.size.width

                val shouldApplyExtraPaddingToDate = dateWidth < monthWidth //we are toggling boolean value // 01 < NOV = true

                val indicatorRadius = (barWidth.toPx() / 2f) - indicatorPadding.toPx()

                val minBarHeight = ((indicatorRadius * 2f) + indicatorPadding.toPx() * 2f)// Ensure bars are taller than the indicator

                val barHeight = ((bar.score.dp.toPx() * maxGraphHeight.toPx()) / maxScore.dp.toPx()).coerceAtLeast(minBarHeight)
                val startDrawingFrom = maxGraphHeight.toPx() - barHeight

                val barX = if (barWidth.toPx() > dateWidth.toDp().toPx()) currentX + (maxBarItemWidth[index] - barWidth.toPx()) / 2 else currentX

                drawRoundRect(
                    color = bar.barColor,
                    size = Size(
                        barWidth.toPx(),
                        barHeight
                    ),
                    topLeft = Offset(
                        x = barX,
                        y = (startDrawingFrom + graphMarginTop)
                    ),
                    cornerRadius = CornerRadius(x = cornerSize.toPx(), y = cornerSize.toPx())
                )


                drawText(
                    textLayoutResult = textDateLayoutResult,
                    topLeft = Offset(
                        x = if (shouldApplyExtraPaddingToDate) currentX + (maxBarItemWidth[index] - textDateLayoutResult.size.width) / 2 else currentX,
                        y = maxGraphHeight.toPx() + graphMarginTop + dateMargin.toPx()
                    )
                )

                drawText(
                    textLayoutResult = textMonthLayoutResult,
                    topLeft = Offset(
                        x = if (shouldApplyExtraPaddingToDate) currentX else currentX + (maxBarItemWidth[index] - textMonthLayoutResult.size.width) / 2,
                        y = maxGraphHeight.toPx() + graphMarginTop + dateMargin.toPx() + marginCenter.toPx()
                    )
                )

                val indicatorX = currentX + (barWidth.toPx() - indicatorRadius)

                drawCircle(
                    color = bar.scoreIndicatorColor,
                    radius = indicatorRadius,
                    center = Offset(
                        x = indicatorX,
                        y = (startDrawingFrom + graphMarginTop + indicatorRadius + indicatorPadding.toPx())
                    )
                )


                drawText(
                    textLayoutResult = textScoreLayoutResult,
                    topLeft = Offset(
                        x = ((currentX + barWidth.toPx() - indicatorRadius) - textScoreLayoutResult.size.width / 2),
                        y = (startDrawingFrom + graphMarginTop + indicatorPadding.toPx())
                    )
                )

                // Update the currentX position for the next element
                currentX += maxBarItemWidth[index] + spacing
            }
        }
    }

}


data class SkyFitnessWeeklyProgress(
    val cornerRadius: Dp = 20.dp,
    val titleValues: Title = Title(),
    val valuesHeader: ValuesHeader = ValuesHeader(),
    val bars: Bars = Bars()
) {
    data class Title(
        val title: String = "Weekly Progress",
        val backgroundPadding: DpSize = DpSize(35.dp, 12.dp),
        val backgroundCornerRadius: Dp = 8.dp,
        val backGroundColor: Color = Color(0x0DFFFFFF),
        val textStyle: TextStyle = TextStyle(color = Color.White)
    )

    data class ValuesHeader(
        val items: List<Item> = listOf(
            Item(278, "Highest"),
            Item(110, "Average"),
            Item(1020, "Go"),
        ),
        val marginTop: Dp = 100.dp,
        val marginCenter: Dp = 20.dp,
        val textStyle: TextStyle = TextStyle(color = Color.White)
    ) {
        data class Item(
            val value: Int,
            val title: String
        )
    }

    data class Bars(
        val cornerSize: Dp = 20.dp,
        val barWidth: Dp = 20.dp,
        val marginTop: Dp = 80.dp,
        val marginCenter: Dp = 20.dp,
        val dateMargin: Dp = 10.dp,
        val maxGraphHeight: Dp = 100.dp,
        val indicatorPadding: Dp = 2.dp,
        val textStyle: TextStyle = TextStyle(color = Color.White),
        val bars: List<Item> = listOf(
            Item(
                score = 42,
                barColor = Color(0x4D59D6DF),
                scoreIndicatorColor = Color(0x59D6DF4D),
                textColor = Color.White,
                date = "01",
                month = "Nov"
            ),
            Item(
                score = 50,
                barColor = Color(0x4D59D6DF),
                scoreIndicatorColor = Color(0x59D6DF4D),
                textColor = Color.White,
                date = "02",
                month = "Nov"
            ),
            Item(
                score = 300,
                barColor = Color(0xFF59D6DF),
                scoreIndicatorColor = Color.White,
                textColor = Color.Black,
                date = "02",
                month = "Nov"
            ),
            Item(
                score = 100,
                barColor = Color(0x4D59D6DF),
                scoreIndicatorColor = Color(0x59D6DF4D),
                textColor = Color.White,
                date = "02",
                month = "Nov"
            ),
        )
    ) {
        data class Item(
            val score: Int,
            val barColor: Color,
            val scoreIndicatorColor: Color,
            val textColor: Color,
            val date: String,
            val month: String,
        )
    }

}


@Preview
@Composable
private fun WeeklyProgressScreenPreview() {
    WeeklyProgressScreen(modifier = Modifier.fillMaxSize())
}
