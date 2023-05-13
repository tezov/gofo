package com.tezov.gofo.room.data

import androidx.room.*
import com.tezov.lib_java_android.annotation.ProguardFieldKeep

@Entity
class PartialId {
    @ProguardFieldKeep
    @ColumnInfo(name = "ID")
    var id: Long? = null

    @ProvidedTypeConverter
    class Converter{
        @TypeConverter
        fun toString(partial: PartialId?):String?{
            return partial?.id?.toString()
        }
        @TypeConverter
        fun fromString(s:String?): PartialId?{
            return s?.let { PartialId().apply { id = s.toLong() } }
        }
    }

}