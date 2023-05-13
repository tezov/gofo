package com.tezov.gofo.activity

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.lib.adapterJavaToKotlin.observer.Observable_Kext.observe
import com.tezov.lib.adapterJavaToKotlin.observer.Observable_Kext.observeE
import com.tezov.gofo.navigation.ToolbarContent
import com.tezov.gofo.R
import android.os.Bundle
import com.tezov.lib_java_android.application.AppDisplay
import com.tezov.lib_java_android.ui.component.plain.ButtonIconMaterial
import com.tezov.lib_java_android.ui.navigation.NavigatorManager
import com.tezov.lib_java.async.notifier.task.TaskValue
import com.tezov.lib_java_android.ui.navigation.defMenuListener
import android.view.MenuItem
import android.view.View
import com.tezov.gofo.application.SharePreferenceKey.SP_NAVIGATION_LAST_DESTINATION_STRING
import com.tezov.gofo.application.Application
import com.tezov.gofo.dialog.*
import com.tezov.gofo.navigation.NavigationHelper
import com.tezov.lib_java.type.defEnum.Event
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.ui.dialog.DialogNavigable
import com.tezov.lib_java_android.ui.fragment.FragmentNavigable
import com.tezov.lib_java_android.ui.navigation.NavigatorManager.NavigatorKey.FRAGMENT
import com.tezov.gofo.fragment.FragmentFeed
import com.tezov.gofo.fragment.FragmentRandom
import com.tezov.gofo.fragment.FragmentRecyclerBase.Companion.FILTER_UPDATED
import com.tezov.gofo.fragment.FragmentSearch
import com.tezov.gofo.navigation.NavigationArguments
import com.tezov.lib.adapterJavaToKotlin.Navigate
import com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent
import com.tezov.lib.adapterJavaToKotlin.wrapperAnonymous.ViewOnclickListenerW
import com.tezov.gofo.room.data.ItemFilter

abstract class ActivityMain : ActivityBase() {
    var toolbarContent: ToolbarContent? = null
        private set

    override fun getState(): State? {
        return super.getState() as? State
    }
    override fun newState(): State {
        return State()
    }
    override fun obtainState(): State {
        return super.obtainState() as State
    }

    override fun getLayoutId(): Int {
        return R.layout.tpl_activity_tbc_tbba_overlap_fade
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppDisplay.setOrientationPortrait(true)
        toolbarContent = ToolbarContent(this)
    }
    override fun onPrepare(hasBeenReconstructed: Boolean) {
        super.onPrepare(hasBeenReconstructed)
        if (!hasBeenReconstructed) {
            val source = Navigate.getSource(this)
            if (source == null) {
                var destination: NavigationHelper.DestinationKey.Is? = null
                val sp = com.tezov.lib_java_android.application.Application.sharedPreferences()
                val destinationString = sp.getString(SP_NAVIGATION_LAST_DESTINATION_STRING)
                if (destinationString != null) {
                    destination = NavigationHelper.DestinationKey.find(destinationString)
                }
                if (destination == null) {
                    destination = NavigationHelper.DestinationKey.FRAGMENT_SEARCH
                }
                Navigate.observe(this, FRAGMENT){
                    val id = destination!!.id
                    onComplete{ _ , event ->
                        if (event == NavigatorManager.Event.NAVIGATE_TO_CONFIRMED) {
                            unsubscribe()
                            getToolbarBottom().setChecked(id)
                        }
                    }
                }
                Navigate.To(destination!!)
                mustShowSuggestBuy().observe(this){
                    onComplete { mustShow ->
                        if (mustShow!!) {
                            showSuggestBuy()
                        }
                    }
                }
            }
        }
    }

    private fun mustShowSuggestBuy(): TaskValue<Boolean>.Observable {
        val task = TaskValue<Boolean>()
        DialogSuggestBuyNoAds.isOwned(Application.SKU_NO_ADS).observeE(this) {
            onComplete{ isOwned ->
                task.notifyComplete(!isOwned!! && DialogSuggestBuyNoAds.canShow())
            }
            onException{ _, e ->
/*#-debug-> DebugException.start().log(e).end() <-debug-#*/
                task.notifyComplete(false)
            }
        }
        return task.observable
    }
    private fun showSuggestBuy() {
        DialogSuggestBuyNoAds.open(false).observeE(this)  {
            onComplete{ _ ->

            }
            onException{ _, e ->
                //DebugException.start().log(e).end();
                onComplete(false)
            }
        }
    }

    override fun onCreateMenu(): Boolean {
        val toolbar = getToolbar()
        toolbar.inflateMenu(R.menu.toolbar)
        val toolbarBottom = getToolbarBottom()
        toolbarBottom.visibility = View.VISIBLE
        toolbarBottom.inflateMenu(R.menu.toolbar_bottom)

        val btn = findViewById<ButtonIconMaterial>(R.id.btn_action)
        btn.visibility = View.VISIBLE
        val icFilter = AppContext.getResources().getDrawable(R.drawable.ic_filter_24dp)
        btn.icon = icFilter
        btn.iconTint = AppContext.getResources().getColorStateList(R.color.White)
        btn.setOnClickListener(ViewOnclickListenerW{
            fun <N : FragmentNavigable?> observeConfirm(dialog: DialogNavigable){
                val observer = ObserverEvent<Event.Is, Any>(this,  Event.ON_CONFIRM){
                    onComplete { _, value ->
                        val fr = Navigate.getCurrentFragmentRef<N>()!!
                        val arguments = NavigationArguments.create()
                        arguments.setFilter( value as ItemFilter)
                        fr.requestViewUpdate(FILTER_UPDATED, arguments)
                    }
                }
                dialog.observe(observer)
            }
            fun openDialogFilterRandom(){
                val state = DialogFilterBase.State()
                val param = state.obtainParam()
                param.setTitle(R.string.lbl_dialog_filter_title)
                    .setCancelButtonText(R.string.btn_cancel)
                    .setConfirmButtonText(R.string.btn_confirm)
                Navigate.To(DialogFilterRandom::class.java, state).observeE(this) {
                    onComplete{ dialog ->
                        observeConfirm<FragmentRandom>(dialog!!)
                    }
                    onException{ _, e ->

                    }
                }
            }
            fun openDialogFilterSearch(){
                val state = DialogFilterBase.State()
                val param = state.obtainParam()
                param.setTitle(R.string.lbl_dialog_filter_title)
                    .setCancelButtonText(R.string.btn_cancel)
                    .setConfirmButtonText(R.string.btn_confirm)
                Navigate.To(DialogFilterSearch::class.java, state).observeE(this) {
                    onComplete{ dialog ->
                        observeConfirm<FragmentSearch>(dialog!!)
                    }
                    onException{ _, e ->

                    }
                }
            }
            fun openDialogFilterFeed(){
                val state = DialogFilterBase.State()
                val param = state.obtainParam()
                param.setTitle(R.string.lbl_dialog_filter_title)
                    .setCancelButtonText(R.string.btn_cancel)
                    .setConfirmButtonText(R.string.btn_confirm)
                Navigate.To(DialogFilterFeed::class.java, state).observeE(this) {
                    onComplete{ dialog ->
                        observeConfirm<FragmentFeed>(dialog!!)
                    }
                    onException{ _, e ->

                    }
                }
            }
            onClicked {
                val current = Navigate.getCurrent()
                current?.let {
                    when (it) {
                        NavigationHelper.DestinationKey.FRAGMENT_RANDOM -> {
                            openDialogFilterRandom()
                        }
                        NavigationHelper.DestinationKey.FRAGMENT_SEARCH -> {
                            openDialogFilterSearch()
                        }
                        NavigationHelper.DestinationKey.FRAGMENT_FEED -> {
                            openDialogFilterFeed()
                        }
                    }
                }
            }
        })
        return true
    }
    override fun onMenuItemSelected(uiType: defMenuListener.Type, `object`: Any): Boolean {
        if (uiType == defMenuListener.Type.TOOLBAR) {
            val menuItem = `object` as MenuItem
            if (menuItem.itemId == R.id.mn_setting) {
                Navigate.To(NavigationHelper.DestinationKey.PREFERENCE)
            }
            return true
        }
        if (uiType == defMenuListener.Type.TOOLBAR_BOTTOM) {
            val sp = Application.sharedPreferences()
            val menuItem = `object` as MenuItem
            when (menuItem.itemId) {
                R.id.mn_fragment_feed -> {
                    sp.put(
                        SP_NAVIGATION_LAST_DESTINATION_STRING,
                        NavigationHelper.DestinationKey.FRAGMENT_FEED.name()
                    )
                    Navigate.To(NavigationHelper.DestinationKey.FRAGMENT_FEED)
                }
                R.id.mn_fragment_random -> {
                    sp.put(
                        SP_NAVIGATION_LAST_DESTINATION_STRING,
                        NavigationHelper.DestinationKey.FRAGMENT_RANDOM.name()
                    )
                    Navigate.To(NavigationHelper.DestinationKey.FRAGMENT_RANDOM)
                }
                R.id.mn_fragment_search -> {
                    sp.put(
                        SP_NAVIGATION_LAST_DESTINATION_STRING,
                        NavigationHelper.DestinationKey.FRAGMENT_SEARCH.name()
                    )
                    Navigate.To(NavigationHelper.DestinationKey.FRAGMENT_SEARCH)
                }
            }
            return true
        }
        return false
    }

    class State : ActivityBase.State() {


    }
}