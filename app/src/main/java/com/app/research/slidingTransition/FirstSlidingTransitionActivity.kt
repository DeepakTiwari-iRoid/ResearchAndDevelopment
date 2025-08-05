package com.app.research.slidingTransition

import android.content.Intent
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import com.app.research.R
import kotlin.math.abs


class FirstSlidingTransitionActivity : AppCompatActivity() {

    private lateinit var gestureDetector: GestureDetectorCompat

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sliding_transition_first)
        gestureDetector = GestureDetectorCompat(this, SwipeGestureListener())
    }

    inner class SwipeGestureListener : GestureDetector.OnGestureListener {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100


        override fun onDown(p0: MotionEvent): Boolean {
            // This method must return true to indicate that the gesture is being handled
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            val diffX = e2.x - e1.x
            if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX < 0) {
                    // Swipe Left → Next Activity
                    startActivity(
                        Intent(
                            this@FirstSlidingTransitionActivity,
                            SecondSlidingTransitionActivity::class.java
                        )
                    )
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    return true
                }
            }
            return false
        }

        override fun onLongPress(p0: MotionEvent) {

        }

        override fun onScroll(
            p0: MotionEvent?,
            p1: MotionEvent,
            p2: Float,
            p3: Float
        ): Boolean {
            // This method can be used to handle scrolling gestures, if needed
            return false
        }

        override fun onShowPress(p0: MotionEvent) {

        }

        override fun onSingleTapUp(p0: MotionEvent): Boolean {
            // This method can be used to handle single tap gestures, if needed
            return false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

}