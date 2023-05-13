package com.tezov.gofo.retrofit.unsplash.data

import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataUserLink: DataBase(){
    @ProguardFieldKeep
    @SerializedName("self")
    var self:String? = null
    @ProguardFieldKeep
    @SerializedName("html")
    var html:String? = null
    @ProguardFieldKeep
    @SerializedName("photos")
    var photos:String? = null

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(self)
        buffer.put(html)
        buffer.put(photos)
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        self = buffer.string
        html = buffer.string
        photos = buffer.string
    }
    override fun toDebugString(): DebugString {
        val data = DebugString();
        data.append("self",self)
        data.append("html",html)
        data.append("photos",photos)
        return data
    }
}
