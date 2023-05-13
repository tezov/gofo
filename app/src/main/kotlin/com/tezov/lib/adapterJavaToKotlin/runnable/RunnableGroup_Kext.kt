package com.tezov.lib.adapterJavaToKotlin.runnable

import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib_java.type.runnable.RunnableW
import kotlinx.coroutines.CompletableDeferred

object RunnableGroup_Kext  {

    inline fun RunnableGroup.setOnStart(init: RunnableGroupAction.() -> Unit): RunnableGroup.Action{
        val action = RunnableGroupAction(init)
        this.add(action)
        return action
    }
    inline fun RunnableGroup.add(init: RunnableGroupAction.() -> Unit): RunnableGroup.Action{
        val action = RunnableGroupAction(init)
        this.add(action)
        return action
    }
    inline fun RunnableGroup.setOnDone(init: RunnableGroupAction.() -> Unit): RunnableGroup.Action{
        val action = RunnableGroupAction(init)
        this.add(action)
        return action
    }

    fun RunnableGroup.setOnStartRun(block: RunnableW.() -> Unit): RunnableGroup.Action{
        val action = RunnableGroupAction{runSafe(block)}
        this.setOnStart(action)
        return action
    }
    fun RunnableGroup.addRun(block: RunnableW.() -> Unit): RunnableGroup.Action{
        val action = RunnableGroupAction{runSafe(block)}
        this.add(action)
        return action
    }
    fun RunnableGroup.setOnDoneRun(block: RunnableW.() -> Unit): RunnableGroup.Action{
        val action = RunnableGroupAction{runSafe(block)}
        this.setOnDone(action)
        return action
    }

    inline fun RunnableGroup.setOnStartWithTimeout(init: RunnableGroupActionTimeout.() -> Unit): RunnableGroup.ActionTimeout{
        val action = RunnableGroupActionTimeout(init)
        this.add(action)
        return action
    }
    inline fun RunnableGroup.addWithTimeout(init: RunnableGroupActionTimeout.() -> Unit): RunnableGroup.ActionTimeout{
        val action = RunnableGroupActionTimeout(init)
        this.add(action)
        return action
    }
    inline fun RunnableGroup.setOnDoneWithTimeout(init: RunnableGroupActionTimeout.() -> Unit): RunnableGroup.ActionTimeout{
        val action = RunnableGroupActionTimeout(init)
        this.add(action)
        return action
    }

    fun RunnableGroup.setOnStartRunWithTimeout(block: RunnableW.() -> Unit): RunnableGroup.ActionTimeout{
        val action = RunnableGroupActionTimeout{runSafe(block)}
        this.setOnStart(action)
        return action
    }
    fun RunnableGroup.addRunWithTimeout(block: RunnableW.() -> Unit): RunnableGroup.ActionTimeout{
        val action = RunnableGroupActionTimeout{runSafe(block)}
        this.add(action)
        return action
    }
    fun RunnableGroup.setOnDoneRunWithTimeout(block: RunnableW.() -> Unit): RunnableGroup.ActionTimeout{
        val action = RunnableGroupActionTimeout{runSafe(block)}
        this.setOnDone(action)
        return action
    }

    fun <T> RunnableGroup.build(init: RunnableGroup.(deferred:CompletableDeferred<T>) -> Unit):CompletableDeferred<T>{
        val deferred:CompletableDeferred<T> = CompletableDeferred()
        this.init(deferred)
        return deferred
    }

}