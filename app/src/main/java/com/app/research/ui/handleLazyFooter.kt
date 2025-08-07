package com.app.research.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.app.research.singlescreen_r_d.skaifitness.HStack

fun <T : Any> LazyListScope.handleLazyFooter(
    pagingItems: LazyPagingItems<T>,
) {
    when (val appending = pagingItems.loadState.append) {
        is LoadState.Loading -> {
            item {
                PagingLoadingComponent()
            }
        }

        is LoadState.Error -> {
            item {
                PagingRetryComponent(
                    errorMsg = appending.error.localizedMessage ?: "Unknown Error"
                ) {
                    pagingItems.retry()
                }
            }
        }

        is LoadState.NotLoading -> {}
    }
}


@Composable
fun PagingLoadingComponent(modifier: Modifier = Modifier) {
    HStack(spaceBy = 8.dp, modifier = modifier.fillMaxWidth()) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 8.dp,
            trackColor = Transparent,
            strokeCap = StrokeCap.Round,
        )

        Text(
            text = "Loading...",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}




@Composable
fun PagingRetryComponent(
    modifier: Modifier = Modifier,
    errorMsg: String,
    onRetry: () -> Unit
) {
    HStack(spaceBy = 8.dp, modifier = modifier.fillMaxWidth()) {
        TextButton(
            onClick = onRetry
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(errorMsg)
                    }

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(" Retry")
                    }
                },
            )
        }
    }
}