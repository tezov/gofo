package com.tezov.gofo.dialog

import com.tezov.gofo.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import com.tezov.gofo.application.SharePreferenceKey
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.toolbox.Compare
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterFeed
import com.tezov.gofo.room.database.Database

class DialogFilterFeed : DialogFilterBase() {
    lateinit var dataFilter:DataFilterFeed
    lateinit var dataFilterFingerPrint: ByteArray
    lateinit var spnOrder: Spinner

    override fun getFrameLayoutId(): Int {
        return R.layout.dialog_filter_feed
    }

    override fun getFilterIdKey(): String {
        return SharePreferenceKey.KEY_FILTER(ItemFilter.Type.FEED)
    }

    override fun onFrameMerged(view: View, savedInstanceState: Bundle?) {
        spnOrder = view.findViewById(R.id.spn_order)
    }

    override fun onPrepare(hasBeenReconstructed: Boolean) {
        super.onPrepare(hasBeenReconstructed)
        filter.getData<DataFilterFeed>()?.let {
            dataFilter = it
        }
        ?: let {
            dataFilter = DataFilterFeed(null)
        }
        dataFilterFingerPrint = dataFilter.getFingerPrint()
        with(spnOrder){
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    dataFilter.setOrder(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            dataFilter.order.let {
                setSelection(it.ordinal)
            }
        }
    }

    override fun onConfirm_beforeSave():Deferred<Boolean> {
        val different = !Compare.equals(dataFilterFingerPrint, dataFilter.getFingerPrint())
        if(different){
            filter.id = null
            filter.setData(dataFilter)
        }
        return Completable.notifyComplete(different)
    }
    override fun onConfirm_afterSave(previousFilterId:Long?, nextFilterId:Long): Deferred<Unit> {
        val deferred = CompletableDeferred<Unit>()
        Handler.PRIMARY().postRun(this){
            previousFilterId?.let {
                Database.lock(this)
                Database.photos.deleteAllWithFilterId(it)
                Database.filters.deleteWithId(it)
                Database.unlock(this)
            }
            deferred.notifyComplete()
        }
        return deferred
    }
}