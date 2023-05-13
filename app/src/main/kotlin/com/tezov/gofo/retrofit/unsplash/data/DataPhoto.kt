package com.tezov.gofo.retrofit.unsplash.data

import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataPhoto: DataBase(){
    @ProguardFieldKeep
    @SerializedName("id")
    var id: String? = null
    @ProguardFieldKeep
    @SerializedName("created_at")
    var created: String? = null
    @ProguardFieldKeep
    @SerializedName("blur_hash")
     var blur_hash:String? = null
    @ProguardFieldKeep
    @SerializedName("urls")
     var urls: DataPhotoUrl? = null
    @ProguardFieldKeep
    @SerializedName("links")
     var links: DataPhotoLink? = null
    @ProguardFieldKeep
    @SerializedName("width")
    var width:Int? = null
    @ProguardFieldKeep
    @SerializedName("height")
    var height:Int? = null
    @ProguardFieldKeep
    @SerializedName("color")
    var color:String? = null
    @ProguardFieldKeep
    @SerializedName("description")
    var description:String? = null
    @ProguardFieldKeep
    @SerializedName("user")
    var user: DataUser? = null

    override fun toByteBuffer(buffer:ByteBuffer){
        buffer.put(id)
        buffer.put(created)
        buffer.put(blur_hash)
        urls?.apply {
            buffer.flagNotNull()
            this.toByteBuffer(buffer)
        } ?: buffer.flagNull()
        links?.apply {
            buffer.flagNotNull()
            this.toByteBuffer(buffer)
        } ?: buffer.flagNull()
        buffer.put(width)
        buffer.put(height)
        buffer.put(color)
        buffer.put(description)
        user?.apply {
            buffer.flagNotNull()
            this.toByteBuffer(buffer)
        } ?: buffer.flagNull()
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        id = buffer.string
        created = buffer.string
        blur_hash = buffer.string
        if(buffer.isFlagNotNull) {
            urls = DataPhotoUrl()
            urls!!.fromByteBuffer(buffer)
        }
        if(buffer.isFlagNotNull) {
            links = DataPhotoLink()
            links!!.fromByteBuffer(buffer)
        }
        width = buffer.int
        height = buffer.int
        color = buffer.string
        description = buffer.string
        if(buffer.isFlagNotNull) {
            user = DataUser()
            user!!.fromByteBuffer(buffer)
        }
    }

    override fun toDebugString(): DebugString {
        val data = DebugString();
        data.append("id",id)
        data.append("created",created)
        data.append("blur_hash",blur_hash)
        data.append("size","${width}x${height}")
        data.append("color",color)
        data.append("description",description)
        data.append("urls",urls)
        data.append("links",links)
        data.append("user",user)
        return data
    }
}