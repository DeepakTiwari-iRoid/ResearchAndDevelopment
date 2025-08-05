package com.app.research.slidingTransition.verticalHorizontalPager

data class ParentData(
    val id: String,
    val children: List<ChildData>
) {
    data class ChildData(
        val id: String,
        val cUrl: String
    )
}