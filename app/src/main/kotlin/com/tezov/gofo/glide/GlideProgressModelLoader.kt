package com.tezov.gofo.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelCache
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader
import com.tezov.gofo.fragment.FragmentFeed
import com.tezov.gofo.room.database.Database
import com.tezov.lib.adapterJavaToKotlin.async.Handler.post
import com.tezov.lib_java.application.AppContext
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.type.collection.ListKey
import com.tezov.lib_java.wrapperAnonymous.FunctionW
import com.tezov.lib_java_android.ui.recycler.RecyclerList
import com.tezov.lib_java_android.ui.recycler.RecyclerListGridBag
import okhttp3.*
import okio.*
import java.io.IOException
import java.io.InputStream


class GlideProgressModelLoader(
    val interceptor:InterceptorBody,
    val modelCache: ModelCache<GlideProgressUrl, GlideUrl>?
) : ModelLoader<GlideProgressUrl, InputStream> {
    companion object {
        fun registerComponents(context: Context, glide: Glide, registry: Registry, clientBuilder : OkHttpClient.Builder) {
            val factory = GlideProgressModelLoaderFactory(InterceptorBody())
            clientBuilder.addInterceptor(factory.interceptor)
            registry.prepend(GlideProgressUrl::class.java, InputStream::class.java, factory)
        }
    }

    override fun handles(model: GlideProgressUrl): Boolean {
        return true
    }

    override fun buildLoadData(
        model: GlideProgressUrl,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<InputStream> {
        var url: GlideUrl? = modelCache?.run{
            get(model, width, height)?.also{ put(model, width, height, it) }
        }
        if(url == null){
            url = GlideUrl(model.url)
        }
        interceptor.listen(model)
        return LoadData(url, OkHttpStreamFetcher(GlideModule.client(), url))
    }
}

class InterceptorBody: Interceptor{
    val listeners:ListKey<String, GlideProgressUrl>
    init {
        listeners = ListKey(object : FunctionW<GlideProgressUrl, String>(){
            override fun apply(t: GlideProgressUrl): String {
                return t.url
            }
        })
    }
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request:Request = chain.request()
        val glideUrl:GlideProgressUrl? = listeners.removeKey(request.url.toString())
        val response: Response = chain.proceed(request)
        if((glideUrl != null) && (response.body != null)){
            return response.newBuilder()
                .body(ProgressResponseBody(response.body!!, glideUrl))
                .build()
        }
        return response
    }
    fun listen(model: GlideProgressUrl){
        listeners.add(model)
    }
}
private class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val model: GlideProgressUrl
) :
    ResponseBody() {
    private var bufferedSource: BufferedSource? = null
    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }
    @Throws(IOException::class)
    override fun contentLength(): Long {
        return responseBody.contentLength()
    }
    @Throws(IOException::class)
    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source())
        }
        return bufferedSource!!
    }
    private fun source(source: Source): BufferedSource {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            @Throws(IOException::class)
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                if(bytesRead > 0){
                    totalBytesRead += bytesRead
                    Handler.SECONDARY().post(this){
                        runSafe {
                            model.update(totalBytesRead, responseBody.contentLength())
                        }
                    }
                }
                return bytesRead
            }
        }.buffer()
    }
}

class GlideProgressModelLoaderFactory(val interceptor:InterceptorBody, val modelCache: ModelCache<GlideProgressUrl, GlideUrl>? = null) : com.bumptech.glide.load.model.ModelLoaderFactory<GlideProgressUrl, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<GlideProgressUrl, InputStream>{
        return GlideProgressModelLoader(interceptor, modelCache)
    }
    override fun teardown(){

    }
}
