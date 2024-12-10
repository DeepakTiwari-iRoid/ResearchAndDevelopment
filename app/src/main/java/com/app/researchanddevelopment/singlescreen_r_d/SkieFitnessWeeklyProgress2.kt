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

private val background2 = Color(0xFF252525)

@Composable
fun WeeklyProgressScreen2(modifier: Modifier = Modifier) {
    WeeklyProgressChartComponent2(
        data = SkyFitnessWeeklyProgress2(),
        modifier = modifier
    )
}

@Composable
private fun WeeklyProgressChartComponent2(
    data: SkyFitnessWeeklyProgress2,
    modifier: Modifier = Modifier,
) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
    ) {

        val width = size.width
        val height = size.height

        //keeps track of current height occupied by components
        var currentHeightOccupied = 0f

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

            //This to center the title
            val titleOffset = Offset(
                (width - textLayoutResult.size.width) / 2,
                marginTop.toPx()
            )


            //draw background arround title
            drawRoundRect(
                color = backGroundColor,
                topLeft = titleOffset.copy(
                    x = titleOffset.x - (backgroundPadding.width.toPx() / 2),
                    y = titleOffset.y - (backgroundPadding.height.toPx() / 2)
                ),
                size = Size(
                    textLayoutResult.size.width.toFloat() + backgroundPadding.width.toPx(),
                    textLayoutResult.size.height.toFloat() + backgroundPadding.height.toPx()
                ),
                cornerRadius = CornerRadius(
                    x = backgroundCornerRadius.toPx(),
                    y = backgroundCornerRadius.toPx()
                )
            )

            drawText(
                textLayoutResult,
                topLeft = titleOffset,
            )

            currentHeightOccupied =
                textLayoutResult.size.height.toFloat() + backgroundPadding.height.toPx() + marginTop.toPx()
        }

        /* Drawing Header Values */
        data.valuesHeader.apply {

            var valueHeight = 0
            var labelHeight = 0

            //Calculate max width of item
            val textSizes = items.map { item ->
                val textTitleLayoutResult = textMeasurer.measure(
                    text = item.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle
                )
                val textValueLayoutResult = textMeasurer.measure(
                    text = item.value.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle
                )
                valueHeight = textValueLayoutResult.size.height
                labelHeight = textTitleLayoutResult.size.height

                textValueLayoutResult.size.width.coerceAtLeast(textTitleLayoutResult.size.width)
            }

            // Calculate the total width of all text elements
            val totalTextWidth = textSizes.sum()

            val spacing = ((width - totalTextWidth) / (textSizes.size + 1))

            var currentX = spacing

            val valueStartPoint =
                currentHeightOccupied + valueMarginTop.toPx() //todo correct naming convention
            val labelStartPoint = valueStartPoint + valueHeight + labelMarginTop.toPx()

            items.forEachIndexed { index, item ->

                val textTitleLayoutResult = textMeasurer.measure(
                    text = item.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle
                )
                val textValueLayoutResult = textMeasurer.measure(
                    text = item.value.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle
                )
                val labelWidth = textTitleLayoutResult.size.width
                val valueWidth = textValueLayoutResult.size.width

                val shouldApplyExtraPaddingToTitle =
                    labelWidth < valueWidth //we are toggling boolean value // Highest < 278 = false

                val maxTextWidth = maxOf(
                    textValueLayoutResult.size.width,
                    textTitleLayoutResult.size.width
                )

                // Center each text horizontally within the `maxTextWidth`
                val textValueX = currentX + (maxTextWidth - textValueLayoutResult.size.width) / 2
                val textTitleX = currentX + (maxTextWidth - textTitleLayoutResult.size.width) / 2

                drawText(
                    textLayoutResult = textValueLayoutResult,
                    topLeft = Offset(
                        x = textValueX,
                        y = valueStartPoint
                    )
                )

                drawText(
                    textLayoutResult = textTitleLayoutResult,
                    topLeft = Offset(
                        x = textTitleX,
                        y = labelStartPoint
                    )
                )

                // Update the currentX position for the next element
                currentX += textSizes[index] + spacing
            }
            currentHeightOccupied = labelStartPoint + labelHeight
        }

        /**Drawing Graph Bars*/
        data.bars.apply {

            //Calculate max width of item
            val maxBarItemWidth = bars.map { item ->

                val textDateLayoutResult = textMeasurer.measure(
                    text = item.date,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = dateStyle
                )

                val textMonthLayoutResult = textMeasurer.measure(
                    text = item.month.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = dateStyle
                )

                val textScoreLayoutResult = textMeasurer.measure(
                    text = item.score.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = scoreStyle
                )

                textScoreLayoutResult.size.width
                    .coerceAtLeast(textDateLayoutResult.size.width)
                    .coerceAtLeast(textMonthLayoutResult.size.width)
            }

            // Calculate the total width of all text elements
            val totalTextWidth = maxBarItemWidth.sum()
            val totalSpacing = width - totalTextWidth
            val spacing = totalSpacing / (maxBarItemWidth.size + 1)

            var currentX =
                (totalSpacing - (spacing * (maxBarItemWidth.size - 1))) / 2 // Center-align
            val maxScore = bars.maxOf { it.score }

            val maxScoreWidth = textMeasurer.measure(
                text = maxScore.toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = dateStyle
            ).size.width

            val scoreIndicatorRadius = (maxScoreWidth / 2) + scoreIndicatorPadding.toPx()

            currentHeightOccupied += marginTop.toPx() + scoreIndicatorRadius + indicatorPadding.toPx()

            var isMaxScoreItem: Boolean

            var dateViewHeight = 0
            var dateHeight = 0
            var monthHeight = 0

            var currentBarHeightOccupied = currentHeightOccupied


            bars.forEachIndexed { index, bar ->

                isMaxScoreItem = (bar.score == maxScore)

                val textDateLayoutResult = textMeasurer.measure(
                    text = bar.date,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = dateStyle
                )
                val textMonthLayoutResult = textMeasurer.measure(
                    text = bar.month,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = dateStyle
                )
                val textScoreLayoutResult = textMeasurer.measure(
                    text = bar.score.toString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = scoreStyle
                )

                if (index == 0) {
                    //to add height only one time in variable
                    dateViewHeight =
                        (textDateLayoutResult.size.height + textMonthLayoutResult.size.height)
                    dateHeight = textDateLayoutResult.size.height
                    monthHeight = textMonthLayoutResult.size.height
                }

                val maxBarHeight =
                    height - dateViewHeight - currentHeightOccupied - paddingBottom.toPx()

                val barHeight = ((bar.score.dp.toPx() * maxBarHeight) / maxScore.dp.toPx())
                val startDrawingFrom = currentBarHeightOccupied - barHeight

                // Find the maximum width among the three text elements
                val maxTextWidth = maxOf(
                    textScoreLayoutResult.size.width,
                    textDateLayoutResult.size.width,
                    textMonthLayoutResult.size.width
                )

                // Center each text horizontally within the `maxTextWidth`
                val scoreX = currentX + (maxTextWidth - textScoreLayoutResult.size.width) / 2
                val dateX = currentX + (maxTextWidth - textDateLayoutResult.size.width) / 2
                val monthX = currentX + (maxTextWidth - textMonthLayoutResult.size.width) / 2

                // Horizontal and vertical center of the text
                val scoreCenterX = scoreX + (textScoreLayoutResult.size.width / 2)
                val scoreCenterY = currentHeightOccupied + (textScoreLayoutResult.size.height / 2)


                drawText(
                    textLayoutResult = textScoreLayoutResult,
                    topLeft = Offset(
                        x = scoreX,
                        y = startDrawingFrom
                    )
                )

                val barWidth = (scoreIndicatorRadius * 2) + indicatorPadding.toPx()


                drawRoundRect(
                    color = barColor,
                    cornerRadius = CornerRadius(cornerSize.toPx(), cornerSize.toPx()),
                    size = Size(
                        width = barWidth,
                        height = maxBarHeight
                    ),
                    topLeft = Offset(
                        x = scoreCenterX - (barWidth / 2),
                        y = scoreCenterY - (barWidth / 2)
                    )
                )

                //scoreIndicator
                drawCircle(
                    color = scoreIndicatorColor,
                    radius = scoreIndicatorRadius.toFloat(),
                    center = Offset(
                        x = scoreCenterX,
                        y = scoreCenterY
                    )
                )

                if (index == 0) {
                    currentBarHeightOccupied += maxBarHeight
                }

                drawText(
                    textLayoutResult = textDateLayoutResult,
                    topLeft = Offset(
                        x = dateX,
                        y = currentBarHeightOccupied
                    )
                )

                drawText(
                    textLayoutResult = textMonthLayoutResult,
                    topLeft = Offset(
                        x = monthX,
                        y = currentBarHeightOccupied + dateHeight
                    )
                )

                // Update the currentX position for the next element
                currentX += maxBarItemWidth[index] + spacing
            }
        }
    }

}


data class SkyFitnessWeeklyProgress2(
    val cornerRadius: Dp = 0.dp,
    val titleValues: Title = Title(),
    val valuesHeader: ValuesHeader = ValuesHeader(),
    val bars: Bars = Bars()
) {
    data class Title(
        val title: String = "Weekly Progress",
        val backgroundPadding: DpSize = DpSize(0.dp, 0.dp),
        val backgroundCornerRadius: Dp = 0.dp,
        val marginTop: Dp = 0.dp,
        val backGroundColor: Color = Color(0x0DFFFFFF),
        val textStyle: TextStyle = TextStyle(color = Color.White)
    )

    data class ValuesHeader(
        val items: List<Item> = listOf(
            Item(278, "Highest"),
            Item(110, "Average"),
            Item(1020, "Go"),
        ),
        val valueMarginTop: Dp = 0.dp,
        val labelMarginTop: Dp = 0.dp,
        val textStyle: TextStyle = TextStyle(color = Color.White)
    ) {
        data class Item(
            val value: Int,
            val label: String
        )
    }

    data class Bars(
        val cornerSize: Dp = 20.dp,
        val barWidth: Dp = 20.dp,
        val marginTop: Dp = 0.dp,
        val marginCenter: Dp = 20.dp,
        val dateMargin: Dp = 10.dp,
        val maxGraphHeight: Dp = 100.dp,
        val barColor: Color = Color(0x4D59D6DF),
        val scoreIndicatorColor: Color = Color(0x59D6DF4D),
        val scoreIndicatorPadding: Dp = 5.dp,
        val paddingBottom: Dp = 0.dp,
        val indicatorPadding: Dp = 5.dp,
        val maxScoreColorValues: MaxScoreColorValues = MaxScoreColorValues(),
        val dateStyle: TextStyle = TextStyle(color = Color.White),
        val scoreStyle: TextStyle = TextStyle(color = Color.White),
        val bars: List<Item> = listOf(
            Item(
                score = 42,
                date = "01",
                month = "Nov"
            ),
            Item(
                score = 50,
                date = "02",
                month = "Nov"
            ),
            Item(
                score = 300,
                date = "02",
                month = "Nov"
            ),
            Item(
                score = 100,
                date = "02",
                month = "Nov"
            ),
        )
    ) {
        data class Item(
            val score: Int,
            val date: String,
            val month: String,
        )

        data class MaxScoreColorValues(
            val maxBarColor: Color = Color(0xFF59D6DF),
            val maxIndicatorColor: Color = Color(0xFFFFFFFF),
            val maxMaxTextColor: Color = Color(0xFF000000),
        )
    }
}


@Preview
@Composable
private fun WeeklyProgressScreenPreview() {
    WeeklyProgressScreen(modifier = Modifier.fillMaxSize())
}
