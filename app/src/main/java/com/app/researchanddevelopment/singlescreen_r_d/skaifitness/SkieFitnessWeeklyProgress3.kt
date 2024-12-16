package com.app.researchanddevelopment.singlescreen_r_d.skaifitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

val background3 = Color(0xFF252525)

@Composable
fun WeeklyProgressScreen3(modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()
    ) {
        WeeklyProgressChartComponent3(
            data = SkyFitnessWeeklyProgress3(),
            modifier = modifier
        )
    }

}

@Composable
fun WeeklyProgressChartComponent3(
    data: SkyFitnessWeeklyProgress3,
    modifier: Modifier = Modifier,
) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
    ) {

        val width = size.width
        val height = size.height

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

            var currentX = spacing

            val maxScore = bars.maxOf { it.score }

            val maxScoreTextResult = textMeasurer.measure(
                text = maxScore.toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = dateStyle
            ).size

            val scoreIndicatorRadius = (maxScoreTextResult.width / 2) + scoreIndicatorPadding.toPx()


            var isMaxScoreItem: Boolean

            var dateViewHeight = 0
            var dateHeight = 0
            var monthHeight = 0



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
                    style = scoreStyle.copy(color = if (isMaxScoreItem) maxScoreColorValues.maxMaxTextColor else scoreStyle.color)
                )

                if (index == 0) {
                    dateHeight = textDateLayoutResult.size.height
                    monthHeight = textMonthLayoutResult.size.height
                    //to add height only one time in variable
                    dateViewHeight = dateHeight + monthHeight
                }

                val maxBarHeight = height - dateViewHeight - paddingBottom.toPx()

                val adjustedMaxBarHeight =
                    maxBarHeight - (scoreIndicatorRadius * 2) - indicatorPadding.toPx() // added padding to bottom so that indicator take place

                val barStartFromY = adjustedMaxBarHeight - // we got starting point
                        ((adjustedMaxBarHeight / maxScore) // we got one unit value
                                * bar.score) // actual value result according to per unit

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


                // Calculate bar width as the diameter of the circular indicator
                val barWidth = (scoreIndicatorRadius * 2) + indicatorPadding.toPx()

                // Ensure the minimum bar height includes the indicator size + padding
                val minBarHeight = (scoreIndicatorRadius * 2f) + indicatorPadding.toPx()

                // Calculate the adjusted height for the bar
                val adjustedBarHeight = (maxBarHeight - barStartFromY - scoreIndicatorRadius)
                    .coerceAtLeast(minBarHeight) // Ensure the minimum height

                // Adjust the Y-position to start directly below the circular indicator
                val adjustedStartY =
                    (barStartFromY + indicatorPadding.toPx() + scoreIndicatorPadding.toPx())

                drawRoundRect(
                    color = if (isMaxScoreItem) maxScoreColorValues.maxBarColor else barColor,
                    size = Size(
                        width = barWidth,
                        height = adjustedBarHeight // Height from the top of the bar to its bottom
                    ),
                    topLeft = Offset(
                        x = scoreX + textScoreLayoutResult.size.width / 2 - (barWidth / 2), // Center bar under the circle
                        y = adjustedStartY  // Start the bar at its calculated Y position
                    ),
                    cornerRadius = CornerRadius(barWidth / 2) // Optional: Make the bar's corners match the circular look
                )


                drawCircle(
                    color = if (isMaxScoreItem) maxScoreColorValues.maxIndicatorColor else scoreIndicatorColor,
                    radius = scoreIndicatorRadius,
                    center = Offset(
                        x = scoreX + textScoreLayoutResult.size.width / 2,
                        y = barStartFromY + scoreIndicatorRadius + indicatorPadding.toPx() + maxScoreTextResult.height / 2,
                    )
                )

                drawText(
                    textLayoutResult = textScoreLayoutResult,
                    topLeft = Offset(
                        x = scoreX,
                        y = barStartFromY + scoreIndicatorRadius + indicatorPadding.toPx()
                    )
                )


                drawText(
                    textLayoutResult = textDateLayoutResult,
                    topLeft = Offset(
                        x = dateX,
                        y = maxBarHeight
                    )
                )

                drawText(
                    textLayoutResult = textMonthLayoutResult,
                    topLeft = Offset(
                        x = monthX,
                        y = maxBarHeight + dateHeight
                    )
                )

                // Update the currentX position for the next element
                currentX += maxBarItemWidth[index] + spacing
            }


        }
    }

}


data class SkyFitnessWeeklyProgress3(
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
                score = 12,
                date = "01",
                month = "Nov"
            ),
            Item(
                score = 50,
                date = "02",
                month = "Nov"
            ),
            Item(
                score = 100,
                date = "02",
                month = "Nov"
            ),
            Item(
                score = 10,
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
    WeeklyProgressScreen3(modifier = Modifier.fillMaxSize())
}
