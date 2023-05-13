package com.tezov.gofo.recycler.photo

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import android.widget.ImageView
import com.tezov.gofo.application.AppConfig
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.tezov.gofo.R
import com.tezov.lib_java.application.AppRandomNumber
import com.tezov.lib_java.application.AppUUIDGenerator
import com.tezov.lib_java.generator.uid.UUID
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.ui.recycler.RecyclerListRowBinder
import com.tezov.lib_java_android.ui.recycler.RecyclerListRowHolder
import com.tezov.lib_java_android.ui.recycler.RecyclerListRowManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.gofo.application.AppHandler
import com.tezov.gofo.glide.GlideApp
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.async.PostToHandler
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.addRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.build
import com.tezov.gofo.misc.BlurHashDecoder
import com.tezov.gofo.room.data.ItemPhoto

class PhotoRowBinder(rowManager: RecyclerListRowManager<CompletableDeferred<ItemPhoto?>>) :
    RecyclerListRowBinder<PhotoRowBinder.RowHolder, CompletableDeferred<ItemPhoto?>>(rowManager) {

    override fun getViewType(): ViewType.Is {
        return ViewType.DEFAULT
    }
    override fun create(parent: ViewGroup): RowHolder {
        return addOnClick(RowHolder(R.layout.recycler_item_view, parent))
    }

    enum class ImageState{
        IDLE, LOADING, BLUR, IMAGE
    }

    class RowHolder(layoutID:Int, parent:ViewGroup): RecyclerListRowHolder<CompletableDeferred<ItemPhoto?>>(layoutID, parent) {
        companion object{
            const val QUALITY = 80
            const val FORMAT = "jpg"
            const val COLOR_SPACE = "tinysrgb"
            const val FIT = "clip"
        }
        var data: CompletableDeferred<ItemPhoto?>? = null
        var state: ImageState = ImageState.IDLE
        lateinit var uid:UUID
        var imageViewBlur:ImageView
        var imageViewPhoto:ImageView
        var target:CustomTarget<Drawable>?  = null

        init {
            imageViewBlur = itemView.findViewById(R.id.img_blur)
            imageViewPhoto = itemView.findViewById(R.id.img_photo)
        }
        fun me() : RowHolder {
            return this
        }

        override fun set(data: CompletableDeferred<ItemPhoto?>) {
            initState()
            this.data = data
            val uidMem = uid
            updateItemView()
            data.onComplete {
                it.getCompleted()
                    ?.let{
                        loadImage(uidMem, it)
                    }
                    ?: let {
//                        DebugException.start().log("ItemPhoto is null").end()
                    }
            }
        }
        override fun get(): CompletableDeferred<ItemPhoto?> {
            synchronized(me()){
                return data!!
            }
        }

        private fun showBlur(){
            imageViewBlur.visibility = ViewGroup.VISIBLE
            imageViewPhoto.visibility = ViewGroup.INVISIBLE
        }
        private fun showPhoto(){
            imageViewPhoto.visibility = ViewGroup.VISIBLE
            imageViewBlur.visibility = ViewGroup.INVISIBLE
        }

        private fun initState(){
            synchronized(me()){
                target?.request?.clear()
                state = ImageState.LOADING
                uid = AppUUIDGenerator.next()
                showBlur()
            }
        }
        private fun closeState(){
            synchronized(me()){
                target = null
                state = ImageState.IDLE
            }
        }
        private fun compareState(imageUid:UUID):Boolean{
            return synchronized(me()){
                imageUid == this.uid
            }
        }
        private fun compareState(imageUid:UUID, imageStateExpected: ImageState):Boolean{
            return synchronized(me()){
                imageUid == this.uid && imageStateExpected == this.state
            }
        }
        private fun compareAndSetState(imageUid:UUID, imageStateExpected: ImageState, imageStateToSet: ImageState):Boolean{
            return synchronized(me()){
                if(imageUid != this.uid){
                    return@synchronized false
                }
                if(imageStateExpected == this.state){
                    this.state = imageStateToSet
                    return@synchronized true
                }
                false
            }
        }
        private fun compareAndSetState(imageUid:UUID, imageStateExpected:List<ImageState>, imageStateToSet: ImageState):Boolean{
            return synchronized(me()){
                if(imageUid != this.uid){
                    return@synchronized false
                }
                if(imageStateExpected.contains(this.state)){
                    this.state = imageStateToSet
                    return@synchronized true
                }
                false
            }
        }

        private fun updateItemView(){
            val layoutParams:ViewGroup.LayoutParams = itemView.layoutParams
            layoutParams.width = AppConfig.ITEM_IMAGE_WIDTH
            layoutParams.height = AppRandomNumber.nextInt(AppConfig.ITEM_IMAGE_HEIGHT_MIN, AppConfig.ITEM_IMAGE_HEIGHT_MAX)
            itemView.layoutParams = layoutParams
            val alpha = 0x55 + AppRandomNumber.nextInt(0xAA)
            val red = 0x11 + AppRandomNumber.nextInt(0xEE)
            val green = 0x11 + AppRandomNumber.nextInt(0xEE)
            val bleu = 0x11 + AppRandomNumber.nextInt(0xEE)
            val color:Int = (alpha shl 24) or (red shl 16) or (green shl 8) or bleu
            itemView.setBackgroundColor(color)
        }
        private fun loadImage(imageUid:UUID, itemPhoto: ItemPhoto){
            if(!compareState(imageUid)){
                return
            }
            val imageUidMem = this.uid
            val width = AppConfig.NETWORK_IMAGE_WIDTH
            val height = if((itemPhoto.width != null) && (itemPhoto.width!! > 0) && (itemPhoto.height != null)){
                val ratio = itemPhoto.height!!.toFloat() / itemPhoto.width!!.toFloat()
                (width * ratio).toInt()
            }
            else{
                AppConfig.ITEM_IMAGE_HEIGHT_MAX
            }
            val url = itemPhoto.url?.let {
                it + "cs=$COLOR_SPACE&fit=$FIT&fm=$FORMAT&q=$QUALITY&w=${width}&h=${height}"
            }
            url?.let { url ->
                RunnableGroup(this, AppHandler.getLoaderImageHandler()).name("loadImage").build<Unit> {
                    addRun {
                        if(!compareState(imageUid)){
                            done()
                            return@addRun
                        }
                        itemPhoto.blurHash?.let { _ ->
                            isImageCached(url).onComplete {
                                if(!it.getCompleted() || (imageViewBlur.drawable == null)){
                                    next()
                                }
                                else{
                                    skipNext()
                                }
                            }
                        }?:skipNext()
                    }.name("check if need blur")
                    addRun {
                        if(!compareState(imageUid)){
                            done()
                            return@addRun
                        }
                        itemPhoto.blurHash?.let { blurHash ->
                            loadBlur(imageUidMem, blurHash, width, height)
                        }
                        next()
                    }.name("load blur")
                    addRun {
                        if(!compareState(imageUid)){
                            done()
                            return@addRun
                        }
                        loadImage(imageUidMem, url).onComplete {
                            next()
                        }
                    }.name("load image")
                    start()
                }
            }
        }

        private fun isImageCached(url:String):Deferred<Boolean>{
            val deferred:CompletableDeferred<Boolean> = CompletableDeferred()
            GlideApp.with(AppContext.get())
                .load(url)
                .onlyRetrieveFromCache(true)
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        deferred.notifyComplete(AppHandler.getLoaderImageHandler(), true)
                    }
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        deferred.notifyComplete(AppHandler.getLoaderImageHandler(), false)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        deferred.notifyComplete(AppHandler.getLoaderImageHandler(), false)
                    }
                })
            return deferred
        }
        private fun loadBlur(imageUid:UUID, blurHash:String, width:Int, height:Int):Deferred<Unit>{
            val deferred:CompletableDeferred<Unit> = CompletableDeferred()
            BlurHashDecoder.decode(blurHash, width, height)?.let {
                PostToHandler.of(itemView){
                       runSafe {
                           if(compareAndSetState(imageUid, ImageState.LOADING, ImageState.BLUR)){
                               imageViewBlur.setImageBitmap(it)
                           }
                           deferred.notifyComplete(AppHandler.getLoaderImageHandler())
                       }
                }
            }?:deferred.notifyComplete()
            return deferred
        }
        private fun loadImage(imageUid:UUID, url:String):Deferred<Unit>{
            val deferred:CompletableDeferred<Unit> = CompletableDeferred()
            synchronized(me()){
                target = GlideApp.with(AppContext.get())
                    .load(url)
                    .into(object : CustomTarget<Drawable>() {
                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            PostToHandler.of(itemView){
                                runSafe {
                                    if(compareAndSetState(imageUid, arrayListOf(
                                            ImageState.LOADING,
                                            ImageState.BLUR
                                        ), ImageState.IMAGE
                                        )){
                                        imageViewPhoto.background = resource
                                        showPhoto()
                                        closeState()
                                    }
                                    deferred.notifyComplete(AppHandler.getLoaderImageHandler())
                                }
                            }
                        }
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            closeState()
                            deferred.notifyComplete(AppHandler.getLoaderImageHandler())
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            closeState()
                            deferred.notifyComplete(AppHandler.getLoaderImageHandler())
                        }
                    })
            }
            return deferred
        }
    }

}