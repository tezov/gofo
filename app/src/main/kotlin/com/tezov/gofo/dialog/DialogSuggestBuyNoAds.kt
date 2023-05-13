package com.tezov.gofo.dialog

import com.tezov.lib_java.debug.DebugException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.tezov.gofo.application.AppConfig.AD_SUGGEST_PAID_VERSION_MODULO
import com.tezov.gofo.application.SharePreferenceKey.SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT
import com.tezov.gofo.application.SharePreferenceKey.SP_SUGGEST_BUY_PAID_VERSION_DO_NOT_SHOW_BOOL
import com.tezov.lib_java_android.ui.dialog.modal.DialogModalWebview
import com.tezov.lib_java.async.notifier.observer.state.ObserverStateE
import com.tezov.lib_java_android.playStore.PlayStoreBilling
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib_java.async.notifier.observer.value.ObserverValueE
import com.android.billingclient.api.Purchase
import com.tezov.gofo.R
import com.tezov.gofo.application.AppInfo
import com.tezov.gofo.application.Application
import com.tezov.lib_java_android.ui.view.status.StatusParam
import com.tezov.lib_java.async.notifier.task.TaskValue
import com.tezov.lib_java_android.ui.navigation.Navigate
import com.tezov.lib_java.async.notifier.observer.event.ObserverEvent
import com.tezov.lib_java.async.notifier.task.TaskState
import com.tezov.lib_java.toolbox.Compare
import com.tezov.lib_java.type.defEnum.Event
import com.tezov.lib_java_android.application.SharedPreferences

class DialogSuggestBuyNoAds : DialogModalWebview() {
    override fun onBackPressed(): Boolean {
        return !isCancelable
    }
    override fun onConfirm() {
        isCancelable = false
        buy().observe(object : ObserverStateE(this) {
            override fun onComplete() {
                close()
                postConfirm()
            }
            override fun onException(e: Throwable?) {
/*#-debug-> DebugException.start().log(e).end() <-debug-#*/
                close()
                postCancel()
            }
        })
    }
    override fun onCheckBoxChange(flag: Boolean) {
        doNotShowValue = flag
    }
    private fun buy(): TaskState.Observable {
        val task = TaskState()
        val SKU = Application.SKU_NO_ADS
        val billing = PlayStoreBilling()
        val gr = RunnableGroup(myClass()).name("buy")
        val LBL_DISCONNECT = gr.label()
        val KEY_PURCHASE = gr.key()
        val KEY_OWNED = gr.key()
        gr.add(object : RunnableGroup.Action() {
            override fun runSafe() {
                billing.connect().observe(object : ObserverStateE(this) {
                    override fun onComplete() {
                        next()
                    }
                    override fun onException(e: Throwable?) {
                        putException(e)
                        skipUntilLabel(LBL_DISCONNECT)
                    }
                })
            }
        }.name("connect"))
        gr.add(object : RunnableGroup.Action() {
            override fun runSafe() {
                billing.buy(SKU, false).observe(object : ObserverValueE<Purchase?>(this) {
                    override fun onComplete(purchase: Purchase?) {
                        put(KEY_OWNED, true)
                        put(KEY_PURCHASE, purchase)
                        next()
                    }
                    override fun onException(purchase: Purchase?, e: Throwable?) {
                        AppInfo.toast(
                            R.string.lbl_billing_error,
                            StatusParam.DELAY_INFO_LONG_ms,
                            StatusParam.Color.FAILED,
                            true
                        )
                        putException(e)
                        skipUntilLabel(LBL_DISCONNECT)
                    }
                })
            }
        }.name("buy"))
        gr.add(object : RunnableGroup.Action(LBL_DISCONNECT) {
            override fun runSafe() {
                billing.disconnect().observe(object : ObserverStateE(this) {
                    override fun onComplete() {
                        done()
                    }
                    override fun onException(e: Throwable?) {
                        done()
                    }
                })
            }
        }.name("disconnect"))
        gr.setOnDone(object : RunnableGroup.Action() {
            override fun runSafe() {
                val e = getException()
                if (e != null) {
                    Application.setOwnedNoAds(false)
                    task.notifyException(e)
                } else if (Compare.isFalseOrNull(get(KEY_OWNED))) {
                    Application.setOwnedNoAds(false)
                    task.notifyComplete()
                } else {
                    val purchase: Purchase? = get(KEY_PURCHASE)
                    if (purchase != null) {
                        //NOW ?
                    }
                    Application.setOwnedNoAds(true)
                    task.notifyComplete()
                }
            }
        })
        gr.start()
        return task.observable
    }

    companion object {
        @JvmStatic
        fun open(checkBoxDoNotShowAgainHide: Boolean): TaskValue<Boolean>.Observable {
            val task = TaskValue<Boolean>()
            val state = State()
            val param = state.obtainParam()
            param.setConfirmButtonText(R.string.btn_buy)
            param.setCancelButtonText(R.string.btn_maybe_later)
            if (!checkBoxDoNotShowAgainHide) {
                param.setCheckBoxText(R.string.chk_do_not_show_again)
                param.isChecked = doNotShowValue
                resetNotShowCounter()
            }
            param.setRawFileId(R.raw.suggest_buy_no_ads)
            Navigate.To(DialogSuggestBuyNoAds::class.java, state)
                .observe(object : ObserverValueE<DialogSuggestBuyNoAds?>(myClass()) {
                    override fun onComplete(dialog: DialogSuggestBuyNoAds?) {
                        dialog!!.observe(object :
                            ObserverEvent<Event.Is?, Any?>(myClass(), Event.ON_CANCEL) {
                            override fun onComplete(event: Event.Is?, `object`: Any?) {
                                task.notifyComplete(false)
                            }
                        })
                        dialog.observe(object :
                            ObserverEvent<Event.Is?, Any?>(myClass(), Event.ON_CONFIRM) {
                            override fun onComplete(event: Event.Is?, `object`: Any?) {
                                task.notifyComplete(true)
                            }
                        })
                    }
                    override fun onException(dialog: DialogSuggestBuyNoAds?, e: Throwable?) {
                        task.notifyException(e)
                    }
                })
            return task.observable
        }
        var doNotShowValue: Boolean
            get() {
                val sp = Application.sharedPreferences()
                return Compare.isTrue(sp.getBoolean(SP_SUGGEST_BUY_PAID_VERSION_DO_NOT_SHOW_BOOL))
            }
            private set(flag) {
                val sp = Application.sharedPreferences()
                sp.put(SP_SUGGEST_BUY_PAID_VERSION_DO_NOT_SHOW_BOOL, flag)
            }

        private fun resetNotShowCounter() {
            val sp = Application.sharedPreferences()
            sp.put(SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT, 0)
        }
        private fun incNotShowCounter() {
            val sp = Application.sharedPreferences()
            var adPaidVersionCounter = sp.getInt(SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT)
            if (adPaidVersionCounter == null) {
                sp.put(SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT, 1)
            } else {
                sp.put(SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT, ++adPaidVersionCounter)
            }
        }

        fun canShow(): Boolean {
            return if (doNotShowValue) {
                false
            } else {
                val result: Boolean =
                    getNotShowCounter(Application.sharedPreferences()) >= AD_SUGGEST_PAID_VERSION_MODULO
                if (!result) {
                    incNotShowCounter()
                }
                result
            }
        }

        private fun getNotShowCounter(sp: SharedPreferences): Int {
            var adPaidVersionCounter = sp.getInt(SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT)
            adPaidVersionCounter?:let{
                sp.put(SP_SUGGEST_BUY_PAID_VERSION_COUNTER_INT, AD_SUGGEST_PAID_VERSION_MODULO)
                adPaidVersionCounter = AD_SUGGEST_PAID_VERSION_MODULO
            }
            return adPaidVersionCounter
        }

        private fun myClass(): Class<DialogSuggestBuyNoAds> {
            return DialogSuggestBuyNoAds::class.java
        }

        fun isOwned(SKU: String?): TaskValue<Boolean>.Observable {
            val task = TaskValue<Boolean>()
            val billing = PlayStoreBilling()
            val gr = RunnableGroup(myClass()).name("isOwned")
            val LBL_DISCONNECT = gr.label()
            val KEY_OWNED = gr.key()
            gr.add(object : RunnableGroup.Action() {
                override fun runSafe() {
                    billing.connect().observe(object : ObserverStateE(this) {
                        override fun onComplete() {
                            next()
                        }

                        override fun onException(e: Throwable) {
                            putException(e)
                            skipUntilLabel(LBL_DISCONNECT)
                        }
                    })
                }
            }.name("connect"))
            gr.add(object : RunnableGroup.Action() {
                override fun runSafe() {
                    billing.isOwned(SKU, false).observe(object : ObserverValueE<Boolean?>(this) {
                        override fun onComplete(owned: Boolean?) {
                            put(KEY_OWNED, owned)
                            if (owned!!) {
                                skipUntilLabel(LBL_DISCONNECT)
                            } else {
                                next()
                            }
                        }
                        override fun onException(owned: Boolean?, e: Throwable?) {
                            putException(e)
                            skipUntilLabel(LBL_DISCONNECT)
                        }
                    })
                }
            }.name("check if owned"))
            gr.add(object : RunnableGroup.Action(LBL_DISCONNECT) {
                override fun runSafe() {
                    billing.disconnect().observe(object : ObserverStateE(this) {
                        override fun onComplete() {
                            done()
                        }

                        override fun onException(e: Throwable) {
                            done()
                        }
                    })
                }
            }.name("disconnect"))
            gr.setOnDone(object : RunnableGroup.Action() {
                override fun runSafe() {
                    val e = getException()
                    if (e != null) {
                        Application.setOwnedNoAds(false)
                        task.notifyException(null, e)
                    } else {
                        val result = Compare.isTrue(get(KEY_OWNED))
                        Application.setOwnedNoAds(result)
                        task.notifyComplete(result)
                    }
                }
            })
            gr.start()
            return task.observable
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view!!.findViewById<Button>(com.tezov.lib_java_android.R.id.btn_confirm)!!.isEnabled = false
        return view;
    }
}