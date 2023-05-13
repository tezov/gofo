package com.tezov.lib

import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.debug.annotation.DebugLogEnable

@DebugLogEnable(false)
object SpySynchronized {
    var lastId:Int = 0
    fun nextId():Int{
        synchronized(this){
            return ++lastId
        }
    }

    var counter:Int = 0
    fun start(){
        synchronized(this){
/*#-debug-> DebugLog.start().send(myClass(), "${++counter}++").end() <-debug-#*/
        }
    }
    fun end(){
        synchronized(this){
/*#-debug-> DebugLog.start().send(myClass(), "${--counter}--").end() <-debug-#*/
        }
    }

    fun myClass():Class<SpySynchronized>{
        return SpySynchronized::class.java
    }

    operator inline fun <R> invoke(lock: Any, block: () -> R): R {
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}:" + Thread.getName()).end() <-debug-#*/
        val value = synchronized(lock, block)
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}:" + Thread.getName()).end() <-debug-#*/
        end()
        return value
    }

}