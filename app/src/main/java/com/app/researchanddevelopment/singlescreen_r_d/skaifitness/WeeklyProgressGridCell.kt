package com.app.researchanddevelopment.singlescreen_r_d.skaifitness

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.researchanddevelopment.R
import com.app.researchanddevelopment.data.TempDataSource.weekList
import com.app.researchanddevelopment.ui.theme.aliceBlue
import com.app.researchanddevelopment.ui.theme.black25
import com.app.researchanddevelopment.ui.theme.grey94
import com.app.researchanddevelopment.ui.theme.outFit
import com.app.researchanddevelopment.ui.theme.white


@Preview(showBackground = true)
@Composable
private fun WeeklyProgressPreview(
    modifier: Modifier = Modifier
) {
    WeeklyProgress(
        weeks = weekList,
        modifier = modifier
    )
}


@Composable
fun WeeklyProgress(
    weeks: List<List<Int>>,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier,
    ) {

        itemsIndexed(weeks) { index, item ->
            WeeklyProgressItem(
                week = index + 1,
                completedDays = item,
                weeklyProgressCheckPoints = weeklyBarHelper(index, weeks),
            )
        }
    }
}


private fun weeklyBarHelper(
    currentIndex: Int,
    weeks: List<List<Int>>,
): WeeklyProgressCheckPoints {

    val isFirst = currentIndex == 0
    val isLast = currentIndex == weeks.lastIndex
    val previousWeek = weeks.getOrNull(currentIndex - 1)
    val currentWeek = weeks[currentIndex]
    val nextWeek = weeks.getOrNull(currentIndex + 1)

    val topStickColor = when {
        previousWeek == null -> Transparent
        previousWeek.size < 5 || currentWeek.isEmpty() -> grey94
        else -> black25
    }
    val bottomStickColor = when {
        nextWeek == null -> Transparent
        nextWeek.isEmpty() -> grey94
        else -> black25
    }
    val shouldHideCheckMark = (isFirst || isLast) || weeks[currentIndex].isNotEmpty()
    val isCompleted = weeks[currentIndex].size == 5

    return WeeklyProgressCheckPoints(
        topStickColor = topStickColor,
        bottomStickColor = bottomStickColor,
        isCompleted = isCompleted && shouldHideCheckMark,
        shouldHideCheckMark = shouldHideCheckMark
    )
}


data class WeeklyProgressCheckPoints(
    val topStickColor: Color,
    val bottomStickColor: Color,
    val isCompleted: Boolean,
    val shouldHideCheckMark: Boolean
)


@Composable
private fun WeeklyProgressItem(
    week: Int,
    completedDays: List<Int>,
    weeklyProgressCheckPoints: WeeklyProgressCheckPoints,
    modifier: Modifier = Modifier
) {

    HStack(
        spaceBy = 8.dp,
        modifier = modifier
            .height(150.dp)
    ) {

        weeklyProgressCheckPoints.apply {
            CheckMark(
                topStickColor = topStickColor,
                bottomStickColor = bottomStickColor,
                shouldHideCheckMark = shouldHideCheckMark,
                isCompleted = isCompleted
            )
        }

        Box(
            modifier = Modifier
                .weight(5f)
                .padding(8.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12))
                .background(
                    brush =
                    Brush.verticalGradient(
                        listOf(aliceBlue.copy(.6f), aliceBlue)
                    ),
                    RoundedCornerShape(12)
                ),
            contentAlignment = Alignment.Center
        ) {

            HStack(12.dp) {
                Spacer(Modifier.padding(4.dp))
                WeekIndicator(week)
                VerticalDivider(
                    thickness = 1.dp,
                    color = grey94,
                    modifier = Modifier.fillMaxHeight()
                )

                ProgressGrid(
                    daysList = completedDays,
                    modifier = Modifier.weight(1f)
                )
            }

        }
    }
}


@Composable
private fun CheckMark(
    modifier: Modifier = Modifier,
    topStickColor: Color = black25,
    bottomStickColor: Color = black25,
    shouldHideCheckMark: Boolean = false,
    isCompleted: Boolean = true,
) {
    VStack(
        spaceBy = 0.dp,
        modifier = modifier.width(18.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .width(2.dp)
                .background(topStickColor)
        )

        if (shouldHideCheckMark)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .border(1.dp, black25, shape = CircleShape)
                    .paint(
                        painter = painterResource(R.drawable.completed),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        colorFilter = ColorFilter.tint(if (isCompleted) black25 else grey94)
                    )
            )

        Box(
            modifier = Modifier
                .weight(1f)
                .width(2.dp)
                .background(bottomStickColor)
        )
    }
}


@Composable
private fun WeekIndicator(
    week: Int,
    modifier: Modifier = Modifier
) {

    VStack(
        spaceBy = 8.dp,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        "WEEK".forEach { char ->
            Text(
                text = "$char",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                lineHeight = 12.sp,
                modifier = Modifier
            )
        }

        Spacer(Modifier.padding(4.dp))
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(25))
                .border(1.dp, Color.Black, shape = RoundedCornerShape(25))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(25))
                    .background(
                        color = black25,
                        shape = RoundedCornerShape(25)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$week",
                    fontFamily = outFit,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    lineHeight = 1.sp,
                    color = white
                )
            }
        }
    }
}

//@Preview(showBackground = true)
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgressGrid(
    daysList: List<Int>,
    modifier: Modifier = Modifier
) {

    val numberOfCompletedDays = daysList.size

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
            .height(118.dp),
        maxItemsInEachRow = 4,
        maxLines = 3,
        horizontalArrangement = Arrangement.SpaceEvenly, // Distributes items evenly
        verticalArrangement = Arrangement.SpaceAround
    ) {
        repeat(8) { index ->
            Cell(
                day = index + 1,
                isEnable = isEnable(index, numberOfCompletedDays),
                isStickVisible = (index + 1) % 4 != 0,
                cellTYPE = getCellType(daysList.indices, index),
                modifier = Modifier.weight(1f)
            ) { day ->
                //TODO:Implement Cell Click
            }
        }
    }
}

private fun getCellType(indices: IntRange, index: Int): CellType {
    return when (index) {
        in indices -> CellType.DAY_COMPLETED
        in 5..6 -> CellType.REST_DAY
        7 -> CellType.WEEK_COMPLETED
        else -> CellType.DAY_NUMBER
    }
}

private fun isEnable(index: Int, numberOfCompletedDays: Int): Boolean {
    return index < numberOfCompletedDays || numberOfCompletedDays == 5
}

//@Preview(showBackground = true)
@Composable
private fun Cell(
    day: Int = 1,
    isStickVisible: Boolean = CellDefaults.STICK_VISIBLE,
    cornerRadius: Int = CellDefaults.CORNER_RADIUS,
    isEnable: Boolean = false,
    cellSize: Dp = CellDefaults.cellSize,
    cellTYPE: CellType = CellDefaults.cellType,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit
) {

    val color = if (isEnable) CellDefaults.EnableColor else CellDefaults.DisableColor

    HStack(
        0.dp,
        modifier = modifier
    ) {

        Box(
            modifier = Modifier
                .size(cellSize)
                .clip(RoundedCornerShape(cornerRadius))
                .border(
                    width = 1.dp,
                    color = color,
                    shape = RoundedCornerShape(cornerRadius)
                )
                .clickable { onClick(day) },
            contentAlignment = Alignment.Center
        ) {

            when (cellTYPE) {
                CellType.DAY_COMPLETED -> {
                    Image(
                        painter = painterResource(R.drawable.completed),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(black25)
                    )
                }

                CellType.REST_DAY -> {
                    Text(
                        text = "REST\nDAY",
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.W900,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }

                CellType.WEEK_COMPLETED -> {

                    val weekBackgroundColor = if (isEnable) black25 else Transparent

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(weekBackgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.completed),
                            contentDescription = null,
                            colorFilter = if (isEnable)
                                null
                            else ColorFilter.tint(grey94)
                        )
                    }
                }

                CellType.DAY_NUMBER -> {
                    Text(
                        text = day.toString(),
                        color = color,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.W900,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                    )
                }
            }
        }

        if (isStickVisible) {
            HorizontalDivider(
                thickness = 1.dp,
                color = color,
                modifier = Modifier
                    .weight(1f)
                    .size(2.dp)
            )
        }

    }
}


object CellDefaults {
    val cellSize: Dp = 48.dp
    const val CORNER_RADIUS: Int = 25
    val EnableColor: Color = black25
    val DisableColor: Color = grey94
    val cellType: CellType = CellType.WEEK_COMPLETED
    const val STICK_VISIBLE: Boolean = true
}

enum class CellType {
    DAY_NUMBER,
    DAY_COMPLETED,
    REST_DAY,
    WEEK_COMPLETED,
}

