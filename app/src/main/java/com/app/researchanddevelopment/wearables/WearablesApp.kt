package com.app.researchanddevelopment.wearables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Preview(showBackground = true)
@Composable
private fun WearableScreenPreview() {
    WearableScreen(
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
fun WearableScreen(modifier: Modifier = Modifier) {

    WearableContent(
        steps = "",
        maxBmx = "",
        minBmx = "",
        modifier = modifier
    )
}


@Composable
fun WearableContent(
    steps: String,
    minBmx: String,
    maxBmx: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "Steps: $steps")
        Text(text = "BMX: min $minBmx max $maxBmx")

        Spacer(modifier = Modifier.padding(18.dp))

        Button(
            onClick = {},
            shape = RoundedCornerShape(12)
        ) {
            Text("Fetch From Wearable")
        }
    }
}