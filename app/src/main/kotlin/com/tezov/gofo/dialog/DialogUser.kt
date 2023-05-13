package com.tezov.gofo.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tezov.gofo.R
import com.tezov.gofo.application.AppInfo
import com.tezov.gofo.glide.GlideApp
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.ui.dialog.DialogNavigable
import com.tezov.lib.adapterJavaToKotlin.wrapperAnonymous.ViewOnclickListenerW
import com.tezov.gofo.room.data.ItemPhoto

class DialogUser : DialogNavigable() {

    override fun newState(): DialogNavigable.State {
        return State()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val param: Param = getParam()
        val view: View = inflater.inflate(R.layout.dialog_user,container,false)
        val itemPhoto = param.itemPhoto!!
        val dataUser = itemPhoto.getDataPhoto()?.user

        dataUser?.name?.let {
            val lbl = view.findViewById<TextView>(R.id.lbl_author)
            lbl.text = it
        }
        dataUser?.images?.medium?.let {
            val img = view.findViewById<ImageView>(R.id.img_author)
            GlideApp.with(AppContext.get())
                .load(it)
                .transform(RoundedCorners(10))
                .into(img)
        }
        dataUser?.bio?.let {
            val lbl = view.findViewById<TextView>(R.id.lbl_bio)
            lbl.text = it
        }
        dataUser?.location?.let {
            val lbl = view.findViewById<TextView>(R.id.lbl_location)
            lbl.text = it
        }
        dataUser?.photoCount?.let {
            val lbl = view.findViewById<TextView>(R.id.lbl_photo_count)
            lbl.text = it.toString()
        }
        val profileClickListener = ViewOnclickListenerW{
            onClicked {
                dataUser?.links?.html?.let {
                    AppInfo.openLink(it)
                }
            }
        }
        view.findViewById<View>(R.id.btn_open_profile).setOnClickListener(profileClickListener)
        return view
    }

    class State : DialogNavigable.State() {
        override fun newParam(): Param {
            return Param()
        }

        override fun obtainParam(): Param {
            return super.obtainParam() as Param
        }
    }
    class Param : DialogNavigable.Param(){
        var itemPhoto:ItemPhoto? = null

    }

}