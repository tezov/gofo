package com.tezov.lib.adapterJavaToKotlin.async

import com.tezov.lib_java.async.Handler
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun

object Completable {

    fun CompletableDeferred<Unit>.notifyComplete(handler:Handler){
        notifyComplete(handler, Unit)
    }
    fun <T> CompletableDeferred<T>.notifyComplete(handler:Handler, value:T){
        handler.postRun(this){
            this@notifyComplete.complete(value)
        }
    }
    fun <T> CompletableDeferred<T>.notifyComplete(value:T){
        complete(value)
    }
    fun CompletableDeferred<Unit>.notifyComplete(){
        complete(Unit)
    }

    fun CompletableDeferred<*>.notifyException(handler:Handler, e:Throwable){
        handler.postRun(this){
            this@notifyException.completeExceptionally(e)
        }
    }
    fun CompletableDeferred<*>.notifyException(e:Throwable){
        completeExceptionally(e)
    }

    inline fun <T, D:Deferred<T>> D.onComplete(crossinline succeed: (D) -> Unit, crossinline failed: (Throwable) -> Unit){
        this.invokeOnCompletion {
            it?.let {
                failed.invoke(it)
            }?:let {
                succeed.invoke(this)
            }
        }
    }
    fun <T, D:Deferred<T>> D.onComplete(succeed: (D) -> Unit){
        this.invokeOnCompletion {
            succeed.invoke(this)
        }
    }

    fun <T> notifyComplete(handler:Handler, t:T):CompletableDeferred<T>{
        val deferred:CompletableDeferred<T> = CompletableDeferred()
        deferred.notifyComplete(handler, t)
        return deferred
    }
    fun <T> notifyComplete(t:T):CompletableDeferred<T>{
        val deferred:CompletableDeferred<T> = CompletableDeferred()
        deferred.notifyComplete(t)
        return deferred
    }
    fun notifyComplete(handler:Handler):CompletableDeferred<Unit>{
        val deferred:CompletableDeferred<Unit> = CompletableDeferred()
        deferred.notifyComplete(handler, Unit)
        return deferred
    }
    fun notifyComplete():CompletableDeferred<Unit>{
        val deferred:CompletableDeferred<Unit> = CompletableDeferred()
        deferred.notifyComplete(Unit)
        return deferred
    }

    fun <T> notifyException(handler:Handler, e:Exception):CompletableDeferred<T>{
        val deferred:CompletableDeferred<T> = CompletableDeferred()
        deferred.notifyException(handler, e)
        return deferred
    }
    fun <T> notifyException(e:Exception):CompletableDeferred<T>{
        val deferred:CompletableDeferred<T> = CompletableDeferred()
        deferred.notifyException(e)
        return deferred
    }

}