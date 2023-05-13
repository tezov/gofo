package com.tezov.lib.adapterJavaToKotlin.async

import kotlinx.coroutines.Deferred

interface defProviderAsync_K<T:Any> {
    fun getFirstIndex(): Deferred<Int?>
    fun getLastIndex(): Deferred<Int?>
    fun size(): Deferred<Int>
    fun get(index:Int): Deferred<T?>
    fun select(offset: Int,length: Int): Deferred<List<T>?>
    fun indexOf(t: T): Deferred<Int?>
    fun toDebugLog(pageStart:Int? = null, pageEnd:Int? = null): Deferred<Unit>

}