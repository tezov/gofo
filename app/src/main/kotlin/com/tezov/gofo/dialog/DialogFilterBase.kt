package com.tezov.gofo.dialog

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.gofo.application.Application
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib_java_android.ui.dialog.modal.DialogModalRequest
import com.tezov.lib_java_android.ui.component.plain.FocusCemetery
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.addRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.build
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.setOnDoneRun
import com.tezov.gofo.room.data.ItemFilter

abstract class DialogFilterBase : DialogModalRequest() {
    override fun obtainParam(): Param {
        return super.obtainParam() as Param
    }
    override fun getParam(): Param {
        return super.getParam() as Param
    }
    override fun enableScrollbar(): Boolean {
        return true
    }

    protected lateinit var filter: ItemFilter
        private set

    protected abstract fun getFilterIdKey():String

    override fun onPrepare(hasBeenReconstructed: Boolean) {
        super.onPrepare(hasBeenReconstructed)
        val sp = Application.sharedPreferences()
        filter = sp.getLong(getFilterIdKey())?.let{ ItemFilter.load(it) }?:let{ ItemFilter() }
    }

    final override fun onConfirm() {
        btnConfirm.isEnabled = false
        FocusCemetery.request(view)
        RunnableGroup(this).build<Unit> {
            val KEY_PREVIOUS_FILTER_ID = key()
            val KEY_NEXT_FILTER_ID = key()
            addRun {
                put(KEY_PREVIOUS_FILTER_ID, filter.id)
                onConfirm_beforeSave().onComplete {
                    if(it.getCompleted()){
                        next()
                    }
                    else{
                        putException("canceled before save")
                        done()
                    }
                }
            }.name("before save")
            addRun {
                if(filter.save()){
                    put(KEY_NEXT_FILTER_ID, filter.id)
                    val sp = Application.sharedPreferences()
                    sp.put(getFilterIdKey(), filter.id)
                    next()
                }
                else{
                    putException("failed to save")
                    done()
                }
            }.name("save")
            addRun {
                onConfirm_afterSave(get(KEY_PREVIOUS_FILTER_ID), get(KEY_NEXT_FILTER_ID)).onComplete {
                    next()
                }
            }.name("after save")
            setOnDoneRun {
                if(exception == null){
                    postConfirm(filter)
                }
                else{
/*#-debug-> DebugException.start().log(exception).end() <-debug-#*/
                    postCancel()
                }
                close()
            }
            start()
        }
    }

    abstract fun onConfirm_beforeSave():Deferred<Boolean>
    abstract fun onConfirm_afterSave(previousFilterId:Long?, nextFilterId:Long):Deferred<Unit>

    class State : DialogModalRequest.State() {
        override fun newParam(): Param {
            return Param()
        }

        override fun obtainParam(): Param {
            return super.obtainParam() as Param
        }
    }
    class Param : DialogModalRequest.Param()

}