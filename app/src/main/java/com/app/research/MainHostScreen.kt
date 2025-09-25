package com.app.research

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.research.data.Constants
import com.app.research.deeplink.DeepLinkActivity
import com.app.research.faceml.FaceMLActivity
import com.app.research.good_gps.ForGolfActivity
import com.app.research.health_connect.HealthConnectActivity
import com.app.research.singlescreen_r_d.skaifitness.HStack
import com.app.research.slidingTransition.FirstSlidingTransitionActivity
import com.app.research.slidingTransition.verticalHorizontalPager.VHPagerActivity
import com.app.research.utils.AppUtils.intent

@Preview(showBackground = true)
@Composable
fun MainHostScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = Color(0xFFFAF7F0),
        topBar = {
            HStack(
                horizontalArrangement = Arrangement.Center,
                spaceBy = 0.dp, modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Research And Development",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                )
            }
        }
    ) { innerPadding ->
        MainHostScreenContent(modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun MainHostScreenContent(
    modifier: Modifier = Modifier
) {

    val ctx = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = Screens.entries
        ) { screen ->
            Item(
                screenName = screen.name,
                modifier = Modifier
                    .fillMaxSize()
                    .heightIn(min = 128.dp)
            ) {
                navigateToScreen(
                    context = ctx,
                    screen = screen
                )
            }
        }
    }
}

@Composable
fun Item(
    modifier: Modifier = Modifier,
    screenName: String = "Item",
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18))
            .clickable(onClick = onClick)
            .background(color = Color(0xFFC0C9EE)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = screenName,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}


enum class Screens {
    ChatCustomPaging,
    ForGolfRND,
    SkaiRAndD,
    HealthConnect,
    DeepLink,
    FaceMl,
    ScreenSlidTransitionXML,
    VerticalHorizontalPagerXML,
}


fun navigateToScreen(
    context: Context,
    screen: Screens,
) {

    when (screen) {
        Screens.ForGolfRND -> intent(context, ForGolfActivity::class.java)
        Screens.FaceMl -> intent(context, FaceMLActivity::class.java)
        Screens.DeepLink -> intent(context, DeepLinkActivity::class.java)
        Screens.HealthConnect -> intent(context, HealthConnectActivity::class.java)
        Screens.ScreenSlidTransitionXML -> {
            intent(context, FirstSlidingTransitionActivity::class.java, false).apply {
                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity)
                context.startActivity(this, options.toBundle())
            }
        }

        Screens.VerticalHorizontalPagerXML -> intent(context, VHPagerActivity::class.java)

        Screens.ChatCustomPaging -> {
            val bundle = Bundle().apply {
                putString(Constants.KEYS.START_DEST, Constants.ComposeHostScreens.CHAT_SCREEN)
            }
            intent(context, ComposeHostActivity::class.java, bundle = bundle)
        }

        else -> {
            Toast.makeText(
                context,
                "Screen ${screen.name} is not implemented yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


}