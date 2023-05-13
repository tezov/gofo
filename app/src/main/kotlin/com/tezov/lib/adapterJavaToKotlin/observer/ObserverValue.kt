package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.async.notifier.observer.value.ObserverValue

class ObserverValue<T:Any>(val owner:Any) {
    companion object{
        inline operator fun <T:Any> invoke(owner:Any, init: com.tezov.lib.adapterJavaToKotlin.observer.ObserverValue<T>.() -> Unit): ObserverValue<T> {
            return com.tezov.lib.adapterJavaToKotlin.observer.ObserverValue<T>(owner).apply {
                init()
            }.build()
        }
    }
    private lateinit var blockComplete: ObserverValue<T>.(T?) -> Unit
    private lateinit var blockCancel: ObserverValue<T>.() -> Unit
    fun onComplete(block: ObserverValue<T>.(T?) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverValue<T> {
        blockComplete = block
        return this
    }
    fun onCancel(block: ObserverValue<T>.() -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverValue<T> {
        blockCancel = block
        return this
    }
    fun build(): ObserverValue<T> {
        if(!this::blockComplete.isInitialized){
/*#-debug-> DebugException.start().log("call onComplete is mandatory").end() <-debug-#*/
        }
        return object : ObserverValue<T>(owner) {
            override fun onComplete(t: T?) {
                blockComplete(this, t)
            }
            override fun onCancel() {
                blockCancel(this)
            }
        }
    }
}