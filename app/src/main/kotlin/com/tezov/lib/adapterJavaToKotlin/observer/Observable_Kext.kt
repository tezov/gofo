package com.tezov.lib.adapterJavaToKotlin.observer

import com.tezov.lib_java.async.notifier.Notifier
import com.tezov.lib_java.async.notifier.task.TaskState
import com.tezov.lib_java.async.notifier.task.TaskValue

object Observable_Kext {
    inline fun <T:Any> TaskValue<T>.Observable.observeE(owner:Any, init: ObserverValueE<T>.() -> Unit): Notifier.Subscription {
        return this.observe(ObserverValueE(owner, init))
    }
    inline fun <T:Any> TaskValue<T>.Observable.observe(owner:Any, init: ObserverValue<T>.() -> Unit): Notifier.Subscription {
        return this.observe(ObserverValue(owner, init))
    }
    inline fun TaskState.Observable.observeE(owner:Any, init: ObserverStateE.() -> Unit): Notifier.Subscription {
        return this.observe(ObserverStateE(owner, init))
    }
    inline fun TaskState.Observable.observe(owner:Any, init: ObserverState.() -> Unit): Notifier.Subscription {
        return this.observe(ObserverState(owner, init))
    }
}