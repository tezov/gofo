package com.tezov.gofo.room.data.dataFilterUnsplash

import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.debug.DebugString
import com.tezov.gofo.retrofit.unsplash.UnsplashNetworkProvider
import com.tezov.gofo.room.data.ItemFilter

class DataFilterFeed(id: Long?) : ItemFilter.Data<DataFilterFeed>(id){
    var order: UnsplashNetworkProvider.OrderFeed = UnsplashNetworkProvider.OrderFeed.LATEST

    override fun getType(): ItemFilter.Type {
        return ItemFilter.Type.FEED
    }

    override fun getProvider(): ItemFilter.Provider {
        return ItemFilter.Provider.UNSPLASH
    }

    fun setOrder(position:Int){
        order = UnsplashNetworkProvider.OrderFeed.values()[position]
    }

    override fun toByteBuffer(buffer: ByteBuffer){
        buffer.put(order.name)
    }
    override fun fromByteBuffer(buffer: ByteBuffer){
        buffer.string.let {
            order = UnsplashNetworkProvider.OrderFeed.valueOf(it)
        }
    }

    override fun toDebugString(): DebugString {
        val data = super.toDebugString()
        data.append("order", order)
        return data
    }
}