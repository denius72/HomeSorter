package com.denisu.homesorter.controller

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class GestureListener(context: Context, private val listener: OnGestureListener) :
    GestureDetector.SimpleOnGestureListener() {

    private val SWIPE_THRESHOLD = 100
    private val SWIPE_VELOCITY_THRESHOLD = 100

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(context, this)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val diffY = e1?.y?.let { e2?.y?.minus(it) } ?: 0F
        val diffX = e1?.x?.let { e2?.x?.minus(it) } ?: 0F
        if (abs(diffY) > abs(diffX) &&
            abs(diffY) > SWIPE_THRESHOLD &&
            abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
        ) {
            if (diffY > 0) {
                listener.onSwipeDown()
            } else {
                listener.onSwipeUp()
            }
            return true
        }
        return false
    }

    interface OnGestureListener {
        fun onSwipeUp()
        fun onSwipeDown()
    }
}
