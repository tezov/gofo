package com.tezov.gofo.recycler.photo

import com.tezov.lib_java.async.Handler
import com.tezov.lib_java_android.ui.recycler.RecyclerListDataManager
import kotlinx.coroutines.*
import com.tezov.gofo.application.AppHandler
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.lib.adapterJavaToKotlin.async.defProviderAsync_K
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.addRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.build
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.setOnDoneRun
import com.tezov.gofo.retrofit.unsplash.UnsplashCacheProvider
import com.tezov.gofo.room.data.ItemPhoto

class PhotoDataManager(cacheProvider: defProviderAsync_K<ItemPhoto>) : RecyclerListDataManager<CompletableDeferred<ItemPhoto?>>(CompletableDeferred<ItemPhoto?>().javaClass) {
    val iterator = UnsplashCacheProvider.newIterator(cacheProvider)
    var size:Int = 0
    fun init():Deferred<Unit>{
        return RunnableGroup(this, Handler.PRIMARY()).build { deferred ->
            addRun {
                iterator.initIndex().onComplete {
                    next()
                }
            }
            addRun {
                iterator.getDataProvider().size().onComplete {
                    Handler.MAIN().postRun(this){
                        size = it.getCompleted()
                        rowManager?.notifyUpdatedAll(false)
                       next()
                    }
                }
            }
            setOnDoneRun {
                deferred.notifyComplete()
            }
            start()
        }
    }
    override fun size(): Int {
        return size
    }
    override fun get(index: Int): CompletableDeferred<ItemPhoto?> {
        val deferred = CompletableDeferred<ItemPhoto?>()
        AppHandler.getLoaderImageHandler().postRun(this){
            val result = try {
                iterator.get(index, true)
            }
            catch (e: Exception){
                null
            }
            if(AppHandler.getLoaderImageHandler().isMe()){
                deferred.notifyComplete(result)
            }
            else{
                deferred.notifyComplete(AppHandler.getLoaderImageHandler(), result)
            }
        }
        return deferred
    }
}