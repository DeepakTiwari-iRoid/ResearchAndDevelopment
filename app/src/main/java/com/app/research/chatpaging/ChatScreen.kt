package com.app.research.chatpaging

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.app.research.singlescreen_r_d.skaifitness.FullSizeCenterBox
import com.app.research.singlescreen_r_d.skaifitness.HStack
import com.app.research.ui.handleLazyFooter
import com.app.research.ui.theme.Black23
import com.app.research.ui.theme.Black60
import com.app.research.ui.theme.GreyF2
import com.app.research.ui.theme.primaryGreen

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = ChatViewModel(),
) {

    val context = LocalContext.current

    val remoteMessages = viewModel.remoteChatHistory.collectAsLazyPagingItems()
    val localMessages by viewModel.localChatHistory.collectAsStateWithLifecycle()

    var message by remember { mutableStateOf("") }


    Scaffold(
        modifier = modifier,
        containerColor = White,
        topBar = {
            HStack(0.dp, modifier = Modifier.statusBarsPadding()) {
                IconButton(
                    onClick = {
                        (context as Activity).finish()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        bottomBar = {
            HStack(
                spaceBy = 0.dp,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .navigationBarsPadding()
            ) {
                TextField(
                    value = message,
                    onValueChange = {
                        message = it
                    },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        viewModel.event(ChatEvent.SendMessage(message))
                        message = "" // Clear the input field after sending
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Send"
                    )
                }
            }
        }
    ) { innerPadding ->
        ChatScreenContent(
            remoteMessages = remoteMessages,
            localMessage = localMessages,
            modifier = Modifier.padding(innerPadding)
        )
    }

}

@Composable
fun ChatScreenContent(
    modifier: Modifier = Modifier,
    remoteMessages: LazyPagingItems<GroupedChatItem>,
    localMessage: List<GroupedChatItem>
) {

    var shouldShowCurrentDate by remember { mutableStateOf(true) }

    val state = rememberLazyListState()

    when (val remote = remoteMessages.loadState.refresh) {

        is LoadState.Error -> {
            FullSizeCenterBox {
                Text("${remote.error.localizedMessage}")
            }
        }

        LoadState.Loading -> {
            FullSizeCenterBox {
                CircularProgressIndicator(
                    strokeWidth = 6.dp,
                    color = primaryGreen,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        is LoadState.NotLoading -> {

            LazyColumn(
                state = state,
                reverseLayout = true,
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {


                items(
                    count = localMessage.size,
                ) { index ->

                    val chat = localMessage[(localMessage.size - 1) - index]

                    when (chat) {
                        is GroupedChatItem.DateHeaderItem -> {
                            if (!shouldShowCurrentDate) return@items
                            FullSizeCenterBox {
                                Text(chat.date)
                            }
                        }

                        is GroupedChatItem.Message -> {
                            MessageItem(item = chat.chat)
                        }
                    }
                }

                items(
                    count = remoteMessages.itemCount,
                ) { index ->

                    val rMessage =
                        remoteMessages[(remoteMessages.itemCount - 1) - index] ?: return@items

                    when (rMessage) {
                        is GroupedChatItem.DateHeaderItem -> {
                            FullSizeCenterBox {
                                Text(rMessage.date)

                                //Checking if remote message contain Today's date so we can avoid duplicate
                                if (rMessage.date == "07 Aug, 2025" && !shouldShowCurrentDate) shouldShowCurrentDate =
                                    false
                            }
                        }

                        is GroupedChatItem.Message -> {
                            MessageItem(item = rMessage.chat)
                        }
                    }
                }

                handleLazyFooter(remoteMessages)

            }
        }
    }

    LaunchedEffect(localMessage.size) {
        state.scrollToItem(0)
    }
}


@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    item: Chat
) {

    val color = if (item.isFromMe) Pair(primaryGreen, White) else Pair(GreyF2, Black)
    val alignment = if (item.isFromMe) Alignment.End else Alignment.Start

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 18.dp, start = 18.dp)
    ) {

        Text(
            text = item.author,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Black23
            ),
            modifier = Modifier.align(alignment)
        )

        Spacer(modifier = Modifier.padding(top = 10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(alignment)
        ) {

            if (item.isFromMe) {
                Text(
                    text = item.time,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = Black60
                    ),
                )
            }

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (item.isFromMe) 12.dp else 0.dp,
                            bottomEnd = if (item.isFromMe) 0.dp else 12.dp
                        )
                    )
                    .background(color.first)
                    .padding(16.dp)
            ) {

                Text(
                    text = item.message,
                    color = color.second,
                    modifier = Modifier.widthIn(max = 230.dp)
                )

            }

            if (!item.isFromMe) {
                Text(
                    text = item.time,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = Black60
                    )
                )
            }

        }
    }
}
