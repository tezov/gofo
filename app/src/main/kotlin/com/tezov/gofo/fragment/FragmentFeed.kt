package com.tezov.gofo.fragment

import com.tezov.gofo.R
import com.tezov.gofo.application.AppHandler
import com.tezov.gofo.application.SharePreferenceKey
import com.tezov.gofo.application.Application
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java_android.ui.recycler.RecyclerList
import com.tezov.lib_java_android.ui.recycler.RecyclerListGridBag
import com.tezov.lib.adapterJavaToKotlin.async.Handler.post
import com.tezov.gofo.retrofit.unsplash.UnsplashCacheProvider
import com.tezov.gofo.room.data.ItemPhoto

import com.tezov.lib.adapterJavaToKotlin.async.defProviderAsync_K
import com.tezov.lib.adapterJavaToKotlin.observer.ObserverValue
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterFeed
import com.tezov.gofo.room.database.Database

open class FragmentFeed : FragmentRecyclerBase() {
    companion object{
        const val MAX_ENTRY = 500
    }
    override fun getProvider(): defProviderAsync_K<ItemPhoto> {
        val sp = Application.sharedPreferences()
        val id = sp.getLong(SharePreferenceKey.KEY_FILTER(ItemFilter.Type.FEED))
        val filter = ItemFilter.load(id)
        val dataFilter = filter!!.getData<DataFilterFeed>()!!
        return getProvider(dataFilter)
    }
    private fun getProvider(dataFilter: DataFilterFeed): defProviderAsync_K<ItemPhoto> {
        val provider = UnsplashCacheProvider.Feed(AppHandler.getCacheIteratorHandler(), dataFilter)
        provider.observe(ObserverValue(this){
            onComplete {
                Handler.SECONDARY().post(this){
                    runSafe {
                        Database.lock(this)
                        val positionBase = Database.photos.getMaxPositionBase(dataFilter.getId())
                        positionBase?.let {
                            val recycler = view as RecyclerListGridBag
                            var position = recycler.getPosition(RecyclerList.PositionSnap.BOTTOM)
                            if(position == RecyclerList.NO_POSITION){
                                position = 0
                            }
                            val count = Database.photos.countAfterPosition(dataFilter.getId(), position, positionBase)
                            if (count > MAX_ENTRY) {
                                val maxAllowedPosition = Database.photos.getPositionAbsoluteAfterOffset(dataFilter.getId(), position, positionBase, MAX_ENTRY)
                                maxAllowedPosition?.let {
                                    Database.photos.deleteAllAfterPosition(dataFilter.getId(), it, positionBase)
                                }
                            }
                        }
                        Database.unlock(this)
                    }
                }
            }
        })
        return provider
    }
    override fun getProvider(itemFilter: ItemFilter): defProviderAsync_K<ItemPhoto> {
        val dataFilter = itemFilter.getData<DataFilterFeed>()!!
        return getProvider(dataFilter)
    }

    override fun getTitleIdResource(): Int {
        return R.string.frg_feed_title
    }

}