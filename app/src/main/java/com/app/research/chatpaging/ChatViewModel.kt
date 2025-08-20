package com.app.research.chatpaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import com.app.research.data.TempDataSource.chatList
import com.app.research.ui.transformToLazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    val remoteChatHistory: Flow<PagingData<GroupedChatItem>> =
        chatList.transformToLazyPagingItems().map { pagingData ->
            pagingData.map { chat ->
                GroupedChatItem.Message(chat)
            }.insertSeparators { before, after ->
                val beforeDate = before?.chat?.localTimeStamp
                val afterDate = after?.chat?.localTimeStamp

                when {
                    beforeDate != afterDate && afterDate != null -> {
                        GroupedChatItem.DateHeaderItem(afterDate)
                    }

                    else -> null
                }

            }
        }

    private val _localChatHistory = MutableStateFlow<List<Chat>>(emptyList())
    val localChatHistory: StateFlow<List<GroupedChatItem>> = _localChatHistory.map { chats ->
        chats
            .groupBy { it.localTimeStamp }
            .flatMap { (date, messages) ->
                buildList {
                    add(GroupedChatItem.DateHeaderItem(date))
                    addAll(messages.map { GroupedChatItem.Message(it) })
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    fun event(event: ChatEvent) {
        when (event) {
            is ChatEvent.SendMessage -> {
                val newChat = Chat(
                    chatId = System.currentTimeMillis().toInt(),
                    message = event.message,
                    isFromMe = true,
                    createdAt = System.currentTimeMillis(),
                )

                _localChatHistory.update { it + newChat }
            }
        }
    }
}


data class ChatUiState(
    val isLoading: Boolean = false,
    val message: String = "",
    val errorMessage: String? = null
)

sealed interface PagerUiState {
    object Ideal : PagerUiState
    object Refreshing {
        object Loading : PagerUiState
        data class Error(val message: String) : PagerUiState
    }

    object Appending {
        object Loading : PagerUiState
        data class Error(val message: String) : PagerUiState
    }

    data class Success(val data: List<Chat>) : PagerUiState
}

sealed interface ChatEvent {
    data class SendMessage(val message: String) : ChatEvent
}

sealed interface GroupedChatItem {
    data class DateHeaderItem(val date: String) : GroupedChatItem
    data class Message(val chat: Chat) : GroupedChatItem
}
