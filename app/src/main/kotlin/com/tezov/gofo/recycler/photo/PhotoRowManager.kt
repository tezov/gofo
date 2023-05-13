package com.tezov.gofo.recycler.photo

import com.tezov.lib_java_android.ui.recycler.RecyclerListRowBinder
import com.tezov.lib_java_android.ui.recycler.RecyclerListRowManager
import kotlinx.coroutines.CompletableDeferred
import com.tezov.gofo.room.data.ItemPhoto

class PhotoRowManager(dataManager: PhotoDataManager) :
    RecyclerListRowManager<CompletableDeferred<ItemPhoto?>>(dataManager) {
    init {
        add(PhotoRowBinder(this))
    }
    override fun getItemViewType(position: Int): Int {
        return RecyclerListRowBinder.ViewType.DEFAULT.ordinal()
    }
}