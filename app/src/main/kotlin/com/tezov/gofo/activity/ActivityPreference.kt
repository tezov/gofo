package com.tezov.gofo.activity

import android.os.Build
import com.tezov.gofo.dialog.DialogSuggestBuyNoAds.Companion.open
import com.tezov.gofo.navigation.ToolbarContent
import com.tezov.gofo.R
import android.os.Bundle
import com.tezov.lib_java_android.application.AppDisplay
import com.tezov.gofo.navigation.ToolbarHeaderBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.Preference
import com.tezov.gofo.application.SharePreferenceKey.SP_DESTINATION_DIRECTORY_STRING
import com.tezov.gofo.application.AppInfo
import com.tezov.gofo.application.Application
import com.tezov.lib_java_android.wrapperAnonymous.PreferenceOnClickListenerW
import com.tezov.gofo.dialog.DialogSuggestAppRating
import com.tezov.lib_java.async.notifier.observer.event.ObserverEvent
import com.tezov.lib_java.async.notifier.observer.state.ObserverStateE
import com.tezov.lib_java.async.notifier.observer.value.ObserverValueE
import com.tezov.lib_java.type.defEnum.Event
import com.tezov.lib_java.type.runnable.RunnableW
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.application.SharePreferenceKey.FILE_NAME_SHARE_PREFERENCE
import com.tezov.lib_java_android.application.SharedPreferences
import com.tezov.lib_java_android.file.StorageTree
import com.tezov.lib_java_android.playStore.PlayStore
import com.tezov.lib_java_android.toolbox.PostToHandler
import com.tezov.lib_java_android.ui.activity.ActivityPreference

class ActivityPreference : ActivityPreference() {
    var toolbarContent: ToolbarContent? = null
        private set

    override fun getLayoutId(): Int {
        return R.layout.activity_preference
    }

    override fun getPreferenceContainerId(): Int {
        return R.id.container_fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppDisplay.setOrientationPortrait(true)
        toolbarContent = ToolbarContent(this)
        AppInfo.privacyPolicySetOnClickListener(viewRoot, null)
        AppInfo.contactSetOnClickListener(viewRoot)
    }

    override fun onOpen(hasBeenReconstructed: Boolean, hasBeenRestarted: Boolean) {
        super.onOpen(hasBeenReconstructed, hasBeenRestarted)
        setToolbarTittle(R.string.activity_privacy_policy_title)
    }

    protected fun <DATA> setToolbarTittle(data: DATA?) {
        val toolbarContent = toolbarContent
        if (data == null) {
            toolbarContent!!.setToolBarView(null)
        } else {
            val header = ToolbarHeaderBuilder().setData(data)
            toolbarContent!!.setToolBarView(header.build(getToolbar()))
        }
    }

    override fun onCreateMenu(): Boolean {
        val toolbar = getToolbar()
        toolbar.visibility = View.VISIBLE
        val toolbarBottom = getToolbarBottom()
        toolbarBottom.visibility = View.GONE
        return true
    }

    override fun createFragmentPreference(): FragmentPreference {
        return FragmentPreference(R.xml.preference)
    }

    class FragmentPreference : FragmentSharePreference {
        constructor() {}
        internal constructor(xmlId: Int) : super(xmlId) {}

        override fun getSharedPreferencesName(): String {
            return FILE_NAME_SHARE_PREFERENCE
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val view = super.onCreateView(inflater, container, savedInstanceState)
            findPreference(R.id.pref_app_version).summary = AppContext.getResources()
                .getString(R.string.application_version) + "/" + Build.VERSION.SDK_INT
            findPreference(R.id.pref_app_share).onPreferenceClickListener =
                object : PreferenceOnClickListenerW() {
                    override fun onClicked(preference: Preference): Boolean {
                        setOnClickListenerEnabled(preference, false)
                        shareApp()
                        return true
                    }
                }
            val preferenceAppRating = findPreference(R.id.pref_app_rating)
            if (!DialogSuggestAppRating.isAlreadyDone()) {
                preferenceAppRating.isVisible = true
                preferenceAppRating.onPreferenceClickListener =
                    object : PreferenceOnClickListenerW() {
                        override fun onClicked(preference: Preference): Boolean {
                            setOnClickListenerEnabled(preference, false)
                            openDialogSuggestReview()
                            return true
                        }
                    }
            }
            updatePref_DestinationFolder(
                sp, findPreference(R.id.pref_destination_directory)
            )
            return view
        }

        override fun onResume() {
            super.onResume()
            updatePrefSkuNoAds(Application.isOwnedNoAds())
        }

        override fun onSharedPreferenceChanged(
            sp: SharedPreferences,
            key: String,
            keyDecoded: String
        ) {
            super.onSharedPreferenceChanged(sp, key, keyDecoded)
            if (SP_DESTINATION_DIRECTORY_STRING.equals(keyDecoded)) {
                updatePref_DestinationFolder(sp, findPreference(key))
            }
        }

        private fun updatePref_DestinationFolder(sp: SharedPreferences, pref: Preference?) {
            var value = sp.getString(SP_DESTINATION_DIRECTORY_STRING)
            if (value != null) {
                val uriTree = StorageTree.fromLink(value)
                if (uriTree != null) {
                    value = if (uriTree.canWrite()) {
                        uriTree.displayPath
                    } else {
                        sp.remove(SP_DESTINATION_DIRECTORY_STRING)
                        null
                    }
                }
            }
            val summary: String
            if (value == null) {
                summary = AppContext.getResources()
                    .getString(R.string.pref_destination_directory_android_summary)
            } else {
                summary =
                    AppContext.getResources().getString(R.string.pref_destination_directory_summary)
            }
            pref!!.summary = String.format(summary, value)
        }

        private fun updatePrefSkuNoAds(isOwnedNoAds: Boolean) {
            val sp = Application.sharedPreferences()
            val keySkuNoAds =
                sp.encodeKey(AppContext.getResources().getIdentifierName(R.id.pref_sku_no_ads))
            val prefSkuNoAds = findPreference<Preference>(keySkuNoAds)
            if (isOwnedNoAds) {
                prefSkuNoAds!!.setTitle(R.string.pref_sku_no_ads_owned_title)
                prefSkuNoAds.setSummary(R.string.pref_sku_no_ads_owned_summary)
                prefSkuNoAds.setIcon(R.drawable.ic_confirm_outline_24dp)
                prefSkuNoAds.isEnabled = false
            } else {
                prefSkuNoAds!!.setTitle(R.string.pref_sku_no_ads_buy_full_version_title)
                prefSkuNoAds.setSummary(R.string.pref_sku_no_ads_buy_full_version_summary)
                prefSkuNoAds.setIcon(R.drawable.ic_buy_24dp)
                prefSkuNoAds.isEnabled = true
                prefSkuNoAds.onPreferenceClickListener = object : PreferenceOnClickListenerW() {
                    override fun onClicked(preference: Preference): Boolean {
                        setOnClickListenerEnabled(preference, false)
                        showSuggestBuyNoAds()
                        return true
                    }
                }
            }
        }

        private fun showSuggestBuyNoAds() {
            open(true).observe(object : ObserverValueE<Boolean>(this) {
                override fun onComplete(isOwned: Boolean) {
                    if (isOwned) {
                        PostToHandler.of(view, object : RunnableW() {
                            override fun runSafe() {
                                updatePrefSkuNoAds(true)
                            }
                        })
                    }
                    setOnClickListenerEnabled(R.id.pref_sku_no_ads, true)
                }

                override fun onException(isOwned: Boolean, e: Throwable) {
                    setOnClickListenerEnabled(R.id.pref_sku_no_ads, true)
                    //DebugException.start().log((e)).end();
                }
            })
        }

        private fun openDialogSuggestReview() {
            DialogSuggestAppRating.open()
                .observe(object : ObserverValueE<DialogSuggestAppRating>(this) {
                    override fun onComplete(dialog: DialogSuggestAppRating) {
                        dialog.observe(object :
                            ObserverEvent<Event.Is?, Any?>(this, Event.ON_CLOSE) {
                            override fun onComplete(event: Event.Is?, value: Any?) {
                                val preferenceAppRating = findPreference(R.id.pref_app_rating)
                                if(DialogSuggestAppRating.isAlreadyDone()) {
                                    preferenceAppRating.isVisible = false
                                } else {
                                    setOnClickListenerEnabled(preferenceAppRating, true)
                                }
                            }
                        })
                    }
                    override fun onException(dialog: DialogSuggestAppRating, e: Throwable) {
                        setOnClickListenerEnabled(R.id.pref_app_rating, true)
                    }
                })
        }

        private fun shareApp() {
            PlayStore.shareLink().observe(object : ObserverStateE(this) {
                override fun onComplete() {
                    setOnClickListenerEnabled(R.id.pref_app_share, true)
                }

                override fun onException(e: Throwable) {
                    setOnClickListenerEnabled(R.id.pref_app_share, true)
                }
            })
        }
    }
}