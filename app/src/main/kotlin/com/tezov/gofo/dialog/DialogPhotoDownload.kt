package com.tezov.gofo.dialog

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tezov.gofo.R
import com.tezov.gofo.glide.GlideApp
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.application.AppDisplay
import com.tezov.lib_java_android.ui.dialog.DialogNavigable
import com.tezov.lib.adapterJavaToKotlin.Navigate
import com.tezov.lib.adapterJavaToKotlin.wrapperAnonymous.ViewOnclickListenerW
import com.tezov.gofo.room.data.ItemPhoto

class DialogPhotoDownload : DialogNavigable() {
    companion object{
        private const val QUALITY = 80
        private const val FORMAT = "jpg"
        private const val COLOR_SPACE = "tinysrgb"
        private const val FIT = "clip"
    }

    override fun newState(): DialogNavigable.State {
        return State()
    }

    override fun getWidth(): Int {
        return AppDisplay.getSizeOriented().width
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val param: Param = getParam()
        val view: View = inflater.inflate(R.layout.dialog_photo_download,container,false)
        val itemPhoto = param.itemPhoto!!
        val dataPhoto = itemPhoto.getDataPhoto()!!
        dataPhoto.user?.name?.let {
            val lbl = view.findViewById<TextView>(R.id.lbl_author)
            lbl.text = it
        }
        dataPhoto.user?.images?.small?.let {
            val img = view.findViewById<ImageView>(R.id.img_author)
            GlideApp.with(AppContext.get())
                .load(it)
                .transform(RoundedCorners(10))
                .into(img)
        }
        dataPhoto.description?.let {
            val lbl = view.findViewById<TextView>(R.id.lbl_description)
            lbl.text = it
        }?:let {
            view.findViewById<TextView>(R.id.lbl_description).visibility = ViewGroup.GONE
        }
        val width = getWidth()
        val height = if((itemPhoto.width != null) && (itemPhoto.width!! > 0) && (itemPhoto.height != null)){
            val ratio = itemPhoto.height!!.toFloat() / itemPhoto.width!!.toFloat()
            (width * ratio).toInt()
        }
        else{
            null
        }
        height?.let {
            val lblSize = view.findViewById<TextView>(R.id.lbl_size)
            lblSize.text = "${itemPhoto.width}x${itemPhoto.height}"
            val url = itemPhoto.url?.let {
                it + "cs=$COLOR_SPACE&fit=$FIT&fm=$FORMAT&q=$QUALITY&w=${width}&h=${height}"
            }
            val img = view.findViewById<ImageView>(R.id.img_photo)
            img.layoutParams = ViewGroup.LayoutParams(width, height)
            img.setOnClickListener(ViewOnclickListenerW{
                onClicked {
                    close()
                }
            })
            GlideApp.with(AppContext.get())
                .load(url)
                .addListener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        view.findViewById<TextView>(R.id.btn_download).visibility = ViewGroup.VISIBLE
                        return false
                    }
                })
                .into(img)
        }
        dataPhoto.urls?.raw?.let {
            val btn = view.findViewById<TextView>(R.id.btn_download)
            btn.setOnClickListener(ViewOnclickListenerW{
                onClicked {
                    val state = DialogPhotoDownloadOption.State()
                    state.obtainParam().apply {
                        this.setTitle(R.string.lbl_download_photo_title)
                        this.name = dataPhoto.user?.name
                        this.url = it
                        this.downloadLocation = dataPhoto.links?.downloadLocation
                        this.width = dataPhoto.width
                        this.height = dataPhoto.height
                        this.setConfirmButtonText(R.string.btn_confirm)
                        this.setCancelButtonText(R.string.btn_cancel)
                    }
                    Navigate.To(DialogPhotoDownloadOption::class.java, state)
                }
            })
        }
        val profileClickListener = ViewOnclickListenerW{
            onClicked {
                val state = DialogUser.State()
                val param = state.obtainParam()
                param.itemPhoto = itemPhoto
                Navigate.To(DialogUser::class.java, state)

            }
        }
        view.findViewById<View>(R.id.img_author).setOnClickListener(profileClickListener)
        view.findViewById<View>(R.id.lbl_author).setOnClickListener(profileClickListener)
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