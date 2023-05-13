package com.tezov.gofo.retrofit.unsplash.data

import com.tezov.lib_java.debug.DebugLog
import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataResult{
    @ProguardFieldKeep
    @SerializedName("total")
    var photoCount:Int? = null
    @ProguardFieldKeep
    @SerializedName("total_pages")
    var pageCount:Int? = null
    @ProguardFieldKeep
    @SerializedName("results")
    var photos:List<DataPhoto>? = null

    fun toDebugString(): DebugString {
        val data = DebugString()
        data.append("photoCount",photoCount)
        data.append("pageCount",pageCount)
        data.append("photos",photos)
        return data
    }
    fun toDebugLog(){
/*#-debug-> DebugLog.start().send(toDebugString()).end(); <-debug-#*/
    }
}