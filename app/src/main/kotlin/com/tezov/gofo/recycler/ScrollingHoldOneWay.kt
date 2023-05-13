package com.tezov.gofo.recycler

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View

class ScrollingHoldOneWay: View.OnTouchListener {
    companion object{
        const val MIN_DISTANCE_TO_LOCK_DIRECTION = 250.0f
    }

    private var direction:Boolean? = null
    private var originY:Float? = null
    private var previousY:Float = 0F
    private var offsetY:Float = 0F

    private fun computeLock(position:Float){
        if(direction  == null){
            val diffOrigin = position - originY!!
            if(Math.abs(diffOrigin) >  MIN_DISTANCE_TO_LOCK_DIRECTION){
                direction = diffOrigin > 0
            }
        }
    }
    private fun alterMotionEvent(event: MotionEvent):MotionEvent{
        val altered:MotionEvent
        if(direction == null){
            altered = event
        }
        else{
            val diff = event.y - previousY
            if(((direction == false) && (diff>0)) || ((direction == true)&&(diff<0))){
                offsetY += diff
            }
            altered = MotionEvent.obtain(event.downTime, event.eventTime, event.action,
                event.x, event.y - offsetY, event.metaState)
        }
        previousY = event.y
        return altered
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if(event.action == MotionEvent.ACTION_UP){
            originY = null
            return false
        }
        if(originY == null){
            direction = null
            originY = event.y
            previousY = event.y
            offsetY = 0F
        }
        computeLock(event.y)
        view.onTouchEvent(alterMotionEvent(event))
        return true
    }

}