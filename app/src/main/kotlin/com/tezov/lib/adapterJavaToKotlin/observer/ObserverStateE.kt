package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.async.notifier.observer.state.ObserverStateE

class ObserverStateE(val owner:Any) {
    companion object{
        inline operator fun invoke(owner:Any, init: com.tezov.lib.adapterJavaToKotlin.observer.ObserverStateE.() -> Unit): ObserverStateE {
            return com.tezov.lib.adapterJavaToKotlin.observer.ObserverStateE(owner).apply {
                init()
            }.build()
        }
    }
    private lateinit var blockComplete: ObserverStateE.() -> Unit
    private lateinit var blockException: ObserverStateE.(Throwable) -> Unit
    private lateinit var blockCancel: ObserverStateE.() -> Unit
    fun onComplete(block: ObserverStateE.() -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverStateE {
        blockComplete = block
        return this
    }
    fun onException(block: ObserverStateE.(Throwable) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverStateE {
        blockException = block
        return this
    }
    fun onCancel(block: ObserverStateE.() -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverStateE {
        blockCancel = block
        return this
    }
    fun build(): ObserverStateE {
        if(!this::blockComplete.isInitialized){
/*#-debug-> DebugException.start().log("call onComplete is mandatory").end() <-debug-#*/
        }
        if(!this::blockException.isInitialized){
/*#-debug-> DebugException.start().log("call onException is mandatory").end() <-debug-#*/
        }
        return object : ObserverStateE(owner) {
            override fun onComplete() {
                blockComplete(this)
            }
            override fun onException(e: Throwable) {
                blockException(this, e)
            }
            override fun onCancel() {
                blockCancel(this)
            }
        }
    }
}