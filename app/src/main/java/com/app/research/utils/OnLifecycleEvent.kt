package com.app.research.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun OnLifecycleEvent(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit
) {

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            onEvent.invoke(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}