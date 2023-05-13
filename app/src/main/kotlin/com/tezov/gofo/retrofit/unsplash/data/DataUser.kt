package com.tezov.gofo.retrofit.unsplash.data

import com.google.gson.annotations.SerializedName
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

class DataUser: DataBase(){
    @ProguardFieldKeep
    @SerializedName("id")
    var id:String? = null
    @ProguardFieldKeep
    @SerializedName("username")
    var pseudoUnsplash:String? = null
    @ProguardFieldKeep
    @SerializedName("name")
    var name:String? = null
    @ProguardFieldKeep
    @SerializedName("portfolio_url")
    var portfolioExterne:String? = null
    @ProguardFieldKeep
    @SerializedName("bio")
    var bio:String? = null
    @ProguardFieldKeep
    @SerializedName("location")
    var location:String? = null
    @ProguardFieldKeep
    @SerializedName("total_photos")
    var photoCount:Int? = null
    @ProguardFieldKeep
    @SerializedName("instagram_username")
    var pseudoInstagram:String? = null
    @ProguardFieldKeep
    @SerializedName("twitter_username")
    var pseudoTwitter:String? = null
    @ProguardFieldKeep
    @SerializedName("profile_image")
    var images: DataUserImage? = null
    @ProguardFieldKeep
    @SerializedName("links")
    var links: DataUserLink? = null

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(id)
        buffer.put(pseudoUnsplash)
        buffer.put(name)
        buffer.put(portfolioExterne)
        buffer.put(bio)
        buffer.put(location)
        buffer.put(photoCount)
        buffer.put(pseudoInstagram)
        buffer.put(pseudoTwitter)
        images?.apply {
            buffer.flagNotNull()
            this.toByteBuffer(buffer)
        } ?: buffer.flagNull()
        links?.apply {
            buffer.flagNotNull()
            this.toByteBuffer(buffer)
        } ?: buffer.flagNull()
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        id = buffer.string
        pseudoUnsplash = buffer.string
        name = buffer.string
        portfolioExterne = buffer.string
        bio = buffer.string
        location = buffer.string
        photoCount = buffer.int
        pseudoInstagram = buffer.string
        pseudoTwitter = buffer.string
        if(buffer.isFlagNotNull){
            images = DataUserImage()
            images!!.fromByteBuffer(buffer)
        }
        if(buffer.isFlagNotNull){
            links = DataUserLink()
            links!!.fromByteBuffer(buffer)
        }
    }

    override fun toDebugString(): DebugString {
        val data = DebugString();
        data.append("id",id)
        data.append("pseudoUnsplash",pseudoUnsplash)
        data.append("name",name)
        data.append("portfolioExterne",portfolioExterne)
        data.append("bio",bio)
        data.append("location",location)
        data.append("photoCount",photoCount)
        data.append("pseudoInstagram",pseudoInstagram)
        data.append("pseudoTwitter",pseudoTwitter)
        data.append("images",images)
        data.append("links",links)
        return data
    }

}
