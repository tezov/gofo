package com.tezov.gofo.retrofit.unsplash

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.google.gson.GsonBuilder
import com.tezov.gofo.R
import com.tezov.gofo.application.AppInfo
import com.tezov.lib_java.application.AppUUIDGenerator
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.ui.view.status.StatusParam
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import com.tezov.gofo.retrofit.unsplash.data.DataPhoto
import com.tezov.gofo.retrofit.unsplash.data.DataResult
import com.tezov.lib_java.debug.DebugLog
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executor

class UnsplashNetworkProvider(val handlerCallback: Handler) {
    companion object {
        const val REQUEST_COUNT = 30
        private const val EXCEPTION_LIMIT_API = "exception_limit_api"
        private const val EXCEPTION_RESPONSE = "exception_response"

        private val MEDIA_TYPE_JSON = "application/json".toMediaType()
        private const val URL_BASE = "https://api.unsplash.com/"
    }

    private lateinit var retrofit: Retrofit
    lateinit var client: OkHttpClient
    private lateinit var api: restApi

    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
        try {
            val url = URL(URL_BASE)
            client = OkHttpClient.Builder()
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val requestSource = chain.request()
                        val requestAltered: Request =
                            requestSource.newBuilder()
                                .addHeader("Accept-Version", "v1")
//                                .addHeader("Authorization","Client-ID c8ac06eded170381782c8f855b9bf5f12bc6020cfa83bb5415466f81b0cdeba5")
//                                .addHeader("Authorization","Client-ID Yme6ZcumIXpWryQ0DPc249CE0ua2Mxh66Y-4W2gPAAc")
                                .addHeader("Authorization","Client-ID ZXLj0JRu31JtwJ1gEx-gSw6iDFvVo2JpU46vun-Ay6M") // TEZOV
                                .build()
                        return chain.proceed(requestAltered)
                    }
                })
                .addInterceptor(object : Interceptor {
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val request: Request = chain.request()
                        try {
                            val response: Response = chain.proceed(request)
                            val limitString = response.headers["x-ratelimit-remaining"]
                            limitString?.run {
//                            DebugLog.start().send("unsplash remaining request " + limitString).end()
                                val limit: Int = this.toInt()
                                if (limit <= 0) {
                                    throw IOException(EXCEPTION_LIMIT_API)
                                }
                            }
                            if (response.isSuccessful) {
                                if (request.url.toString().replaceFirst(URL_BASE, "")
                                        .startsWith("photos?")
                                ) {
                                    val total = response.headers["x-total"]
                                    val perPage = response.headers["x-per-page"]
                                    val body: ResponseBody? = response.body
                                    val dataBuild = StringBuilder()
                                    if (total != null && perPage != null && body != null && body.contentType() == MEDIA_TYPE_JSON) {
                                        dataBuild.append("{")
                                        dataBuild.append("\"total\":").append(total).append(",")
                                        dataBuild.append("\"total_pages\":").append(
                                            Math.ceil(total.toDouble() / perPage.toDouble()).toInt()
                                        ).append(",")
                                        dataBuild.append("\"results\":").append(body.string())
                                        dataBuild.append("}")
                                    } else {
                                        dataBuild.append("{")
                                        dataBuild.append("\"total\":0,")
                                        dataBuild.append("\"total_pages\":0,")
                                        dataBuild.append("\"results\":[]")
                                        dataBuild.append("}")
                                    }
                                    val alteredBody: ResponseBody =
                                        dataBuild.toString().toResponseBody(MEDIA_TYPE_JSON)
                                    return response.newBuilder().body(alteredBody).build()
                                }
                            }
                            else{
                                throw IOException(EXCEPTION_RESPONSE)
                            }
                            return response
                        }
                        catch (e:Exception){
                            if(e.message == EXCEPTION_LIMIT_API){
                                AppInfo.toast(AppContext.getResources().getString(R.string.lbl_exception_limit_api), StatusParam.DELAY_INFO_SHORT_ms, StatusParam.Color.FAILED)
                            }
                            else if(e.message == EXCEPTION_RESPONSE){
                                AppInfo.toast(AppContext.getResources().getString(R.string.lbl_exception_response), StatusParam.DELAY_INFO_SHORT_ms, StatusParam.Color.FAILED)
                            }
                            else{
                                AppInfo.toast(AppContext.getResources().getString(R.string.lbl_exception_http_failed), StatusParam.DELAY_INFO_SHORT_ms, StatusParam.Color.FAILED)
                            }
                            throw e
                        }
                    }
                })
//                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
            retrofit = Retrofit.Builder()
                .client(client)
                .callbackExecutor(object : Executor {
                    override fun execute(r: Runnable) {
                       handlerCallback.postRun(this){
                           r.run()
                       }
                    }
                })
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .baseUrl(url).build()
            api = retrofit.create(restApi::class.java)
        } catch (e: Exception) {
/*#-debug-> DebugException.start().log(e).end() <-debug-#*/
        }
    }

    enum class OrderSearch(val s: String) {
        RELEVANT("relevant"),
        LATEST("latest");
        override fun toString(): String {
            return s
        }
    }
    enum class OrderFeed(val s: String) {
        LATEST("latest"),
        OLDEST("oldest"),
        POPULAR("popular");
        override fun toString(): String {
            return s
        }
    }
    enum class Color(val s: String) {
        BLACK("black"),
        WHITE("white"),
        BLACK_AND_WHITE("black_and_white"),
        YELLOW("yellow"),
        ORANGE("orange"),
        RED("red"),
        PURPLE("purple"),
        MAGENTA("magenta"),
        GREEN("green"),
        TEAL("teal"),
        BLUE("blue");

        override fun toString(): String {
            return s
        }
    }
    enum class Orientation(val s: String) {
        LANDSCAPE("landscape"),
        PORTRAIT("portrait"),
        SQUARISH("squarish");

        override fun toString(): String {
            return s
        }
    }

    @Throws(Throwable::class)
    fun selectRandom(
        tags: String? = null,
        orientation: Orientation? = null
    ): Deferred<List<DataPhoto>?> {
        val deferred = CompletableDeferred<List<DataPhoto>?>()
        api.selectRandom(
            tags,
            orientation = orientation,
            sig = AppUUIDGenerator.next().toHexString()
        ).enqueue(object : Callback<List<DataPhoto>> {
            override fun onResponse(
                call: Call<List<DataPhoto>>,
                response: retrofit2.Response<List<DataPhoto>>
            ) {
                val data = response.body()
                if (data?.isNotEmpty() == true) {
                    deferred.notifyComplete(data)
                } else {
                    deferred.notifyComplete(null)
                }
            }

            override fun onFailure(call: Call<List<DataPhoto>>, t: Throwable) {
                deferred.notifyComplete(null)
            }
        })
        return deferred
    }

    @Throws(Throwable::class)
    fun selectSearch(
        tags: String,
        pageNumber: Int,
        order: OrderSearch? = OrderSearch.RELEVANT,
        orientation: Orientation? = null,
        color: Color? = null
    ): Deferred<DataResult?> {
        val deferred = CompletableDeferred<DataResult?>()
        api.selectSearch(tags, pageNumber, order = order, orientation = orientation, color = color)
            .enqueue(object : Callback<DataResult> {
                override fun onResponse(
                    call: Call<DataResult>,
                    response: retrofit2.Response<DataResult>
                ) {
                    val data = response.body()
                    if (data?.photos?.isNotEmpty() == true) {
                        deferred.notifyComplete(data)
                    } else {
                        deferred.notifyComplete(null)
                    }
                }

                override fun onFailure(call: Call<DataResult>, t: Throwable) {
                    deferred.notifyComplete(null)
                }
            })
        return deferred
    }

    @Throws(Throwable::class)
    fun selectFeed(pageNumber: Int, order: OrderFeed? = OrderFeed.LATEST): Deferred<DataResult?> {
        val deferred = CompletableDeferred<DataResult?>()
        api.selectFeed(pageNumber, order = order).enqueue(object : Callback<DataResult> {
            override fun onResponse(
                call: Call<DataResult>,
                response: retrofit2.Response<DataResult>
            ) {
                val data = response.body()
                if (data?.photos?.isNotEmpty() == true) {
                    deferred.notifyComplete(data)
                } else {
                    deferred.notifyComplete(null)
                }
            }

            override fun onFailure(call: Call<DataResult>, t: Throwable) {
                deferred.notifyComplete(null)
            }
        })
        return deferred
    }

    protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
    }

    interface restApi {
        @GET("photos/random")
        fun selectRandom(
            @Query("query") tags: String?,
            @Query("count") countMax: Int = REQUEST_COUNT,
            @Query("orientation") orientation: Orientation?,
            @Query("sig") sig: String,
        ): Call<List<DataPhoto>>

        @GET("photos")
        fun selectFeed(
            @Query("page") pageNumber: Int,
            @Query("per_page") pageCountMax: Int = REQUEST_COUNT,
            @Query("order_by") order: OrderFeed?,
        ): Call<DataResult>

        @GET("search/photos")
        fun selectSearch(
            @Query("query") tags: String,
            @Query("page") pageNumber: Int,
            @Query("per_page") pageCountMax: Int = REQUEST_COUNT,
            @Query("order_by") order: OrderSearch?,
            @Query("orientation") orientation: Orientation?,
            @Query("color") color: Color?,
        ): Call<DataResult>
    }
}