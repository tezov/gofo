package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.async.notifier.observer.state.ObserverState

class ObserverState(val owner:Any) {
    companion object{
        inline operator fun invoke(owner:Any, init: com.tezov.lib.adapterJavaToKotlin.observer.ObserverState.() -> Unit): ObserverState {
            return com.tezov.lib.adapterJavaToKotlin.observer.ObserverState(owner).apply {
                init()
            }.build()
        }
    }
    private lateinit var blockComplete: ObserverState.() -> Unit
    private lateinit var blockCancel: ObserverState.() -> Unit
    fun onComplete(block: ObserverState.() -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverState {
        blockComplete = block
        return this
    }
    fun onCancel(block: ObserverState.() -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverState {
        blockCancel = block
        return this
    }
    fun build(): ObserverState {
        if(!this::blockComplete.isInitialized){
/*#-debug-> DebugException.start().log("call onComplete is mandatory").end() <-debug-#*/
        }
        return object : ObserverState(owner) {
            override fun onComplete() {
                blockComplete(this)
            }
            override fun onCancel() {
                blockCancel(this)
            }
        }
    }
}