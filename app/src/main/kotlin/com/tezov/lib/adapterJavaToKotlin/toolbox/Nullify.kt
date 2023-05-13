package com.tezov.lib.adapterJavaToKotlin.toolbox

import com.tezov.lib_java.toolbox.Nullify

object Nullify {

    fun String?.nullify():String?{
        return Nullify.string(this)
    }
    fun CharSequence?.nullify():String?{
        return Nullify.string(this)
    }
    fun <K, V, M: Map<K, V>> M?.nullify():M?{
        return Nullify.map(this)
    }

    fun <T> Collection<T>?.nullify():Collection<T>?{
        return Nullify.collection(this)
    }

    fun ByteArray?.nullify():ByteArray?{
        return Nullify.array(this)
    }
    fun CharArray?.nullify():CharArray?{
        return Nullify.array(this)
    }

}