package com.app.researchanddevelopment.singlescreen_r_d.skaifitness

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.researchanddevelopment.ui.theme.neonNazar
import com.app.researchanddevelopment.ui.theme.outFit


data class MyProgressBarChart(
    val item: List<Item>,
    val dateStyle: TextStyle? = MyProgressBarChartDefaults.dateStyle,
    val monthStyle: TextStyle = MyProgressBarChartDefaults.monthStyle,
    val scoreStyle: TextStyle = MyProgressBarChartDefaults.scoreStyle,
    val colors: MyProgressBarChartDefaults.Colors = MyProgressBarChartDefaults.Colors(),
    val barWidth: Dp = MyProgressBarChartDefaults.barWidth,
    val padding:Dp = 2.dp
) {
    data class Item(
        val score: Int = 0,
        val month: String = "Jan",
        val date: String = "01",
    )

}


object MyProgressBarChartDefaults {
    // this is for Simple Progress Chat without score indicator
    val barWidth: Dp = 10.dp


    val dateStyle = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 6.sp,
        color = Color.White
    )
    val monthStyle: TextStyle = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 4.sp,
        color = Color.White
    )
    val scoreStyle: TextStyle = TextStyle(
        fontFamily = outFit,
        fontWeight = FontWeight.SemiBold,
        fontSize = 4.sp,
        color = Color.White
    )

    data class Colors(
        val maxBarColor: Color = neonNazar,
        val maxScoreIndicatorColor: Color = Color.White,
        val maxScoreColor: Color = Color.Black,
        val otherBarColor: Color = neonNazar.copy(alpha = 0.5f),
        val otherScoreIndicatorColor: Color = neonNazar.copy(alpha = 0.3f),
        val otherScoreColor: Color = Color.White
    )

}

