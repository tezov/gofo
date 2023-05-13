package com.tezov.gofo.dialog

import com.tezov.gofo.activity.ActivityMain
import android.graphics.Bitmap
import com.tezov.lib_java_android.ui.dialog.modal.DialogModalRequest
import com.tezov.lib_java_android.ui.form.component.plain.FormEditText
import com.tezov.gofo.R
import android.os.Bundle
import com.tezov.lib_java_android.ui.component.plain.FocusCemetery
import com.tezov.lib_java_android.file.StorageMedia
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.tezov.gofo.application.Environment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.tezov.gofo.BuildConfig
import com.tezov.gofo.glide.GlideApp
import com.tezov.gofo.glide.GlideProgressUrl
import com.tezov.lib_java.data.validator.ValidatorDigit
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib_java.type.runnable.RunnableW
import com.tezov.lib_java.util.UtilsString
import com.tezov.lib_java_android.application.AppContext
import com.tezov.lib_java_android.file.UriW
import com.tezov.lib_java_android.file.UtilsFile
import com.tezov.lib_java_android.toolbox.PostToHandler
import com.tezov.lib_java_android.type.image.imageHolder.ImageJPEG
import com.tezov.lib_java_android.ui.component.plain.EditTextWithIconAction
import com.tezov.lib_java_android.util.UtilsTextWatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyException
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.observer.Observable_Kext.observeE
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.addRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.build
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.setOnDoneRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.setOnStartRun
import com.tezov.lib.adapterJavaToKotlin.toolbox.Nullify.nullify
import com.tezov.lib.adapterJavaToKotlin.wrapperAnonymous.ViewOnclickListenerW
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import com.tezov.gofo.retrofit.unsplash.UnsplashCacheProvider
import com.tezov.lib.adapterJavaToKotlin.async.Handler.post
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.type.primitive.string.StringCharTo
import com.tezov.lib_java.wrapperAnonymous.BiConsumerW
import com.tezov.lib_java_android.ui.view.ProgressBarTransfer
import java.io.IOException
import java.lang.StringBuilder
import java.util.*

class DialogPhotoDownloadOption : DialogModalRequest() {
    companion object{
        private const val QUALITY = 100
        private const val FORMAT = "jpg"
        private const val FIT = "clip"
    }

    private var viewState: ViewState? = null
    private lateinit var frmWidth: FormEditText
    private lateinit var entryWidth: Entry
    private lateinit var frmHeight: FormEditText
    private lateinit var entryHeight: Entry
    private lateinit var btnKeepRatio: Button
    private lateinit var progressBarTransfer:ProgressBarTransfer

    override fun obtainParam(): Param {
        return super.obtainParam() as Param
    }
    override fun getParam(): Param {
        return super.getParam() as Param
    }

    override fun enableScrollbar(): Boolean {
        return true
    }
    override fun getFrameLayoutId(): Int {
        return R.layout.dialog_download_request
    }

    private fun me(): DialogPhotoDownloadOption {
        return this
    }

    override fun onFrameMerged(view: View, savedInstanceState: Bundle?) {
        viewState = ViewState.REQUEST
        frmWidth = view.findViewById(R.id.frm_width)
        frmWidth.setValidator(ValidatorDigit())
        frmWidth.addTextChangedListener(UtilsTextWatcher.IntRange(1, param.width))
        entryWidth = object : Entry(){
            override fun updateSizeRatio() {
                me().updateSizeRatio_withToheight()
            }
            override fun command(action: Any?): Boolean {
                if(action == EditTextWithIconAction.IconAction.ACTION){
                    frmWidth.setText(param.width?.toString())
                }
                return true
            }
        }
        frmWidth.attach(entryWidth)
        frmWidth.setText(param.width?.toString())

        frmHeight = view.findViewById(R.id.frm_height)
        frmHeight.setValidator(ValidatorDigit())
        frmHeight.addTextChangedListener(UtilsTextWatcher.IntRange(1, param.height))
        entryHeight = object : Entry(){
            override fun updateSizeRatio() {
                me().updateSizeRatio_heightToWidth()
            }
            override fun command(action: Any?): Boolean {
                if(action == EditTextWithIconAction.IconAction.ACTION){
                    frmHeight.setText(param.height?.toString())
                }
                return true
            }
        }
        frmHeight.attach(entryHeight)
        frmHeight.setText(param.height?.toString())

        entryWidth.dispatchOnSet = true
        entryHeight.dispatchOnSet = true

        btnKeepRatio = view.findViewById(R.id.btn_keep_ratio)
        btnKeepRatio.isActivated = true
        btnKeepRatio.setOnClickListener(ViewOnclickListenerW{
            onClicked {
                if(btnKeepRatio.isActivated) {
                    btnKeepRatio.setActivated(false)
                } else {
                    btnKeepRatio.setActivated(true)
                    if(frmWidth.value.toInt() > frmHeight.value.toInt()){
                        updateSizeRatio_withToheight()
                    }
                    else{
                        updateSizeRatio_heightToWidth()
                    }
                }
            }
        })
    }

    override fun onConfirm() {
        if (viewState == ViewState.REQUEST) {
            FocusCemetery.request(view)
            if (!frmWidth.isValid || !frmHeight.isValid) {
                if (!frmWidth.isValid) {
                    frmWidth.showError()
                }
                if (!frmHeight.isValid) {
                    frmHeight.showError()
                }
                setButtonsEnable(true)
                return
            }
            val url = param.url!!.let {
                if((frmWidth.value.toInt() == param.width) && (frmHeight.value.toInt() == param.height)){
                    it + "fm=$FORMAT&q=$QUALITY"
                }
                else{
                    it + "&fit=$FIT&fm=$FORMAT&q=$QUALITY&w=${frmWidth.value}&h=${frmHeight.value}"
                }
            }
            val nameBuilder = StringBuilder()
            param.name?.let {
//                val pattern = Regex("[^a-zA-Z0-9.\\-]")
                val pattern = Regex("[\\\\/:*?\"<>|]")
                var name = pattern.replace(it, " ")
                name = name.trim().replace(' ', '_')
                nameBuilder.append(name.lowercase(Locale.getDefault()))
            }
            nameBuilder.append("_").append(frmWidth.value).append("x").append(frmHeight.value)
            UtilsString.appendDateAndTime(nameBuilder)
            downloadAndSave(nameBuilder.toString(), url, param.downloadLocation)
        }
        else if (viewState == ViewState.RESULT_SUCCESS) {
            postConfirm()
            close()
        }
        else if (viewState == ViewState.RESULT_EXCEPTION) {
            postException()
            close()
        }
    }

    private fun updateSizeRatio_withToheight(){
        if(!btnKeepRatio.isActivated){
            return
        }
        val ratio = param.height!!.toFloat() / param.width!!.toFloat()
        entryHeight.dispatchOnSet = false
        var result:Int = (frmWidth.value.toInt() * ratio).toInt()
        if(result < 1){
            result = 1
        }
        if(result > param.height!!){
            result = param.height!!
        }
        frmHeight.value = result.toString()
        entryHeight.dispatchOnSet = true
    }
    private fun updateSizeRatio_heightToWidth(){
        if(!btnKeepRatio.isActivated){
            return
        }
        val ratio = param.width!!.toFloat() / param.height!!.toFloat()
        entryWidth.dispatchOnSet = false
        var result:Int = (frmHeight.value.toInt() * ratio).toInt()
        if(result < 1){
            result = 1
        }
        if(result > param.width!!){
            result = param.width!!
        }
        frmWidth.value = result.toString()
        entryWidth.dispatchOnSet = true
    }

    private fun downloadAndSave(name:String, url:String, downloadLocation:String?) {
        RunnableGroup(this).name("downloadAndSave").build<Unit> {
            val KEY_BITMAP = key()
            val KEY_URI = key()
            setOnStartRun {
                inflateProgress()
            }
            if(!StorageMedia.PERMISSION_CHECK_WRITE()){
                addRun {
                    StorageMedia.PERMISSION_REQUEST_WRITE(true).observeE(this){
                        onComplete {
                            next()
                        }
                        onException { e ->
                            putException(e)
                            done()
                        }
                    }
                }.name("check permission")
            }
            addRun {
                download(url).onComplete(
                    succeed = {
                        put(KEY_BITMAP, it.getCompleted())
                        next()
                    },
                    failed = {
                        putException(it)
                        done()
                    })
            }.name("download")
            addRun {
                val nameData = name + "." + ImageJPEG.FILE_EXTENSION_JPG
                val bitmap: Bitmap = get(KEY_BITMAP)
                put(KEY_BITMAP, null)
                save(nameData, bitmap).onComplete(
                    succeed = {
                        put(KEY_URI, it.getCompleted())
                        next()
                    },
                    failed = {
                        putException(it)
                        done()
                    })
            }.name("save")
            if(!BuildConfig.DEBUG_ONLY) {
                addRun {
                    downloadLocation?.let {
                        val request: Request = Request.Builder().url(it).get().build()
                        UnsplashCacheProvider.getNetworkProvider().client.newCall(request).enqueue(object :
                            Callback {
                            override fun onFailure(call: Call, e: IOException) {
/*#-debug-> DebugException.start().log("report download failed").end() <-debug-#*/
                            }
                            override fun onResponse(call: Call, response: Response) {
                                val bodyResponse = response.body
                                if (bodyResponse != null) {
                                    val string = bodyResponse.string().nullify()
                                    if((string != null) && (!string.startsWith("{\"errors\""))){
                                        return
                                    }
                                }
/*#-debug-> DebugException.start().log("report download failed").end() <-debug-#*/
                            }
                        })
                    }
                    next()
                }.name("report download")
            }
            setOnDoneRun {
                if(exception == null){
                    val activityMain: ActivityMain = activity as ActivityMain
                    com.tezov.lib.adapterJavaToKotlin.async.PostToHandler.of(activityMain){
                        runSafe {
                            inflateResultSuccess(get(KEY_URI))
                        }
                    }
                }
                else{
                    inflateResultFailed(
                        AppContext.getResources().getString(R.string.lbl_exception_download)
                    )
                }
            }
            start()
        }
    }
    private fun download(url:String): Deferred<Bitmap> {
        val deferred = CompletableDeferred<Bitmap>()
        GlideApp.with(AppContext.get())
            .asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .load(object : GlideProgressUrl(url){
                override fun update(bytesRead: Long, contentLength: Long) {
                    val progress = (100 * bytesRead / contentLength)
                    progressBarTransfer.setCurrent(progress)
                }
            })
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    progressBarTransfer.setCurrent(100)
                    deferred.notifyComplete(resource)
                }
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    deferred.notifyException(Throwable("failed to load"))
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    deferred.notifyException(Throwable("canceled"))
                }
            })
        return deferred
    }
    private fun save(name:String, bitmap: Bitmap): Deferred<UriW> {
        val deferred = CompletableDeferred<UriW>()
        val uri = Environment.obtainUniquePendingUri(name)
        uri?.let {
            try {
                val os = uri.outputStream
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()
                uri.pending(false)
                deferred.notifyComplete(uri)
            }
            catch (e:Throwable){
                uri.delete()
                deferred.notifyException(e)
            }
        }
        ?:let {
                deferred.notifyException(Throwable("failed to save"))
            }
        return deferred
    }

    private fun inflateProgress() {
        viewState = ViewState.PROGRESS
        isCancelable = false
        replaceFrameView(R.layout.dialog_download_progress_indeterminate, object : BiConsumerW<View, Void>(){
            override fun accept(view: View?, u: Void?) {
                inflateProgressDone(view!!)
            }
        })
        PostToHandler.of(view, object : RunnableW() {
            override fun runSafe() {
                setButtonsVisibility(View.GONE)
            }
        })
    }
    private fun inflateProgressDone(view: View){
        progressBarTransfer = ProgressBarTransfer(view.findViewById(R.id.container_bar_progress), R.layout.mrg_progress_bar)
        progressBarTransfer.setSeparator(AppContext.getResources().getString(R.string.transfer_sep))
        progressBarTransfer.setMax(AppContext.getResources().getString(R.string.transfer_max).toLong())
        progressBarTransfer.setUnit(AppContext.getResources().getString(R.string.transfer_unit))
        progressBarTransfer.setCurrent(0)
    }
    private fun inflateResultFailed(message: String) {
        viewState = ViewState.RESULT_EXCEPTION
        isCancelable = true
        replaceFrameView(
            R.layout.dialog_download_result,
            { view: View, message: String -> onInflatedResultFailed(view, message) },
            message
        )
    }
    private fun onInflatedResultFailed(view: View, message: String) {
        val imgIcon = view.findViewById<FrameLayout>(R.id.img_icon)
        val drawable = AppContext.getResources().getDrawable(R.drawable.ic_error_24dp)
        drawable.setTint(AppContext.getResources().getColorARGB(R.color.DarkRed))
        imgIcon.background = drawable
        val lblResult = view.findViewById<TextView>(R.id.lbl_message)
        lblResult.text = message
        PostToHandler.of(getView(), object : RunnableW() {
            override fun runSafe() {
                btnConfirm.isEnabled = true
                btnConfirm.visibility = View.VISIBLE
            }
        })
    }
    private fun inflateResultSuccess(uri: UriW) {
        viewState = ViewState.RESULT_SUCCESS
        isCancelable = true
        replaceFrameView(
            R.layout.dialog_download_result_success,
            { view: View, uri: UriW -> onInflatedResultSuccess(view, uri) },
            uri
        )
    }
    private fun onInflatedResultSuccess(view: View, uri: UriW) {
        val lblFileName = view.findViewById<TextView>(R.id.lbl_file_name)
        lblFileName.text = uri.fullName
        val lblFolder = view.findViewById<TextView>(R.id.lbl_folder)
        val p = UtilsFile.splitToPathAndFileName(uri.displayPath)
        if (p != null) {
            lblFolder.text = p.first
        }
        btnConfirm.isEnabled = true
        btnConfirm.visibility = View.VISIBLE
    }

    private enum class ViewState {
        REQUEST, PROGRESS, RESULT_SUCCESS, RESULT_EXCEPTION
    }

    class State : DialogModalRequest.State() {
        override fun newParam(): Param {
            return Param()
        }
        override fun obtainParam(): Param {
            return super.obtainParam() as Param
        }
    }
    class Param : DialogModalRequest.Param(){
        var name:String? = null
        var url:String? = null
        var downloadLocation:String? = null
        var width:Int? = null
        var height:Int? = null

    }

    abstract class Entry : FormEditText.EntryString() {
        var dispatchOnSet:Boolean = false
        override fun <T : Any?> onSetValue(type: Class<T>?) {
            if(dispatchOnSet){
                updateSizeRatio()
            }
        }
        abstract fun updateSizeRatio()
    }
}