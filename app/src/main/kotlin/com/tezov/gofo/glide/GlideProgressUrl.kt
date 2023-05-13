package com.tezov.gofo.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.type.primitive.string.StringCharTo
import java.io.InputStream

abstract class GlideProgressUrl (
    val url: String
){
    abstract fun update(bytesRead: Long, contentLength: Long)

}