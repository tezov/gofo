package com.tezov.gofo.retrofit.unsplash.data

import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataPhotoUrl: DataBase(){
    @ProguardFieldKeep
    @SerializedName("raw")
     var raw:String? = null
    @ProguardFieldKeep
    @SerializedName("full")
     var full:String? = null
    @ProguardFieldKeep
    @SerializedName("regular")
     var regular:String? = null
    @ProguardFieldKeep
    @SerializedName("small")
     var small:String? = null
    @ProguardFieldKeep
    @SerializedName("thumb")
     var thumb:String? = null

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(raw)
        buffer.put(full)
        buffer.put(regular)
        buffer.put(small)
        buffer.put(thumb)
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        raw = buffer.string
        full = buffer.string
        regular = buffer.string
        small = buffer.string
        thumb = buffer.string
    }
    override fun toDebugString(): DebugString {
        val data = DebugString();
        data.append("raw",raw)
        data.append("full",full)
        data.append("regular",regular)
        data.append("small",small)
        data.append("thumb",thumb)
        return data
    }

}
