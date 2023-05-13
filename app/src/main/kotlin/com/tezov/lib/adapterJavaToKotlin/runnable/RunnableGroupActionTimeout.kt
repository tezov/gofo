package com.tezov.lib.adapterJavaToKotlin.runnable

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.lib_java.type.runnable.RunnableGroup

class RunnableGroupActionTimeout {
    companion object{
        inline operator fun invoke(init: RunnableGroupActionTimeout.() -> Unit): RunnableGroup.ActionTimeout{
            return RunnableGroupActionTimeout().apply {
                init()
            }.build()
        }
    }

    private var blockBeforeRun: (RunnableGroup.ActionTimeout.() -> Unit)? = null
    private var blockAfterRun: (RunnableGroup.ActionTimeout.() -> Unit)? = null
    private lateinit var blockRunSafe: RunnableGroup.ActionTimeout.() -> Unit
    private lateinit var blockOnTimeout: RunnableGroup.ActionTimeout.() -> Unit
    private var blockOnException: (RunnableGroup.ActionTimeout.(Throwable) -> Unit)? = null
    fun beforeRun(block: RunnableGroup.ActionTimeout.() -> Unit): RunnableGroupActionTimeout {
        blockBeforeRun = block
        return this
    }
    fun afterRun(block: RunnableGroup.ActionTimeout.() -> Unit): RunnableGroupActionTimeout {
        blockAfterRun = block
        return this
    }
    @Throws(Throwable::class)
    fun runSafe(block: RunnableGroup.ActionTimeout.() -> Unit): RunnableGroupActionTimeout {
        blockRunSafe = block
        return this
    }
    fun onTimeout(block: RunnableGroup.ActionTimeout.() -> Unit): RunnableGroupActionTimeout {
        blockOnTimeout = block
        return this
    }
    fun onException(block: RunnableGroup.ActionTimeout.(Throwable) -> Unit): RunnableGroupActionTimeout {
        blockOnException = block
        return this
    }
    fun build(): RunnableGroup.ActionTimeout {
        if(!this::blockRunSafe.isInitialized){
/*#-debug-> DebugException.start().log("call runSafe is mandatory").end() <-debug-#*/
        }
        if(!this::blockOnTimeout.isInitialized){
/*#-debug-> DebugException.start().log("call onTimeout is mandatory").end() <-debug-#*/
        }
        return object : RunnableGroup.ActionTimeout(){
            override fun beforeRun() {
                blockBeforeRun?.invoke(this)
            }
            override fun afterRun() {
                blockAfterRun?.invoke(this)
            }
            override fun runSafe() {
                blockRunSafe(this)
            }
            override fun onTimeOut() {
                blockOnTimeout(this)
            }
            override fun onException(e: Throwable) {
                blockOnException?.invoke(this, e)
            }
        }
    }
}