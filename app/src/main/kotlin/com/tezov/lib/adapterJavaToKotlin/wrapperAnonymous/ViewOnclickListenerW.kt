package com.tezov.lib.adapterJavaToKotlin.wrapperAnonymous

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import android.view.View
import com.tezov.lib_java_android.wrapperAnonymous.ViewOnClickListenerW

class ViewOnclickListenerW {
    companion object{
        inline operator fun invoke(init: ViewOnclickListenerW.() -> Unit):ViewOnClickListenerW{
            return ViewOnclickListenerW().apply {
                init()
            }.build()
        }
    }
    private lateinit var blockOnClicked: (ViewOnClickListenerW.() -> Unit)
    fun onClicked(block: ViewOnClickListenerW.() -> Unit): ViewOnclickListenerW {
        blockOnClicked = block
        return this
    }
    fun build(): ViewOnClickListenerW {
        if(!this::blockOnClicked.isInitialized){
/*#-debug-> DebugException.start().log("call onClicked is mandatory").end() <-debug-#*/
        }
        return object : ViewOnClickListenerW(){
            override fun onClicked(view: View?) {
                blockOnClicked.invoke(this)
            }
        }
    }
}