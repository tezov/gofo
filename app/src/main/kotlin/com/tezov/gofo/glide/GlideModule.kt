package com.tezov.gofo.glide

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.tezov.gofo.application.AppConfig
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java_android.application.AppContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InputStream
import java.util.concurrent.TimeUnit

@GlideModule
class GlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val clientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
//        clientBuilder.addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS})
        GlideProgressModelLoader.registerComponents(context, glide, registry, clientBuilder)
        clientBuilder.readTimeout(TIMEOUT_ms.toLong(), TimeUnit.MILLISECONDS)
        clientBuilder.writeTimeout(TIMEOUT_ms.toLong(), TimeUnit.MILLISECONDS)
        clientBuilder.connectTimeout(TIMEOUT_ms.toLong(), TimeUnit.MILLISECONDS)
        client = clientBuilder.build()
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
    }
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        with(builder){
            val cacheSize = AppConfig.CACHE_IMAGE_SIZE_o * AppConfig.CACHE_IMAGE_COUNT
            val lru = LruResourceCache(cacheSize)
            setMemoryCache(lru)
            setDiskCache(InternalCacheDiskCacheFactory(context, cacheSize))
            setDefaultRequestOptions(requestOptionsDefault())
        }
    }

    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
    }
    protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
    }

    companion object {
        private val TIMEOUT_ms = 15000
        private lateinit var client: OkHttpClient
        fun client() = client
        private fun requestOptionsDefault(): RequestOptions {
            return RequestOptions()
                .centerCrop()
                .timeout(TIMEOUT_ms)
                .format(DecodeFormat.PREFER_ARGB_8888)
                .encodeFormat(Bitmap.CompressFormat.JPEG)
                .encodeQuality(100)
                .dontAnimate()
        }
        fun clearCacheAsync():Deferred<Unit>{
            val deferred:CompletableDeferred<Unit> = CompletableDeferred()
            val handler = Handler.fromMyLooper()
            Handler.SECONDARY().postRun(this){
                Glide.get(AppContext.get())
                    .clearDiskCache()
                deferred.notifyComplete(handler)
            }
            return deferred
        }
    }

}

