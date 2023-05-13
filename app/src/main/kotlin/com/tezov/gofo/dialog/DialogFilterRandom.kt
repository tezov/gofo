package com.tezov.gofo.dialog

import com.tezov.gofo.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import com.tezov.gofo.application.SharePreferenceKey
import com.google.android.flexbox.FlexboxLayout
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.toolbox.Compare
import com.tezov.lib_java_android.application.AppKeyboard
import com.tezov.lib_java_android.ui.component.plain.ButtonIconMaterial
import com.tezov.lib_java_android.ui.form.component.plain.FormEditText
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.lib.adapterJavaToKotlin.wrapperAnonymous.ViewOnclickListenerW
import com.tezov.lib.adapterJavaToKotlin.async.PostToHandler
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterRandom
import com.tezov.gofo.room.database.Database

class DialogFilterRandom : DialogFilterBase() {
    lateinit var dataFilter: DataFilterRandom
    lateinit var dataFilterFingerPrint: ByteArray
    lateinit var frmTag: FormEditText
    lateinit var lblTags: FlexboxLayout
    lateinit var spnOrientation: Spinner
    lateinit var btnDeleteTags: ButtonIconMaterial

    override fun getFrameLayoutId(): Int {
        return R.layout.dialog_filter_random
    }
    override fun getFilterIdKey(): String {
        return SharePreferenceKey.KEY_FILTER(ItemFilter.Type.RANDOM)
    }

    override fun onFrameMerged(view: View, savedInstanceState: Bundle?) {
        frmTag = view.findViewById(R.id.frm_tag)
        lblTags = view.findViewById(R.id.lbl_tags)
        spnOrientation = view.findViewById(R.id.spn_orientation)
        btnDeleteTags = view.findViewById(R.id.btn_delete_tags)
    }

    override fun onPrepare(hasBeenReconstructed: Boolean) {
        super.onPrepare(hasBeenReconstructed)
        filter.getData<DataFilterRandom>()?.let {
            dataFilter = it
        }
        ?: let {
            dataFilter = DataFilterRandom(null)
        }
        dataFilterFingerPrint = dataFilter.getFingerPrint()
        addTags(dataFilter.getTags(), false)
        btnDeleteTags.setOnClickListener(ViewOnclickListenerW{
            onClicked {
                clearTags()
            }
        })
        with(spnOrientation){
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    dataFilter.setOrientation(position)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
            dataFilter.orientation?.let {
                setSelection(it.ordinal + 1)
            } ?: let {
                setSelection(0)
            }
        }
        frmTag.link(object : FormEditText.EntryString(){
            override fun <T : Any?> onSetValue(type: Class<T>?) {
                value?.let {
                    addTags(value)
                    frmTag.setText(null)
                }
            }
        })
        AppKeyboard.show(frmTag)
    }

    private fun clearTags(){
        PostToHandler.of(lblTags) {
            runSafe {
                dataFilter.clearTag()
                lblTags.removeAllViews()
            }
        }
    }
    private fun addTags(tags:String){
        addTags(tags.split(",").map{ it.trim() }.toSet(), true)
    }
    private fun addTags(tags:Set<String>, addToFilter: Boolean){
        PostToHandler.of(lblTags){
            runSafe {
                tags.forEach { tag ->
                    val addView = !addToFilter || dataFilter.addTag(tag)
                    if(addView){
                        val view = LayoutInflater.from(context).inflate(R.layout.flexbox_item_tag, lblTags, false)
                        val textView = view.findViewById<TextView>(R.id.lbl_tag)
                        textView.setText(tag)
                        val btnDelete = view.findViewById<ButtonIconMaterial>(R.id.btn_delete)
                        btnDelete.setOnClickListener(ViewOnclickListenerW{
                            onClicked {
                                dataFilter.removeTag(tag)
                                lblTags.removeView(view)
                            }
                        })
                        lblTags.addView(view)
                    }
                }
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