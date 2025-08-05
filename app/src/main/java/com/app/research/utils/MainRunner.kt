package com.app.research.utils

import com.app.research.slidingTransition.verticalHorizontalPager.ParentData

val viewPagerData: List<ParentData> = List(10) { index ->
    ParentData(
        id = "Parent $index",
        children = List(5) { childIndex ->
            ParentData.ChildData(
                id = "Child $childIndex of Parent $index",
                cUrl = "https://example.com/parent$index/child$childIndex"
            )
        }
    )
}


fun main() {

    viewPagerData.take(1).forEach { item ->
        println(item)
    }

}

