package com.tezov.lib.adapterJavaToKotlin.runnable

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.lib_java.type.runnable.RunnableW

class RunnableW {
    companion object{
        inline operator fun invoke(init: com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW.() -> Unit): RunnableW {
            return com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW().apply {
                init()
            }.build()
        }
    }
    private var blockBeforeRun: (RunnableW.() -> Unit)? = null
    private var blockAfterRun: (RunnableW.() -> Unit)? = null
    private lateinit var blockRunSafe: RunnableW.() -> Unit
    private var blockOnException: (RunnableW.(Throwable) -> Unit)? = null
    fun beforeRun(block: RunnableW.() -> Unit): com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW {
        blockBeforeRun = block
        return this
    }
    fun afterRun(block: RunnableW.() -> Unit): com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW {
        blockAfterRun = block
        return this
    }
    @Throws(Throwable::class)
    fun runSafe(block: RunnableW.() -> Unit): com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW {
        blockRunSafe = block
        return this
    }
    fun onException(block: RunnableW.(Throwable) -> Unit): com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW {
        blockOnException = block
        return this
    }
    fun build(): RunnableW {
        if(!this::blockRunSafe.isInitialized){
/*#-debug-> DebugException.start().log("call runSafe is mandatory").end() <-debug-#*/
        }
        return object : RunnableW(){
            override fun beforeRun() {
                blockBeforeRun?.invoke(this)
            }
            override fun afterRun() {
                blockAfterRun?.invoke(this)
            }
            override fun runSafe() {
                blockRunSafe(this)
            }
            override fun onException(e: Throwable) {
                blockOnException?.invoke(this, e)
            }
        }
    }
}