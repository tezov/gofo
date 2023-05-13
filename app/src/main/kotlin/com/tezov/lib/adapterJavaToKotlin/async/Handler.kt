package com.tezov.lib.adapterJavaToKotlin.async

import com.tezov.lib_java.async.Handler
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableW
import java.util.concurrent.TimeUnit

object Handler  {
    inline fun Handler.post(owner:Any, delay_ms:Long, unit:TimeUnit, init: RunnableW.() -> Unit) {
        this.post(owner, TimeUnit.MILLISECONDS.convert(delay_ms, unit), init)
    }
    inline fun Handler.post(owner:Any, delay_ms:Long = 0, init: RunnableW.() -> Unit) {
        this.post<com.tezov.lib_java.type.runnable.RunnableW>(owner, delay_ms, RunnableW(init))
    }

    fun Handler.postRun(owner:Any, delay_ms:Long, unit:TimeUnit, block: com.tezov.lib_java.type.runnable.RunnableW.() -> Unit) {
        this.postRun(owner, TimeUnit.MILLISECONDS.convert(delay_ms, unit), block)
    }
    fun Handler.postRun(owner:Any, delay_ms:Long = 0, block: com.tezov.lib_java.type.runnable.RunnableW.() -> Unit) {
        this.post<com.tezov.lib_java.type.runnable.RunnableW>(owner, delay_ms,
            RunnableW {runSafe(block)})
    }

}

