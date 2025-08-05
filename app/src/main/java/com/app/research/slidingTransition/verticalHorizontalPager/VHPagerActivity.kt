package com.app.research.slidingTransition.verticalHorizontalPager

import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.viewpager2.widget.ViewPager2
import com.app.research.R
import com.app.research.slidingTransition.verticalHorizontalPager.adapter.ParentAdapter

class VHPagerActivity : AppCompatActivity() {
    private lateinit var verticalPager: ViewPager2

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_vertical_horizontal_pager)

        verticalPager = findViewById(R.id.vertical_view_pager)

        val dummyData = listOf(
            ParentData(
                id = "1",
                children = listOf(
                    ParentData.ChildData(
                        "1",
                        RawResourceDataSource.buildRawResourceUri(R.raw.map_line_shadow).toString()
                    ),
                    ParentData.ChildData(
                        "2",
                        RawResourceDataSource.buildRawResourceUri(R.raw.s_record).toString()
                    )
                )
            ),
            ParentData(
                id = "2",
                children = listOf(
                    ParentData.ChildData(
                        "3",
                        RawResourceDataSource.buildRawResourceUri(R.raw.s_record).toString()
                    ),
                    ParentData.ChildData(
                        "4",
                        RawResourceDataSource.buildRawResourceUri(R.raw.map_line_shadow).toString()
                    )
                )
            )
        )

        verticalPager.adapter = ParentAdapter(dummyData)
    }
}
