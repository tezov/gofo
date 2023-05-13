package com.tezov.lib.adapterJavaToKotlin.runnable


import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.lib_java.type.runnable.RunnableGroup

class RunnableGroupAction {
    companion object{
        inline operator fun invoke(init: RunnableGroupAction.() -> Unit): RunnableGroup.Action{
            return RunnableGroupAction().apply {
                init()
            }.build()
        }
    }

    private var blockBeforeRun: (RunnableGroup.Action.() -> Unit)? = null
    private var blockAfterRun: (RunnableGroup.Action.() -> Unit)? = null
    private lateinit var blockRunSafe: RunnableGroup.Action.() -> Unit
    private var blockOnException: (RunnableGroup.Action.(Throwable) -> Unit)? = null
    fun beforeRun(block: RunnableGroup.Action.() -> Unit): RunnableGroupAction {
        blockBeforeRun = block
        return this
    }
    fun afterRun(block: RunnableGroup.Action.() -> Unit): RunnableGroupAction {
        blockAfterRun = block
        return this
    }
    @Throws(Throwable::class)
    fun runSafe(block: RunnableGroup.Action.() -> Unit): RunnableGroupAction {
        blockRunSafe = block
        return this
    }
    fun onException(block: RunnableGroup.Action.(Throwable) -> Unit): RunnableGroupAction {
        blockOnException = block
        return this
    }
    fun build(): RunnableGroup.Action {
        if(!this::blockRunSafe.isInitialized){
/*#-debug-> DebugException.start().log("call runSafe is mandatory").end() <-debug-#*/
        }
        return object : RunnableGroup.Action(){
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