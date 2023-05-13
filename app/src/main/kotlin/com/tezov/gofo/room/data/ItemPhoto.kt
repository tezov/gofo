package com.tezov.gofo.room.data

import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import androidx.annotation.NonNull
import androidx.room.*
import com.tezov.lib_java.debug.DebugString
import com.tezov.gofo.retrofit.unsplash.data.DataPhoto
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

@Entity(tableName = "ITEM_PHOTO")
class ItemPhoto {
    @ProguardFieldKeep
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    var id: Long? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "ID_PHOTO")
    var idPhoto: String? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "ID_FILTER")
    var idFilter: Long? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "POSITION")
    var position: Int? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "POSITION_BASE")
    var positionBase: Int? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "WIDTH")
    var width: Int? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "HEIGHT")
    var height: Int? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "BLUR_HASH", collate = ColumnInfo.NOCASE)
    var blurHash: String? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "URL", collate = ColumnInfo.NOCASE)
    var url: String? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "DATA_BYTES", typeAffinity = ColumnInfo.BLOB)
    var dataBytes: ByteArray? = null

    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
    }

    fun clear(){
        id = null
        idPhoto = null
        idFilter = null
        position = null
        positionBase = null
        width = null
        height = null
        blurHash = null
        url = null
        dataBytes = null
    }

    fun setDataPhoto(position:Int, positionBase: Int, data: DataPhoto, idFilter: Long){
        this.idPhoto = data.id
        this.idFilter = idFilter
        this.position = position
        this.positionBase = positionBase
        this.width = data.width
        this.height = data.height
        this.blurHash = data.blur_hash
        this.url = data.urls?.raw
        this.dataBytes = data.toBytes()
    }
    fun getDataPhoto(): DataPhoto?{
        return dataBytes?.run { DataPhoto().fromBytes(this)}
    }

    fun toDebugString(): DebugString {
        val data = DebugString()
        data.append("id",id)
        data.append("idPhoto",idPhoto)
        data.append("idFilter",idFilter)
        data.append("position",position)
        data.append("positionBase",positionBase)
        data.append("width",width)
        data.append("height",height)
        data.append("blurHash",blurHash)
        data.append("url",url)
        data.append("dataBytes.length",this.dataBytes?.size)
        return data
    }
    fun toDebugLog(){
/*#-debug-> DebugLog.start().send(toDebugString()).end() <-debug-#*/
    }

    protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
    }
}