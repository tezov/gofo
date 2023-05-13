package com.tezov.gofo.fragment

import com.tezov.gofo.R
import com.tezov.gofo.application.AppHandler
import com.tezov.gofo.application.SharePreferenceKey
import com.tezov.gofo.application.Application
import com.tezov.gofo.retrofit.unsplash.UnsplashCacheProvider
import com.tezov.gofo.room.data.ItemPhoto

import com.tezov.lib.adapterJavaToKotlin.async.defProviderAsync_K
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterRandom

open class FragmentRandom : FragmentRecyclerBase() {

    override fun getProvider(): defProviderAsync_K<ItemPhoto> {
        val sp = Application.sharedPreferences()
        val id = sp.getLong(SharePreferenceKey.KEY_FILTER(ItemFilter.Type.RANDOM))
        val filter = ItemFilter.load(id)
        val dataFilter = filter!!.getData<DataFilterRandom>()!!
        return getProvider(dataFilter)
    }
    private fun getProvider(dataFilter: DataFilterRandom): defProviderAsync_K<ItemPhoto> {
        //TODO clean cache before notify recycler and datamanager iterator
        return UnsplashCacheProvider.Random(AppHandler.getCacheIteratorHandler(), dataFilter)
    }
    override fun getProvider(itemFilter: ItemFilter): defProviderAsync_K<ItemPhoto> {
        val dataFilter = itemFilter.getData<DataFilterRandom>()!!
        return getProvider(dataFilter)
    }

    override fun getTitleIdResource(): Int {
        return R.string.frg_random_title
    }


}