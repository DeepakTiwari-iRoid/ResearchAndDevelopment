package com.app.research.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.app.research.R
import com.app.research.singlescreen_r_d.skaifitness.FullSizeCenterBox
import com.app.research.singlescreen_r_d.skaifitness.VStack
import com.app.research.ui.theme.black25
import com.app.research.ui.theme.primaryGreen
import com.app.research.ui.theme.white


@Preview
@Composable
fun DialogLoader(
    color: Color = primaryGreen,
    onBackNavigation: () -> Unit = {},
    @DrawableRes icon: Int = R.drawable.ic_launcher_background
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onBackNavigation,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.5f),
            color = white,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = "loading",
                )

                LinearProgressIndicator(
                    color = color,
                    trackColor = Transparent,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}


@Preview
@Composable
fun CircularProgress(
    modifier: Modifier = Modifier,
    color: Color = primaryGreen,
    strokeWidth: Float = 8f
) {

    FullSizeCenterBox(modifier = modifier) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = strokeWidth.dp,
            trackColor = Transparent,
            strokeCap = StrokeCap.Round,
        )
    }
}


@Preview
@Composable
fun LinearLoader(
    modifier: Modifier = Modifier,
    title: String = "Loading...",
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = modifier,
            color = primaryGreen,
        ) {
            VStack(
                spaceBy = 12.dp,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = buildAnnotatedString {
                        val sTyle = SpanStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                        )
                        withStyle(style = sTyle) {
                            append(title)
                        }
                        withStyle(style = sTyle.copy(fontSize = 8.sp)) {
                            append("\nPlease Wait")
                        }
                    },
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                LinearProgressIndicator(
                    color = Color.White,
                    strokeCap = StrokeCap.Square,
                    trackColor = Color.Transparent,
                    modifier = Modifier
                        .width(150.dp)
                        .height(2.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CircularLoader(modifier: Modifier = Modifier) {
    FullSizeCenterBox(modifier = modifier.fillMaxWidth()) {
        CircularProgressIndicator(
            trackColor = Color.Transparent,
            color = black25,
            strokeWidth = 5.dp,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.size(48.dp)
        )
    }
}


@Composable
fun CrossFadeLoader(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Crossfade(
        modifier = modifier,
        targetState = isLoading,
    ) {
        if (it) CircularProgress()
        else content()
    }

}