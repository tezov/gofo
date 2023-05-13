package com.tezov.gofo.room.data

import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import androidx.annotation.NonNull
import androidx.room.*
import com.tezov.lib_java.buffer.ByteBuffer
import com.tezov.lib_java.buffer.ByteBufferBuilder
import com.tezov.lib_java.cipher.UtilsMessageDigest
import com.tezov.lib_java.debug.DebugString
import com.tezov.lib_java.toolbox.Clock
import com.tezov.lib_java.util.UtilsList.NULL_INDEX
import com.tezov.gofo.misc.ClockFormat
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterFeed
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterRandom
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterSearch
import com.tezov.gofo.room.database.Database
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

@Entity(tableName = "ITEM_FILTER")
class ItemFilter {
    @ProguardFieldKeep
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "ID")
    var id: Long? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "TYPE", collate = ColumnInfo.NOCASE)
    var type: Type? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "PROVIDER", collate = ColumnInfo.NOCASE)
    var provider: Provider? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "DATA_BYTES", typeAffinity = ColumnInfo.BLOB)
    var dataBytes: ByteArray? = null
    @ProguardFieldKeep
    @NonNull
    @ColumnInfo(name = "TIMESTAMP")
    var timestamp: Long? = null

    companion object{
        fun load(id:Long):ItemFilter?{
            Database.lock(this)
            val item = Database.filters.getWithId(id)
            Database.unlock(this)
            return item
        }
    }

    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
    }

    fun clear(){
        id = null
        type = null
        provider = null
        dataBytes = null
    }

    fun <T:Data<T>> setData(data: T) {
        this.type = data.getType()
        this.provider = data.getProvider()
        this.dataBytes = data.toBytes()
    }
    fun <T> getData(): T? {
        if (type != null && provider != null) {
            if(provider == Provider.UNSPLASH){
                return when (type) {
                    Type.RANDOM -> {
                        DataFilterRandom(id).apply { dataBytes?.let { fromBytes(it) } }
                    }
                    Type.SEARCH -> {
                        DataFilterSearch(id).apply { dataBytes?.let { fromBytes(it) } }
                    }
                    Type.FEED -> {
                        DataFilterFeed(id).apply { dataBytes?.let { fromBytes(it) } }
                    }
                    else -> {
/*#-debug-> DebugException.start().unknown("type", type).end() <-debug-#*/
                        null
                    }
                }  as? T
            }
        }
        return null
    }

    fun save():Boolean{
        timestamp = Clock.MilliSecond.now()
        val id = Database.filters.offer(this)
        return if(id != NULL_INDEX.toLong()){
            this.id = id
            true
        } else{
            false
        }
    }

    fun toDebugString(): DebugString {
        val data = DebugString()
        data.append("id",id)
        dataBytes?.let {
            data.append("data", getData<Any?>())
            data.append("timestamp", ClockFormat.longToDateTime_FULL(timestamp))
        }
        ?:let {
            data.append("type",type)
            data.append("provider",provider)
            data.append("data is null")
            data.append("timestamp", ClockFormat.longToDateTime_FULL(timestamp))
        }

        return data
    }
    fun toDebugLog(){
/*#-debug-> DebugLog.start().send(toDebugString()).end() <-debug-#*/
    }

    protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
    }

    enum class Type{
        FEED, RANDOM, SEARCH;
        @ProvidedTypeConverter
        class Converter{
            @TypeConverter
            fun toString(status: Type?):String?{
                return status?.name
            }
            @TypeConverter
            fun fromString(status:String?): Type?{
                return status?.let {valueOf(it)}
            }
        }
    }
    enum class Provider{
        UNSPLASH;
        @ProvidedTypeConverter
        class Converter{
            @TypeConverter
            fun toString(status: Provider?):String?{
                return status?.name
            }
            @TypeConverter
            fun fromString(status:String?): Provider?{
                return status?.let {valueOf(it)}
            }
        }
    }

    abstract class Data<T : Data<T>?>(private var id:Long?) {
        abstract fun getType(): Type
        abstract fun getProvider(): Provider

        fun getId():Long{
            return id!!
        }

        fun toBytes():ByteArray{
            val buffer = ByteBufferBuilder.obtain()
            toByteBuffer(buffer)
            return buffer.array()
        }
        abstract fun toByteBuffer(buffer: ByteBuffer)

        fun fromBytes(bytes:ByteArray): Data<T> {
            fromByteBuffer(ByteBuffer.wrap(bytes))
            return this
        }
        abstract fun fromByteBuffer(buffer: ByteBuffer)

        fun getFingerPrint():ByteArray{
            return UtilsMessageDigest.digest(UtilsMessageDigest.Mode.SHA1, toBytes())
        }

        open fun toDebugString(): DebugString {
            val data = DebugString()
            data.append("type", getType())
//            data.append("provider", getProvider())
            return data
        }

        fun toDebugLog() {
/*#-debug-> DebugLog.start().send(toDebugString()).end(); <-debug-#*/
        }

        @Throws(Throwable::class)
        protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end(); <-debug-#*/
        }
    }

}