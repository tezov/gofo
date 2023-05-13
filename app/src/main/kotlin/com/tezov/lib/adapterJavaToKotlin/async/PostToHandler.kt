package com.tezov.lib.adapterJavaToKotlin.async

import android.view.View
import com.tezov.lib_java_android.toolbox.PostToHandler
import com.tezov.lib_java_android.ui.activity.ActivityBase
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW
import java.util.concurrent.TimeUnit

object PostToHandler{
    fun of(activity: ActivityBase, init: RunnableW.() -> Unit): Boolean {
        return of(activity, 0, TimeUnit.MILLISECONDS, init)
    }
    fun of(activity: ActivityBase, delay: Long, init: RunnableW.() -> Unit): Boolean {
        return of(activity, delay, TimeUnit.MILLISECONDS, init)
    }
    fun of(activity: ActivityBase, delay: Long, timeUnit: TimeUnit?, init: RunnableW.() -> Unit): Boolean {
        return PostToHandler.of(activity.viewRoot, delay, timeUnit, RunnableW(init))
    }
    fun of(view: View?, init: RunnableW.() -> Unit): Boolean {
        return of(view, 0, TimeUnit.MILLISECONDS, init)
    }
    fun of(view: View?, delay: Long, init: RunnableW.() -> Unit): Boolean {
        return of(view, delay, TimeUnit.MILLISECONDS, init)
    }
    fun of(view: View?, delay: Long, timeUnit: TimeUnit?, init: RunnableW.() -> Unit): Boolean {
        return PostToHandler.of(view, delay, timeUnit, RunnableW(init))
    }
}