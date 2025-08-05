package com.app.research.slidingTransition

import android.transition.Slide
import android.view.Gravity
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.app.research.R

class SecondSlidingTransitionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            enterTransition = Slide(Gravity.END)   // slide in from right
            exitTransition = Slide(Gravity.END)   // slide out to right
        }
        setContentView(R.layout.activity_sliding_transition_second)
    }
}