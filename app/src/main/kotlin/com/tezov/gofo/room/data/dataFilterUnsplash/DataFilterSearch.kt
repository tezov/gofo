package com.tezov.gofo.room.data.dataFilterUnsplash

import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib.adapterJavaToKotlin.toolbox.Nullify.nullify
import com.tezov.gofo.retrofit.unsplash.UnsplashNetworkProvider
import com.tezov.gofo.room.data.ItemFilter

class DataFilterSearch(id: Long?) : ItemFilter.Data<DataFilterSearch>(id){
    private val tags:MutableSet<String> = androidx.collection.ArraySet()
    var orientation: UnsplashNetworkProvider.Orientation? = null
    var order: UnsplashNetworkProvider.OrderSearch = UnsplashNetworkProvider.OrderSearch.RELEVANT
    var color: UnsplashNetworkProvider.Color? = null

    override fun getType(): ItemFilter.Type {
        return ItemFilter.Type.SEARCH
    }

    override fun getProvider(): ItemFilter.Provider {
        return ItemFilter.Provider.UNSPLASH
    }

    fun getTags():Set<String>{
        return tags
    }
    fun addTag(tag:String):Boolean{
        return if(tag.nullify() == null){
            false
        }
        else{
            tags.add(tag)
        }
    }
    fun removeTag(tag:String):Boolean{
        return tags.remove(tag)
    }
    fun clearTag(){
        tags.clear()
    }

    fun getQuery():String?{
        return tags.joinToString(",").nullify()
    }

    fun setOrientation(position:Int){
        if(position == 0){
            orientation = null
        }
        else{
            orientation = UnsplashNetworkProvider.Orientation.values()[position - 1]
        }
    }

    fun setOrder(position:Int){
        order = UnsplashNetworkProvider.OrderSearch.values()[position]
    }

    fun setColor(position:Int){
        if(position == 0){
            color = null
        }
        else{
            color = UnsplashNetworkProvider.Color.values()[position - 1]
        }
    }

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(tags.joinToString(",").nullify())
        buffer.put(orientation?.name)
        buffer.put(order.name)
        buffer.put(color?.name)
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        buffer.string?.split(",")?.let {
            tags.addAll(it)
        }
        buffer.string?.let {
            orientation = UnsplashNetworkProvider.Orientation.valueOf(it)
        }
        buffer.string.let {
            order = UnsplashNetworkProvider.OrderSearch.valueOf(it)
        }
        buffer.string?.let {
            color = UnsplashNetworkProvider.Color.valueOf(it)
        }
    }

    override fun toDebugString(): DebugString {
        val data = super.toDebugString()
        data.append("orientation", orientation)
        data.append("order", order)
        data.append("tags", getQuery())
        data.append("color", color)
        return data
    }
}