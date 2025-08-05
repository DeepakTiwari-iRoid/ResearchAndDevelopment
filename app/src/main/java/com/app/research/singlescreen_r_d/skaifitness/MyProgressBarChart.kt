package com.skaifitness.app.ui.canvas.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.research.data.TempDataSource
import com.app.research.singlescreen_r_d.skaifitness.MyProgressBarChart


@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MyProgressPreview(modifier: Modifier = Modifier) {
    MyProgressBarChart(
        modifier = modifier.size(200.dp),
        myProgressBarChart = MyProgressBarChart(item = TempDataSource.tempProgressData)
    )
}


@Composable
fun MyProgressBarChart(
    myProgressBarChart: MyProgressBarChart,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {

        val width = size.width
        val height = size.height


        val monthStyle = myProgressBarChart.monthStyle
        val dateStyle = myProgressBarChart.dateStyle ?: monthStyle
        val colors = myProgressBarChart.colors

        var maxScore = 0

        val maxItemWidth = myProgressBarChart.item.map {
            maxScore = maxOf(
                maxScore,
                it.score
            ) // just used here to not iterate over the list again, used this map to get the max score
            val date = textMeasurer.createTextMeasurer(it.date, dateStyle)
            val month = textMeasurer.createTextMeasurer(it.month, monthStyle)
            date.size.width
                .coerceAtLeast(month.size.width)
                .coerceAtLeast(myProgressBarChart.barWidth.toPx().toInt())
        }

        // Calculate the total width of all text elements
        val totalTextWidth = maxItemWidth.sum()
        val totalSpacing = width - totalTextWidth
        val spacing = totalSpacing / (maxItemWidth.size + 1)

        var currentX = spacing



        myProgressBarChart.item.forEachIndexed { index, item ->

            // Find the maximum width among the three text elements
            val currentIsMax = item.score == maxScore

            val date = textMeasurer.createTextMeasurer(item.date, dateStyle)
            val month = textMeasurer.createTextMeasurer(item.month, monthStyle)


            val maxWidth = maxOf(
                date.size.width,
                month.size.width,
                myProgressBarChart.barWidth.toPx().toInt()
            )

            // Center each text horizontally within the maxTextWidth
            val barWidthX = currentX + (maxWidth - myProgressBarChart.barWidth.toPx()) / 2
            val dateX = currentX + (maxWidth - date.size.width) / 2
            val monthX = currentX + (maxWidth - month.size.width) / 2

            val dateY = height - date.size.height - month.size.height
            val monthY = height - month.size.height

            val progress = dateY * item.score.dp.toPx() / maxScore.dp.toPx()
            val barYStartFrom = dateY - progress

            drawRoundRect(
                color = if (currentIsMax) colors.maxBarColor else colors.otherBarColor,
                topLeft = Offset(barWidthX, barYStartFrom),
                size = Size(myProgressBarChart.barWidth.toPx(), dateY - barYStartFrom),
                cornerRadius = CornerRadius(myProgressBarChart.barWidth.toPx() / 2)
            )

            drawText(
                textLayoutResult = textMeasurer.createTextMeasurer(item.date, style = dateStyle),
                topLeft = Offset(dateX, dateY)
            )

            drawText(
                textLayoutResult = textMeasurer.createTextMeasurer(item.month, style = monthStyle),
                topLeft = Offset(monthX, monthY)
            )

            currentX += maxItemWidth[index] + spacing

        }
    }
}

fun TextMeasurer.createTextMeasurer(text: String, style: TextStyle) =
    measure(text, style = style, overflow = TextOverflow.Ellipsis, maxLines = 1)


