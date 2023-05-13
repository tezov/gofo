package com.tezov.gofo.retrofit.unsplash.data

import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataUserImage: DataBase() {
    @ProguardFieldKeep
    @SerializedName("small")
    var small:String? = null
    @ProguardFieldKeep
    @SerializedName("medium")
    var medium:String?= null
    @ProguardFieldKeep
    @SerializedName("large")
    var large:String?= null

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(small)
        buffer.put(medium)
        buffer.put(large)
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        small = buffer.string
        medium = buffer.string
        large = buffer.string
    }

    override fun toDebugString(): DebugString {
        val data = DebugString();
        data.append("small",small)
        data.append("medium",medium)
        data.append("large",large)
        return data
    }

}
