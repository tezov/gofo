package com.tezov.lib.adapterJavaToKotlin

import com.tezov.lib.adapterJavaToKotlin.observer.ObserverEvent
import com.tezov.lib_java.async.notifier.Notifier
import com.tezov.lib_java.async.notifier.task.TaskValue
import com.tezov.lib_java_android.ui.activity.ActivityNavigable
import com.tezov.lib_java_android.ui.dialog.DialogNavigable
import com.tezov.lib_java_android.ui.fragment.FragmentNavigable
import com.tezov.lib_java_android.ui.navigation.*
import com.tezov.lib_java_android.ui.navigation.Navigate
import com.tezov.lib_java_android.ui.navigation.NavigatorManager.NavigatorKey

object Navigate : Navigate() {
    inline fun observe(owner:Any, navigatorKey:NavigatorManager.NavigatorKey.Is? = null, init: ObserverEvent<NavigatorKey.Is, NavigatorManager.Event>.() -> Unit): Notifier.Subscription {
        return observe(ObserverEvent(owner, navigatorKey, init))
    }

    internal fun To(
        destinationKey: NavigatorManager.DestinationKey.Is,
        option: NavigationOption? = null,
        navigationArguments: NavigationArguments? = null
    ) {
        navigationArguments?.let {
            helper().navigateTo(destinationKey, option, true, it.arguments)
        }
            ?: let {
                helper().navigateTo(destinationKey, option, false, null)
            }
    }

    internal fun <D : DialogNavigable?> To(
        target: Class<D>,
        state: DialogNavigable.State,
        navigationArguments: NavigationArguments? = null
    ): TaskValue<D>.Observable {
        return helper().navigateTo(target, state, navigationArguments)
    }

    internal fun getSource(navigable: defNavigable): NavigatorManager.DestinationKey.Is? {
        return helper().getDestinationKeySource(navigable)
    }
    internal fun getSource(): NavigatorManager.DestinationKey.Is? {
        return helper().lastDestinationKeySource
    }

    internal fun getCurrent(): NavigatorManager.DestinationKey.Is? {
        return helper().lastDestinationKey
    }

    internal fun getCurrentFragment(): NavigatorManager.DestinationKey.Is? {
        val lastDestination = helper().getLastDestination(NavigatorKey.FRAGMENT, true)
        return lastDestination?.key
    }
    internal fun <N : FragmentNavigable?> getCurrentFragmentRef(): N {
        return helper().getLastRef(NavigatorKey.FRAGMENT, true)
    }

    internal fun getCurrentActivity(): NavigatorManager.DestinationKey.Is? {
        val lastDestination = helper().getLastDestination(NavigatorKey.ACTIVITY, true)
        return lastDestination?.key
    }

    internal fun <N : ActivityNavigable?> getCurrentActivityRef(): N {
        return helper().getLastRef(NavigatorKey.ACTIVITY, true)
    }


}