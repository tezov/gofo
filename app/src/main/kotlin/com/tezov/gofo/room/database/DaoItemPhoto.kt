package com.tezov.gofo.room.database

import androidx.room.*
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.util.UtilsList.NULL_INDEX
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.gofo.room.data.ItemPhoto
import com.tezov.gofo.room.data.PartialId

@Dao
interface DaoItemPhoto {
    fun toDebugLog(idFilter: Long? = null){
/*#-debug-> DebugLog.start().send("idFilter: ${idFilter}").end() <-debug-#*/
/*#-debug-> DebugLog.start().send("count: " + count(idFilter)).end() <-debug-#*/
        idFilter?.let { DebugLog.start().send("maxPositionBase:" + getMaxPositionBase(idFilter)).end() }
        idFilter?.let{ select(idFilter) }?.takeIf{it.isNotEmpty()}?.forEach {
/*#-debug-> DebugLog.start().send("" + it.idFilter + " # " + it.idPhoto + " # " + it.position + "/" + it.positionBase).end()// + " -> " + it.url).end() <-debug-#*/
        }?:DebugLog.start().send("database photo is empty").end()
    }

    @Query("SELECT COUNT(id) FROM ITEM_PHOTO WHERE (:idFilter IS NULL OR ID_FILTER = :idFilter)")
    fun count(idFilter: Long? = null): Int
    @Query("SELECT COUNT(id) FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) AND ((POSITION + (:positionBase - POSITION_BASE)) > :position)")
    fun countAfterPosition(idFilter: Long, position:Int, positionBase:Int): Int
    @Transaction
    fun countAfterPosition(idFilter: Long, position:Int): Int?{
        getMaxPositionBase(idFilter)?.let{
            return countAfterPosition(idFilter, position, it)
        } ?: return null
    }
    @Query("SELECT COUNT(id) FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) AND ((POSITION + (:positionBase - POSITION_BASE)) < :position)")
    fun countBeforePosition(idFilter: Long, position:Int, positionBase:Int): Int
    @Transaction
    fun countBeforePosition(idFilter: Long, position:Int): Int?{
        getMaxPositionBase(idFilter)?.let{
            return countBeforePosition(idFilter, position, it)
        } ?: return null
    }

    @Query("SELECT POSITION FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) ORDER BY POSITION ASC LIMIT 1")
    fun getMinPosition(idFilter: Long): Int?
    @Query("SELECT POSITION FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) ORDER BY POSITION DESC LIMIT 1")
    fun getMaxPosition(idFilter: Long): Int?

    @Query("SELECT POSITION_BASE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) ORDER BY POSITION_BASE ASC LIMIT 1")
    fun getMinPositionBase(idFilter: Long): Int?
    @Query("SELECT POSITION_BASE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) ORDER BY POSITION_BASE DESC LIMIT 1")
    fun getMaxPositionBase(idFilter: Long): Int?

    @Query("SELECT (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE ASC LIMIT 1")
    fun getMinPositionAbsolute(idFilter: Long, positionBase:Int): Int?
    @Transaction
    fun getMinPositionAbsolute(idFilter: Long): Int?{
        getMaxPositionBase(idFilter)?.let{
            return getMinPositionAbsolute(idFilter, it)
        } ?: return null
    }
    @Query("SELECT (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE DESC LIMIT 1")
    fun getMaxPositionAbsolute(idFilter: Long, positionBase:Int): Int?
    @Transaction
    fun getMaxPositionAbsolute(idFilter: Long): Int?{
        getMaxPositionBase(idFilter)?.let{
            return getMaxPositionAbsolute(idFilter, it)
        } ?: return null
    }

    @Query("SELECT (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) AND (POSITION_ABSOLUTE > :position) ORDER BY POSITION_ABSOLUTE ASC LIMIT 1 OFFSET :offset")
    fun getPositionAbsoluteAfterOffset(idFilter: Long, position:Int, positionBase:Int, offset:Int): Int?
    @Transaction
    fun getPositionAbsoluteAfterOffset(idFilter: Long, position:Int, offset:Int): Int?{
        getMaxPositionBase(idFilter)?.let{
            return getPositionAbsoluteAfterOffset(idFilter, position, it, offset)
        } ?: return null
    }

    @Query("SELECT (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) AND (POSITION_ABSOLUTE < :position) ORDER BY POSITION_ABSOLUTE DESC LIMIT 1 OFFSET :offset")
    fun getPositionAbsoluteBeforeOffset(idFilter: Long, position:Int, positionBase:Int, offset:Int): Int?
    @Transaction
    fun getPositionAbsoluteBeforeOffset(idFilter: Long, position:Int, offset:Int): Int?{
        getMaxPositionBase(idFilter)?.let{
            return getPositionAbsoluteBeforeOffset(idFilter, position, it, offset)
        } ?: return null
    }

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT EXISTS(SELECT (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (POSITION_ABSOLUTE = :position) AND (ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE ASC)")
    fun isPositionBusy(idFilter: Long, position:Int, positionBase:Int): Boolean
    @Transaction
    fun isPositionBusy(idFilter: Long, position:Int): Boolean{
        getMaxPositionBase(idFilter)?.let{
            return isPositionBusy(idFilter, position, it)
        } ?: return false
    }

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT *,(POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (ID_FILTER = :idFilter) AND (POSITION_ABSOLUTE >= :offset AND POSITION_ABSOLUTE < (:offset + :limit)) ORDER BY POSITION_ABSOLUTE ASC")
    fun select(idFilter: Long, offset:Int, limit:Int, positionBase:Int): List<ItemPhoto>
    @Transaction
    fun select(idFilter: Long, offset:Int, limit:Int): List<ItemPhoto>?{
        getMaxPositionBase(idFilter)?.let{
            return select(idFilter, offset, limit, it)
        } ?: return null
    }

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT *,(POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (:idFilter IS NULL OR ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE ASC")
    fun select(idFilter: Long? = null, positionBase:Int? = 0): List<ItemPhoto>
    @Transaction
    fun select(idFilter: Long): List<ItemPhoto>?{
        getMaxPositionBase(idFilter)?.let{
            return select(idFilter, it)
        } ?: return null
    }

    @Query("SELECT * FROM ITEM_PHOTO WHERE ID=:id")
    fun getWithId(id: Long): ItemPhoto?
    @Query("SELECT * FROM ITEM_PHOTO WHERE ID_PHOTO=:id")
    fun getWithIdPhoto(id: Long): ItemPhoto?
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT *, (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (POSITION_ABSOLUTE = :position) AND (ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE ASC LIMIT 1")
    fun getWithPosition(idFilter: Long, position:Int, positionBase:Int): ItemPhoto?
    @Transaction
    fun getWithPosition(idFilter: Long, position:Int): ItemPhoto?{
        getMaxPositionBase(idFilter)?.let{
            return getWithPosition(idFilter, position, it)
        } ?: return null
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(item: ItemPhoto): Long
    @Transaction
    fun insertIfPositionNotBusy(item:ItemPhoto): Long{
        return insertIfPositionNotBusy(item, item.positionBase!!)
    }
    @Transaction
    fun insertIfPositionNotBusy(item:ItemPhoto, positionBase:Int): Long{
        val positionBusy = isPositionBusy(item.idFilter!!, item.position!!, positionBase)
        return if(!positionBusy){
            insert(item)
        } else{
            NULL_INDEX.toLong()
        }
    }

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(item: ItemPhoto):Int

    @Delete
    fun delete(item: ItemPhoto)
    @Query("DELETE FROM ITEM_PHOTO WHERE ID=:id")
    fun deleteWithId(id: Long)
    @Query("DELETE FROM ITEM_PHOTO WHERE ID_FILTER=:idFilter")
    fun deleteAllWithFilterId(idFilter: Long)
    @Query("DELETE FROM ITEM_PHOTO")
    fun clear()

    @Query("DELETE FROM ITEM_PHOTO WHERE ID IN (:ids)")
    fun delete(ids:List<PartialId>)
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT ID, (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (POSITION_ABSOLUTE > :position) AND (ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE ASC")
    fun selectAllAfterPosition(idFilter: Long, position:Int, positionBase:Int):List<PartialId>
    @Transaction
    fun deleteAllAfterPosition(idFilter: Long, position:Int, positionBase:Int){
        selectAllAfterPosition(idFilter, position, positionBase).takeIf { it.isNotEmpty() }?.let {
            delete(it)
        }
    }
    @Transaction
    fun deleteAllAfterPosition(idFilter: Long, position:Int){
        getMaxPositionBase(idFilter)?.let{ maxPosition ->
            deleteAllAfterPosition(idFilter, position, maxPosition)
        }
    }

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT ID, (POSITION + (:positionBase - POSITION_BASE)) AS POSITION_ABSOLUTE FROM ITEM_PHOTO WHERE (POSITION_ABSOLUTE < :position) AND (ID_FILTER = :idFilter) ORDER BY POSITION_ABSOLUTE ASC")
    fun selectAllBeforePosition(idFilter: Long, position:Int, positionBase:Int):List<PartialId>
    @Transaction
    fun deleteAllBeforePosition(idFilter: Long, position:Int, positionBase:Int){
        selectAllBeforePosition(idFilter, position, positionBase).takeIf { it.isNotEmpty() }?.let {
            delete(it)
        }
    }
    @Transaction
    fun deleteAllBeforePosition(idFilter: Long, position:Int){
        getMaxPositionBase(idFilter)?.let{ maxPosition ->
            deleteAllBeforePosition(idFilter, position, maxPosition)
        }
    }

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