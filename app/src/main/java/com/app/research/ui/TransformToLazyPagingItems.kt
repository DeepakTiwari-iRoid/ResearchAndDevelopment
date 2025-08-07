package com.app.research.ui

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


inline fun <reified T : Any> List<T>.transformToLazyPagingItems(): Flow<PagingData<T>> {

    val loadState = LoadState.NotLoading(endOfPaginationReached = true)
    val loadStates = LoadStates(
        refresh = loadState,
        prepend = loadState,
        append = loadState
    )
    val pagingData = PagingData.from(
        data = this,
        sourceLoadStates = loadStates
    )
    return flowOf(pagingData)
}