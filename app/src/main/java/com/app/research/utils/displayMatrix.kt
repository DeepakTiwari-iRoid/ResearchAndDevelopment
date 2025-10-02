package com.app.research.utils

import android.content.Context

fun Int.dpToPx(ctx: Context): Int = (this * ctx.resources.displayMetrics.density).toInt()
fun Int.pxToDp(ctx: Context): Int = (this / ctx.resources.displayMetrics.density).toInt()