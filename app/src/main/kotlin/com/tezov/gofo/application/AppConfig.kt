package com.tezov.gofo.application

import com.tezov.lib_java_android.application.AppDisplay
import java.util.concurrent.TimeUnit
import application.AppConfig_bt

object AppConfig : AppConfig_bt() {

    val AD_SUGGEST_PAID_VERSION_MODULO = getInt(AppConfigKey.AD_SUGGEST_PAID_VERSION_MODULO.id)

    const val RECYCLER_SPAN:Int = 3
    val ITEM_IMAGE_WIDTH = (AppDisplay.getSizeOriented().width / RECYCLER_SPAN)
    val NETWORK_IMAGE_WIDTH = (ITEM_IMAGE_WIDTH * 1.25).toInt()
    val ITEM_IMAGE_HEIGHT_MIN:Int = (ITEM_IMAGE_WIDTH * 0.75).toInt()
    val ITEM_IMAGE_HEIGHT_MAX = (ITEM_IMAGE_WIDTH * 1.25).toInt()
    val CACHE_IMAGE_SIZE_o = NETWORK_IMAGE_WIDTH.toLong() * ITEM_IMAGE_HEIGHT_MAX.toLong()
    const val CACHE_IMAGE_COUNT:Long = 50L
    const val ITERATOR_PHOTO_TRIGGER:Int = 15
    const val ITERATOR_PHOTO_BUFFER_SIZE:Int = 40
}