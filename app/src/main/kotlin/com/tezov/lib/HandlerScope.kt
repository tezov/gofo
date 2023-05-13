package com.tezov.lib

import android.os.Looper
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.debug.annotation.DebugLogEnable
import kotlinx.coroutines.*
import java.lang.Runnable


@DebugLogEnable(false)
object HandlerScope {

    fun newDispatcher(name:String):CoroutineDispatcher{
        return ExecutorThread(name).asCoroutineDispatcher()
    }
    fun newScope(name:String):CoroutineScope{
        return CoroutineScope(newDispatcher(name)) + CoroutineName(name)
    }

    fun newDispatcher(handler:Handler):CoroutineDispatcher{
        return ExecutorHandler(handler).asCoroutineDispatcher()
    }
    fun newScope(handler:Handler):CoroutineScope{
        return CoroutineScope(newDispatcher(handler)) + CoroutineName(handler.name)
    }

    val MAIN:CoroutineScope = newScope(Handler.MAIN())
    val PRIMARY:CoroutineScope = newScope(Handler.PRIMARY())
    val SECONDARY:CoroutineScope = newScope(Handler.SECONDARY())
    val LOW: CoroutineScope by lazy { newScope(Handler.LOW()) }

    fun currentOrNull(): CoroutineScope? {
        val myLooper = Looper.myLooper()
        if (myLooper == Handler.MAIN().looper) {
            return MAIN
        }
        if (myLooper == Handler.PRIMARY().looper) {
            return PRIMARY
        }
        if (myLooper == Handler.SECONDARY().looper) {
            return SECONDARY
        }
        return null
    }
    fun currentOrMain(): CoroutineScope {
        return currentOrNull() ?: MAIN
    }
    fun currentOrPrimary(): CoroutineScope {
        return currentOrNull() ?: PRIMARY
    }
    fun currentOrSecondary(): CoroutineScope {
        return currentOrNull() ?: SECONDARY
    }
    fun currentOrFromMyLooper(): CoroutineScope {
        return currentOrNull() ?: newScope(Handler.fromMyLooper())
    }
    fun fromMyLooper(): CoroutineScope {
        return newScope(Handler.fromMyLooper())
    }

    private class ExecutorThread(name:String) : java.util.concurrent.Executor{
        private val handler:Handler = Handler.newHandler(name)
        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }
    private class ExecutorHandler(val handler:Handler) : java.util.concurrent.Executor{
        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }

}