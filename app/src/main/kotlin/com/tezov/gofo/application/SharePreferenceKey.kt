package com.tezov.gofo.application

import com.tezov.gofo.R
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.application.SharePreferenceKey
import com.tezov.gofo.room.data.ItemFilter

object SharePreferenceKey : SharePreferenceKey() {

    const val SP_NAVIGATION_LAST_DESTINATION_STRING = "NAVIGATION_LAST_DESTINATION"
    const val SP_SUGGEST_BUY_PAID_VERSION_DO_NOT_SHOW_BOOL = "SUGGEST_BUY_PAID_VERSION_DO_NOT_SHOW"
    const val SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT = "SUGGEST_BUY_PAID_VERSION_COUNTER"
    const val SP_OWNED_NO_ADS_INT = "OWNED_NO_ADS"
    const val SP_SUGGEST_APP_RATING_ALREADY_DONE_BOOL = "SUGGEST_APP_RATING_ALREADY_DONE"

    fun KEY_FILTER(type: ItemFilter.Type):String{
        return makeKey("KEY_FILTER",type.name)
    }

    val SP_DESTINATION_DIRECTORY_STRING =
        AppContext.getResources().getIdentifierName(R.id.pref_destination_directory)

}