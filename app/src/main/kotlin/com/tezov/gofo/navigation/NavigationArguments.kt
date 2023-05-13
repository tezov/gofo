package com.tezov.gofo.navigation

import com.tezov.gofo.navigation.NavigationArguments.ArgumentKey.Companion.FILTER
import com.tezov.lib_java.type.collection.Arguments
import com.tezov.lib_java_android.application.Application
import com.tezov.lib_java_android.ui.navigation.defNavigable
import com.tezov.gofo.room.data.ItemFilter

typealias Is = com.tezov.lib_java_android.ui.navigation.NavigationArguments.ArgumentKey.Is

class NavigationArguments private constructor(
    ref: defNavigable?,
    arguments: Arguments<com.tezov.lib_java_android.ui.navigation.NavigationArguments.ArgumentKey.Is?>?
) : com.tezov.lib_java_android.ui.navigation.NavigationArguments(ref, arguments) {
    interface ArgumentKey : com.tezov.lib_java_android.ui.navigation.NavigationArguments.ArgumentKey{
        companion object {
            val FILTER = Is("TARGET")
        }
    }
    companion object {

        fun create(): NavigationArguments {
            val destinationManager = Application.navigationHelper().destinationManager
            return NavigationArguments(
                null,
                destinationManager.arguments<defNavigable?, com.tezov.lib_java_android.ui.navigation.NavigationArguments.ArgumentKey.Is?>(
                    null,
                    How.CREATE
                )
            )
        }

        fun obtain(ref: defNavigable): NavigationArguments {
            val destinationManager = Application.navigationHelper().destinationManager
            return NavigationArguments(ref, destinationManager.arguments(ref, How.OBTAIN))
        }

        fun get(ref: defNavigable): NavigationArguments {
            val destinationManager = Application.navigationHelper().destinationManager
            return NavigationArguments(ref, destinationManager.arguments(ref, How.GET))
        }

        fun wrap(arguments: Arguments<com.tezov.lib_java_android.ui.navigation.NavigationArguments.ArgumentKey.Is?>?): NavigationArguments {
            return NavigationArguments(null, arguments)
        }
    }

    fun getFilter():ItemFilter {
        return this.get<ItemFilter>(FILTER)
    }
    fun setFilter(data: ItemFilter) {
        this.put<ItemFilter>(FILTER, data)
    }
}