package com.tezov.gofo.fragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.tezov.gofo.application.AppConfig.RECYCLER_SPAN
import com.tezov.gofo.application.AppHandler
import com.tezov.lib_java.type.defEnum.Event
import com.tezov.lib_java.type.defEnum.Event.ON_CLICK_SHORT
import com.tezov.lib_java_android.application.AppResources
import com.tezov.lib_java_android.ui.recycler.RecyclerListLayoutGridBagVertical
import com.tezov.lib_java_android.ui.recycler.RecyclerListGridBag
import com.tezov.gofo.dialog.DialogPhotoDownload
import kotlinx.coroutines.CompletableDeferred
import com.tezov.lib.adapterJavaToKotlin.Navigate
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.async.PostToHandler
import com.tezov.gofo.recycler.photo.PhotoDataManager
import com.tezov.gofo.recycler.photo.PhotoRowManager

import com.tezov.gofo.recycler.ScrollingHoldOneWay
import com.tezov.lib.adapterJavaToKotlin.async.defProviderAsync_K
import com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent
import com.tezov.gofo.retrofit.unsplash.UnsplashCacheProvider
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.ItemPhoto
import com.tezov.lib_java_android.ui.navigation.NavigationArguments
import fragment.FragmentBase_bt

abstract class FragmentRecyclerBase : FragmentBase_bt() {
    companion object{
        const val FILTER_UPDATED = 0
    }

    private val onTouchEventHandler = ScrollingHoldOneWay()
    private var enableTouch = false

    override fun newState(): State {
        return State()
    }
    override fun getState(): State? {
        return super.getState() as? State
    }
    override fun obtainState(): State {
        return super.obtainState() as State
    }
    override fun getParam(): Param? {
        return super.getParam() as? Param
    }

    final override fun getLayoutId(): Int {
        return AppResources.NULL_ID
    }

    protected abstract fun getProvider(): defProviderAsync_K<ItemPhoto>
    protected abstract fun getProvider(itemFilter: ItemFilter): defProviderAsync_K<ItemPhoto>

    protected abstract fun getTitleIdResource():Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val recycler = RecyclerListGridBag(context)
        val params = ViewGroup.LayoutParams(MATCH_PARENT,MATCH_PARENT)
        recycler.layoutParams = params
        val provider = getProvider()
        val dataManager = PhotoDataManager(provider)
        val rowManager = PhotoRowManager(dataManager)
        recycler.setRowManager(rowManager)
        recycler.layoutManager = RecyclerListLayoutGridBagVertical(RECYCLER_SPAN)
        dataManager.init().onComplete {
            observeClickShort(dataManager)
            enableTouch = true
        }
        return recycler
    }

    override fun onOpen(hasBeenReconstructed: Boolean, hasBeenRestarted: Boolean) {
        super.onOpen(hasBeenReconstructed, hasBeenRestarted)
        setToolbarTittle(getTitleIdResource())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if(enableTouch){
            onTouchEventHandler.onTouch(requireView(), event)
        } else {
            true
        }
    }

    private fun observeClickShort(dataManager: PhotoDataManager){
        dataManager.observe(ObserverEvent<Event.Is, CompletableDeferred<ItemPhoto?>>(this, ON_CLICK_SHORT){
            onComplete { _, deferred ->
                deferred?.takeIf { deferred.isCompleted && it.getCompleted() != null }?.let {
                    val itemPhoto = deferred.getCompleted()!!
                    val state = DialogPhotoDownload.State()
                    val param = state.obtainParam()
                    param.itemPhoto = itemPhoto
                    Navigate.To(DialogPhotoDownload::class.java, state)
                }
            }
        })
    }

    override fun requestViewUpdate(what: Int?, arg: NavigationArguments?): Boolean {
        if(what == FILTER_UPDATED){
            enableTouch = false
            val recycler = view as RecyclerListGridBag
            PostToHandler.of(recycler){
                runSafe {
                    val itemFilter: ItemFilter = (arg as com.tezov.gofo.navigation.NavigationArguments).getFilter()
                    UnsplashCacheProvider.quit()
                    AppHandler.quit()
                    AppHandler.init()
                    UnsplashCacheProvider.init()
                    val provider = getProvider(itemFilter)
                    val dataManager = PhotoDataManager(provider)
                    val rowManager = PhotoRowManager(dataManager)
                    recycler.setRowManager(rowManager)
                    dataManager.init().onComplete {
                        observeClickShort(dataManager)
                        enableTouch = true
                    }
                }
            }
            return true
        }
        return super.requestViewUpdate(what, arg)
    }

    open class State : FragmentBase.State() {
        override fun newParam(): Param {
            return Param()
        }
    }
    open class Param : FragmentBase.Param(){

    }
}