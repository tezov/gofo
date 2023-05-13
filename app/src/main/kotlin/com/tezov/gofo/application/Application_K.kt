package com.tezov.gofo.application

import com.tezov.gofo.room.database.Database.Companion.filters
import com.tezov.gofo.application.SharePreferenceKey.KEY_FILTER
import android.content.Intent
import com.tezov.gofo.retrofit.unsplash.UnsplashCacheProvider
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterFeed
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterRandom
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterSearch
import com.tezov.gofo.room.database.Database
import com.tezov.lib_java_android.application.ApplicationSystem

object Application_K {
    @JvmStatic
    fun onMainActivityStart(app: ApplicationSystem, source: Intent?, isRestarted: Boolean) {
        if (!isRestarted) {
            if (AppInfo.isFirstLaunch()) {
                setDefaultSharePreference()
            }
            AppHandler.init()
            UnsplashCacheProvider.init()
        }
    }
    @JvmStatic
    fun onApplicationPause(app: ApplicationSystem?) {

    }
    @JvmStatic
    fun onApplicationClose(app: ApplicationSystem?) {
        UnsplashCacheProvider.quit()
        AppHandler.quit()
    }

    private fun setDefaultSharePreference() {
        Database.lock(this)
        val sp = Application.sharedPreferences()
        ItemFilter().apply {
            type = ItemFilter.Type.FEED
            provider = ItemFilter.Provider.UNSPLASH
            val dataFilterFeed = DataFilterFeed(null)
            setData(dataFilterFeed)
            val id = filters.insert(this)
            sp.put(KEY_FILTER(ItemFilter.Type.FEED), id)
        }
        ItemFilter().apply {
            type = ItemFilter.Type.RANDOM
            provider = ItemFilter.Provider.UNSPLASH
            val dataFilterRandom = DataFilterRandom(null)
            setData(dataFilterRandom)
            val id = filters.insert(this)
            sp.put(KEY_FILTER(ItemFilter.Type.RANDOM), id)
        }
        ItemFilter().apply {
            type = ItemFilter.Type.SEARCH
            provider = ItemFilter.Provider.UNSPLASH
            val dataFilterSearch = DataFilterSearch(null)
            dataFilterSearch.apply {
                addTag("sea")
                addTag("island")
                addTag("mountain")
                addTag("snow")
            }
            setData(dataFilterSearch)
            val id = filters.insert(this)
            sp.put(KEY_FILTER(ItemFilter.Type.SEARCH), id)
        }
        Database.unlock(this)
    }
}