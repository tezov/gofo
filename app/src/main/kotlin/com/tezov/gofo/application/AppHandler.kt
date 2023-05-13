package com.tezov.gofo.application

import com.tezov.lib_java.async.Handler

object AppHandler {
    private var networkProvider:Handler? = null
    private var cacheIterator:Handler? = null
    private var loaderImage:Handler? = null

    fun getNetworkProviderHandler():Handler{
        return networkProvider!!
    }
    fun getCacheIteratorHandler():Handler{
        return cacheIterator!!
    }
    fun getLoaderImageHandler():Handler{
        return loaderImage!!
    }

    fun init(){
        networkProvider = Handler.newHandler("networkProvider")
        cacheIterator = Handler.newHandler("cacheIterator")
        loaderImage = Handler.newHandler("loaderImage")
    }
    fun quit(){
        networkProvider?.apply {
            quit()
            networkProvider = null
        }
        cacheIterator?.apply {
            quit()
            cacheIterator = null
        }
        loaderImage?.apply {
            quit()
            loaderImage = null
        }
    }

}