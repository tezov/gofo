package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.async.notifier.observer.event.ObserverEventE

class ObserverEventE<EVENT:Any, VALUE:Any>(val owner:Any, val event:EVENT?) {
    companion object{
        inline operator fun <EVENT:Any, VALUE:Any> invoke(owner:Any, event:EVENT? = null, init: com.tezov.lib.adapterJavaToKotlin.observer.ObserverEventE<EVENT, VALUE>.() -> Unit): ObserverEventE<EVENT, VALUE> {
            return com.tezov.lib.adapterJavaToKotlin.observer.ObserverEventE<EVENT, VALUE>(
                owner,
                event
            ).apply {
                init()
            }.build()
        }
    }
    private lateinit var blockComplete: ObserverEventE<EVENT, VALUE>.(EVENT?, VALUE?) -> Unit
    private lateinit var blockException: ObserverEventE<EVENT, VALUE>.(EVENT?, Throwable) -> Unit
    private lateinit var blockCancel: ObserverEventE<EVENT, VALUE>.(EVENT?) -> Unit
    fun onComplete(block: ObserverEventE<EVENT, VALUE>.(EVENT?, VALUE?) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverEventE<EVENT, VALUE> {
        blockComplete = block
        return this
    }
    fun onException(block: ObserverEventE<EVENT, VALUE>.(EVENT?, Throwable) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverEventE<EVENT, VALUE> {
        blockException = block
        return this
    }
    fun onCancel(block: ObserverEventE<EVENT, VALUE>.(EVENT?) -> Unit): com.tezov.lib.adapterJavaToKotlin.observer.ObserverEventE<EVENT, VALUE> {
        blockCancel = block
        return this
    }
    fun build(): ObserverEventE<EVENT, VALUE> {
        if(!this::blockComplete.isInitialized){
/*#-debug-> DebugException.start().log("call onComplete is mandatory").end() <-debug-#*/
        }
        if(!this::blockException.isInitialized){
/*#-debug-> DebugException.start().log("call onException is mandatory").end() <-debug-#*/
        }
        return object : ObserverEventE<EVENT, VALUE>(owner, event) {
            override fun onComplete(event: EVENT?, value: VALUE?) {
                blockComplete(this, event, value)
            }
            override fun onException(event: EVENT?, value: VALUE?, e: Throwable) {
                blockException(this, event, e)
            }
            override fun onCancel(event: EVENT) {
                blockCancel(this, event)
            }
        }
    }
}