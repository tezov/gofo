package com.tezov.gofo.room.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tezov.lib_java.async.LockThread
import com.tezov.lib_java_android.application.AppContext
import com.tezov.gofo.room.data.ItemFilter
import com.tezov.gofo.room.data.ItemPhoto
import com.tezov.gofo.room.data.PartialId

@androidx.room.Database(
    entities = [ItemPhoto::class, ItemFilter::class], exportSchema = false, version = 2
)
@TypeConverters(value = [ItemFilter.Type.Converter::class, ItemFilter.Provider.Converter::class, PartialId.Converter::class])
abstract class Database : RoomDatabase() {
    companion object {
        const val DB_NAME = "room"
        private val instance:Database = Room.databaseBuilder(
            AppContext.get(),
            Database::class.java, DB_NAME
        )
        .allowMainThreadQueries()
        .addTypeConverter(ItemFilter.Type.Converter())
        .addTypeConverter(ItemFilter.Provider.Converter())
        .addTypeConverter(PartialId.Converter())
        .fallbackToDestructiveMigration().build()

        private val lockThread: LockThread<Companion> = LockThread(this)
        fun lock(owner:Any){
            lockThread.lock(owner)
        }
        fun unlock(owner:Any){
            lockThread.unlock(owner)
        }
        val filters:DaoItemFilter = instance.newDaoItemFilter()
        val photos:DaoItemPhoto = instance.newDaoItemPhoto()
    }
    abstract fun newDaoItemFilter(): DaoItemFilter
    abstract fun newDaoItemPhoto(): DaoItemPhoto


}