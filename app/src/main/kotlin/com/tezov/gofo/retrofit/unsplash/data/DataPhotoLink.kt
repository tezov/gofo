package com.tezov.gofo.retrofit.unsplash.data

import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataPhotoLink: DataBase() {
    @ProguardFieldKeep
    @SerializedName("self")
     var self:String? = null
    @ProguardFieldKeep
    @SerializedName("html")
     var html:String? = null
    @ProguardFieldKeep
    @SerializedName("download")
     var download:String? = null
    @ProguardFieldKeep
    @SerializedName("download_location")
     var downloadLocation:String? = null

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(self)
        buffer.put(html)
        buffer.put(download)
        buffer.put(downloadLocation)
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        self = buffer.string
        html = buffer.string
        download = buffer.string
        downloadLocation = buffer.string
    }
    override fun toDebugString(): DebugString {
        val data = DebugString();
        data.append("self",self)
        data.append("html",html)
        data.append("download",download)
        data.append("downloadLocation",downloadLocation)
        return data
    }

}
