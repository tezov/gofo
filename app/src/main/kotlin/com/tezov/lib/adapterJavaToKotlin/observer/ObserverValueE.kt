package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.async.notifier.observer.value.ObserverValueE

class ObserverValueE<T:Any>(val owner:Any) {
    companion object{
        inline operator fun <T:Any> invoke(owner:Any, init: com.tezov.lib.adapterJavaToKotlin.observer.ObserverValueE<T>.() -> Unit): ObserverValueE<T> {
            return com.tezov.lib.adapterJavaToKotlin.observer.ObserverValueE<T>(owner).apply {
                init()
            }.build()
        }
    }
    private lateinit var blockComplete: ObserverValueE<T>.(T?) -> Unit
    private lateinit var blockException: ObserverValueE<T>.(T?, Throwable) -> Unit
    private lateinit var blockCancel: ObserverValueE<T>.() -> Unit
    fun onComplete(block: ObserverValueE<T>.(T?) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverValueE<T> {
        blockComplete = block
        return this
    }
    fun onException(block: ObserverValueE<T>.(T?, Throwable) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverValueE<T> {
        blockException = block
        return this
    }
    fun onCancel(block: ObserverValueE<T>.() -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverValueE<T> {
        blockCancel = block
        return this
    }
    fun build(): ObserverValueE<T> {
        if(!this::blockComplete.isInitialized){
/*#-debug-> DebugException.start().log("call onComplete is mandatory").end() <-debug-#*/
        }
        if(!this::blockException.isInitialized){
/*#-debug-> DebugException.start().log("call onException is mandatory").end() <-debug-#*/
        }
        return object : ObserverValueE<T>(owner) {
            override fun onComplete(t: T?) {
                blockComplete(this, t)
            }
            override fun onException(t: T?, e: Throwable) {
                blockException(this, t, e)
            }
            override fun onCancel() {
                blockCancel(this)
            }
        }
    }
}