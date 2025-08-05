package com.app.research.singlescreen_r_d.skaifitness

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.research.data.TempDataSource.tempProgressData
import com.skaifitness.app.ui.canvas.graph.createTextMeasurer


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun MyProgressBarChartWithIndicatorTextPreview(modifier: Modifier = Modifier) {
    MyProgressBarChartWithTextIndicator(
        modifier = modifier.size(200.dp),
        myProgressBarChart = MyProgressBarChart(item = tempProgressData)
    )
}


@Composable
fun MyProgressBarChartWithTextIndicator(
    myProgressBarChart: MyProgressBarChart,
    modifier: Modifier = Modifier
) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {

        val width = size.width
        val height = size.height

        val monthStyle = myProgressBarChart.monthStyle
        val dateStyle = myProgressBarChart.dateStyle ?: monthStyle
        val scoreStyle = myProgressBarChart.scoreStyle
        val colors = myProgressBarChart.colors

        // Padding for the bar and for the indicator will be half of it /2
        val padding = myProgressBarChart.padding.toPx()

        val maxScore = myProgressBarChart.item.maxOf { it.score }

        val maxScoreMeasurer = textMeasurer.createTextMeasurer(maxScore.toString(), scoreStyle)

        val barWidth =
            (maxScoreMeasurer.size.width + padding)
                .coerceAtLeast((maxScoreMeasurer.size.width * 1.5f) + padding)
                .coerceAtLeast((myProgressBarChart.barWidth.toPx() + padding)).toInt()

        // Finds the maximum item width in list
        val maxItemWidth = myProgressBarChart.item.map {
            val date = textMeasurer.createTextMeasurer(it.date, dateStyle)
            val month = textMeasurer.createTextMeasurer(it.month, monthStyle)
            date.size.width.coerceAtLeast(month.size.width).coerceAtLeast(barWidth)
        }

        // Calculate the total width of all text elements
        val totalSpacing = width - maxItemWidth.sum()
        val spacing = totalSpacing / (maxItemWidth.size + 1)

        var currentX = spacing

        myProgressBarChart.item.forEachIndexed { index, item ->

            val currentIsMax = item.score == maxScore

            // Find the maximum width among the three text elements
            val score = textMeasurer.createTextMeasurer(
                item.score.toString(), scoreStyle.copy(
                    color = if (currentIsMax) colors.maxScoreColor
                    else colors.otherScoreColor
                )
            )

            val date = textMeasurer.createTextMeasurer(item.date, dateStyle)
            val month = textMeasurer.createTextMeasurer(item.month, monthStyle)
            maxScoreMeasurer.size.let { maxScoreSize ->
                score.size.let { scoreSize ->
                    month.size.let { monthSize ->
                        date.size.let { dateSize ->

                            val maxWidth = maxOf(
                                date.size.width,
                                month.size.width,
                                barWidth
                            )

                            // Center each view horizontally within the maxItemWidth
                            val barWidthX = currentX + (maxWidth - barWidth) / 2
                            val scoreWidthX = currentX + (maxWidth - scoreSize.width) / 2
                            val dateX = currentX + (maxWidth - dateSize.width) / 2
                            val monthX = currentX + (maxWidth - monthSize.width) / 2
                            val circularIndicatorX = barWidthX + barWidth / 2f

                            val dateY =
                                height - dateSize.height - monthSize.height // date starting point
                            val monthY = dateY + dateSize.height

                            //Text Offset Calculation
                            val totalBarPadding =
                                (padding + maxScoreSize.width) / 2 + (maxScoreSize.height / 2)
                            val drawableArea = dateY - totalBarPadding
                            val textYProgress =
                                item.score.dp.toPx() * drawableArea / maxScore.dp.toPx()

                            val scoreY = maxOf(
                                totalBarPadding,
                                (dateY - totalBarPadding) - textYProgress
                            ) - maxScoreSize.height / 2

                            //Score Indicator
                            val radius = barWidth / 2 - padding / 2
                            val indicatorY = scoreY + maxScoreSize.height / 2

                            //Bar
                            val barY = indicatorY - radius - padding / 2

                            val endingBarY =
                                drawableArea + radius + padding / 2
                            val barHeight =
                                maxOf(radius * 2, endingBarY - barY)

                            drawRoundRect(
                                color = if (currentIsMax) colors.maxBarColor else colors.otherBarColor,
                                cornerRadius = CornerRadius(barWidth / 2f),
                                topLeft = Offset(
                                    x = barWidthX,
                                    y = barY
                                ),
                                size = Size(
                                    width = barWidth * 1f,
                                    height = barHeight
                                ),
                            )

                            drawCircle(
                                color = if (currentIsMax) colors.maxScoreIndicatorColor else colors.otherScoreIndicatorColor,
                                radius = radius,
                                center = Offset(
                                    x = circularIndicatorX,
                                    y = indicatorY
                                )
                            )

                            drawText(
                                topLeft = Offset(
                                    x = scoreWidthX,
                                    y = scoreY
                                ),
                                textLayoutResult = score,
                            )

                            drawText(
                                textLayoutResult = textMeasurer.createTextMeasurer(
                                    item.date,
                                    style = dateStyle
                                ),
                                topLeft = Offset(dateX, dateY)
                            )

                            drawText(
                                textLayoutResult = textMeasurer.createTextMeasurer(
                                    item.month,
                                    style = monthStyle
                                ),
                                topLeft = Offset(monthX, monthY)
                            )

                            currentX += maxItemWidth[index] + spacing

                        }
                    }
                }
            }
        }
    }
}
