package com.tezov.lib

import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.debug.annotation.DebugLogEnable
import com.tezov.lib_java.util.UtilsList.NULL_INDEX
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun CoroutineScope.getName():String{
    return coroutineContext.getName(DebugTrack.getFullSimpleName(javaClass))
}
fun CoroutineContext.getName(alternativeName:String = "no name"):String{
    return get(CoroutineName.Key)?.name?: kotlin.run {
        return@run alternativeName
    }
}
val Thread: Thread? = null
fun Thread?.getName():String{
    val name = java.lang.Thread.currentThread().name
    val indexOfUnderscore = name.indexOf("_")
    return if(indexOfUnderscore != NULL_INDEX ) {
        name.substring(0, indexOfUnderscore)
    }
    else {
        name
    }
}
fun Thread.toDebugLogName():Unit{
/*#-debug-> DebugLog.start().send(Thread.getName()).end() <-debug-#*/
}

@DebugLogEnable(false)
object SpyScope {
    var lastId:Int = 0
    fun nextId():Int{
        synchronized(this){
            return ++lastId
        }
    }

    var counter:Int = 0
    fun start(){
        synchronized(this){
//                DebugLog.start().send("${++counter}++").end()
        }
    }
    fun end(){
        synchronized(this){
//                DebugLog.start().send("${--counter}--").end()
        }
    }

    private fun myClass():Class<SpyScope>{
        return SpyScope::class.java
    }

    suspend fun <T> await(owner: Any, deferred: Deferred<T>):T{
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: await from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        val returnValue = deferred.await()
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: await from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        end()
        return returnValue
    }
    suspend fun <T> join(owner: Any, deferred: Deferred<T>){
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: join from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        deferred.join()
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: join from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        end()
    }
    suspend fun <T> join(owner: Any, job:Job){
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: join from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        job.join()
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: join from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        end()
    }

    fun <T> async(
        owner: Any,
        scope:CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> T
    ): Deferred<T> {
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: async from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName() + " to scope " + scope.getName()).end() <-debug-#*/
        val returnValue = scope.async(context, start, block)
        returnValue.invokeOnCompletion {
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: async from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName() + " to scope " + scope.getName()).end() <-debug-#*/
        }
        end()
        return returnValue
    }

    @Throws(InterruptedException::class)
    fun <T> runBlocking(owner: Any, context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> T): T {
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: runBlocking from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        val returnValue = kotlinx.coroutines.runBlocking(context, block)
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: runBlocking from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName()).end() <-debug-#*/
        end()
        return returnValue
    }
    fun launch(
        owner: Any,
        scope:CoroutineScope,
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: launch from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName() + " to scope " + scope.getName()).end() <-debug-#*/
        val returnValue = scope.launch(context, start, block)
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: launch from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName() + " to scope " + scope.getName()).end() <-debug-#*/
        end()
        return returnValue
    }
    suspend fun <T> withContext(
        owner: Any,
        scope: CoroutineScope,
        block: suspend CoroutineScope.() -> T
    ): T {
        return withContext(owner, scope.coroutineContext, block)
    }
    suspend fun <T> withContext(
        owner: Any,
        context: CoroutineContext,
        block: suspend CoroutineScope.() -> T
    ): T {
        val id = nextId()
        start()
/*#-debug-> DebugLog.start().send(myClass(), "START-${id}: withContext from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName() + " to scope " + context.getName()).end() <-debug-#*/
        val returnValue =  kotlinx.coroutines.withContext(context, block)
/*#-debug-> DebugLog.start().send(myClass(), "END-${id}: withContext from " + DebugTrack.getFullSimpleName(owner) + " on thread "+ Thread.getName() + " to scope " + context.getName()).end() <-debug-#*/
        end()
        return returnValue
    }




}