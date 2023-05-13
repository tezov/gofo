package com.tezov.gofo.retrofit.unsplash.data

import com.tezov.lib_java.debug.DebugLog

import com.tezov.lib_java.util.UtilsString
import com.tezov.lib_java.toolbox.Clock
import java.util.LinkedList
import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.buffer.ByteBufferBuilder
import com.tezov.lib_java.debug.DebugString

abstract class DataBase {
    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
    }

    fun toBytes():ByteArray{
        val buffer = ByteBufferBuilder.obtain()
        toByteBuffer(buffer)
        return buffer.array()
    }
    abstract fun toByteBuffer(buffer: ByteBuffer)

    fun <D:DataBase>fromBytes(bytes:ByteArray): D {
        fromByteBuffer(ByteBuffer.wrap(bytes))
        return this as D
    }
    abstract fun fromByteBuffer(buffer: ByteBuffer)

    abstract fun toDebugString(): DebugString
    fun toDebugLog(){
/*#-debug-> DebugLog.start().send(toDebugString()).end(); <-debug-#*/
    }

    protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
    }
}