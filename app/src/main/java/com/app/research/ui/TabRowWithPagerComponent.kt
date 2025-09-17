package com.app.research.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun TabRowWithPagerComponent(
    modifier: Modifier = Modifier,
    tabItems: List<String> = listOf("firstTab", "secondTab"),
    pageContent: @Composable PagerScope.(page: Int) -> Unit = { }
) {

    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState {
        tabItems.size
    }

    Column(modifier = modifier) {

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = White,
            indicator = { tabPositions ->
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = Color.Black
                )
            },
        ) {

            tabItems.forEachIndexed { index, item ->

                val selected = pagerState.currentPage == index
                val weightAndColor = if (selected) Pair(FontWeight.SemiBold, Black) else Pair(
                    FontWeight.Normal,
                    Black.copy(alpha = 0.5f)
                )

                Tab(
                    selected = selected,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = item,
                            fontFamily = FontFamily.Serif,
                            fontWeight = weightAndColor.first,
                            color = weightAndColor.second,
                            fontSize = 15.sp
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageContent = pageContent
        )
    }

}