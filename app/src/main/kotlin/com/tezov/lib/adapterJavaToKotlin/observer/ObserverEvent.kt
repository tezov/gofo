package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.async.notifier.observer.event.ObserverEvent

class ObserverEvent<EVENT:Any, VALUE:Any>(val owner:Any, val event:EVENT?) {
    companion object{
        inline operator fun <EVENT:Any, VALUE:Any> invoke(owner:Any, event:EVENT? = null, init: com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent<EVENT, VALUE>.() -> Unit): ObserverEvent<EVENT, VALUE> {
            return com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent<EVENT, VALUE>(
                owner,
                event
            ).apply {
                init()
            }.build()
        }
    }
    private lateinit var blockComplete: ObserverEvent<EVENT, VALUE>.(EVENT?, VALUE?) -> Unit
    private lateinit var blockCancel: ObserverEvent<EVENT, VALUE>.(EVENT?) -> Unit
    fun onComplete(block: ObserverEvent<EVENT, VALUE>.(EVENT?, VALUE?) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent<EVENT, VALUE> {
        blockComplete = block
        return this
    }
    fun onCancel(block: ObserverEvent<EVENT, VALUE>.(EVENT?) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent<EVENT, VALUE> {
        blockCancel = block
        return this
    }
    fun build(): ObserverEvent<EVENT, VALUE> {
        if(!this::blockComplete.isInitialized){
/*#-debug-> DebugException.start().log("call onComplete is mandatory").end() <-debug-#*/
        }
        return object : ObserverEvent<EVENT, VALUE>(owner, event) {
            override fun onComplete(event: EVENT?, value: VALUE?) {
                blockComplete(this, event, value)
            }
            override fun onCancel(event: EVENT) {
                blockCancel(this, event)
            }
        }
    }
}