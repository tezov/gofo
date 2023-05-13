package com.tezov.gofo.room.database

import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import androidx.room.*
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.util.UtilsList.NULL_INDEX
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.gofo.room.data.ItemFilter

@Dao
interface DaoItemFilter {

    fun toDebugLog(type: ItemFilter.Type? = null){
/*#-debug-> DebugLog.start().send("type: ${type}").end() <-debug-#*/
/*#-debug-> DebugLog.start().send("count: " + count(type)).end() <-debug-#*/
        select(type).takeIf{it.isNotEmpty()}?.forEach {
            it.toDebugLog()
        }?:DebugLog.start().send("database filter is empty").end()
    }

    @Query("SELECT COUNT(id) FROM ITEM_FILTER WHERE (:type IS NULL OR (:type IS NOT NULL AND TYPE = :type)) AND (:provider IS NULL OR (:provider IS NOT NULL AND PROVIDER = :provider))")
    fun count(type: ItemFilter.Type? = null, provider: ItemFilter.Provider? = null): Int

    @Query("SELECT * FROM ITEM_FILTER WHERE (:type IS NULL OR (:type IS NOT NULL AND TYPE = :type)) AND (:provider IS NULL OR (:provider IS NOT NULL AND PROVIDER = :provider)) ORDER BY TIMESTAMP DESC")
    fun select(type: ItemFilter.Type? = null, provider: ItemFilter.Provider? = null): List<ItemFilter>

    @Query("SELECT * FROM ITEM_FILTER WHERE (:type IS NULL OR (:type IS NOT NULL AND TYPE = :type)) AND (:provider IS NULL OR (:provider IS NOT NULL AND PROVIDER = :provider)) ORDER BY TIMESTAMP DESC LIMIT :limit OFFSET :offset")
    fun select(type: ItemFilter.Type? = null, provider: ItemFilter.Provider? = null, offset:Int, limit:Int): List<ItemFilter>

    @Query("SELECT * FROM ITEM_FILTER WHERE ID=:id")
    fun getWithId(id: Long): ItemFilter?

    @Query("SELECT * FROM ITEM_FILTER WHERE (TYPE = :type) AND (PROVIDER = :provider) AND (DATA_BYTES = :data)")
    fun getWithData(type: ItemFilter.Type, provider: ItemFilter.Provider, data:ByteArray): ItemFilter?

    @Query("SELECT * FROM ITEM_FILTER WHERE (TYPE = :type) AND (PROVIDER = :provider) ORDER BY TIMESTAMP DESC LIMIT 1")
    fun getLatest(type: ItemFilter.Type, provider: ItemFilter.Provider): ItemFilter?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: ItemFilter): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(item: ItemFilter): Int

    @Transaction
    fun offer(item:ItemFilter): Long{
        val itemInDb = getWithData(item.type!!, item.provider!!, item.dataBytes!!)
        return if(itemInDb == null){
            insert(item)
        }
        else{
            val updated = update(item) == 1
            if(!updated){
/*#-debug-> DebugException.start().log("failed to update").end() <-debug-#*/
            }
            itemInDb.id?:NULL_INDEX.toLong()
        }
    }

    @Delete
    fun delete(item: ItemFilter)
    @Query("DELETE FROM ITEM_FILTER WHERE ID=:id")
    fun deleteWithId(id: Long)
    @Query("DELETE FROM ITEM_FILTER")
    fun clear()

    fun clearAsync(): Deferred<Unit> {
        val deferred: CompletableDeferred<Unit> = CompletableDeferred()
        val handler = Handler.fromMyLooper()
        Handler.SECONDARY().postRun(this){
            clear()
            deferred.notifyComplete(handler)
        }
        return deferred
    }
}