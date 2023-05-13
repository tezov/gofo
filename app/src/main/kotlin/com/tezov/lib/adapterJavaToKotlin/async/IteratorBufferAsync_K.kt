package com.tezov.lib.adapterJavaToKotlin.async

import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib_java.util.UtilsList.NULL_INDEX
import com.tezov.lib_java.wrapperAnonymous.PredicateW
import kotlinx.coroutines.*
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyException
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.async.Handler.postRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.addRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.setOnDoneRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.build
import okhttp3.internal.notify
import okhttp3.internal.wait
import java.util.*

class IteratorBufferAsync_K<T : Any> constructor(
    private var handlerUpdate: Handler,
    private var dataProvider: defProviderAsync_K<T>?,
) : MutableListIterator<T> {

    private var setCurrentIndexJob: Deferred<Int?>? = null
    private var updateTailJob: Deferred<Unit>? = null
    private var updateHeadJob: Deferred<Unit>? = null

    private var dataList: MutableList<T>? = null
    private var lastIndex: Int? = null
    private var firstIndex: Int? = null
    private var currentIndex: Int? = null

    private var triggerSizeBeforeDownload = 5
    private var sizeToDownload = 20
    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
    }

    private fun me(): IteratorBufferAsync_K<T> {
        return this
    }

    private enum class LockState{
        NONE, LOCAL, LOCAL_FROM_GLOBAL, GLOBAL
    }

    private var lock = LockState.NONE

    private fun lockAll(){
//        DebugLog.start().send("-------"+ Thread.getName() + ":: request lock:All from " + lock).end()
        synchronized(me()){
            while(lock != LockState.NONE){
                me().wait()
            }
            lock = LockState.LOCAL_FROM_GLOBAL
//            DebugLog.start().send("-------"+Thread.getName() + ":: " + "locked:" + lock).end()
        }
    }
    private fun lock(state: LockState){
//        DebugLog.start().send("-------"+ Thread.getName() + ":: request lock:" + state + " from " + lock).end()
        if((state == LockState.LOCAL_FROM_GLOBAL)||(state == LockState.NONE)){
/*#-debug-> DebugException.start().log("forbidden request state " + state).end() <-debug-#*/
        }
        synchronized(me()){
            if((state == LockState.LOCAL)&&(lock == LockState.GLOBAL)){
                lock = LockState.LOCAL_FROM_GLOBAL
//                DebugLog.start().send("-------"+Thread.getName() + ":: " + "locked:" + lock).end()
                return@synchronized
            }
            while(lock != LockState.NONE){
                me().wait()
            }
            lock = state
//            DebugLog.start().send("-------"+Thread.getName() + ":: " + "locked:" + lock).end()
        }
    }
    private fun unlock(){
//        DebugLog.start().send("-------"+Thread.getName() + ":: " + "request unlock from " + lock).end()
        synchronized(me()){
            if(lock == LockState.LOCAL_FROM_GLOBAL){
                lock = LockState.GLOBAL
//                DebugLog.start().send("-------"+Thread.getName() + ":: " + "unlocked:" + lock).end()
                return@synchronized
            }
            if(lock != LockState.NONE){
                lock = LockState.NONE
//                DebugLog.start().send("-------"+Thread.getName() + ":: " + "unlocked:" + lock).end()
                me().notify()
            }
            else{
/*#-debug-> DebugException.start().log("was not locked").end() <-debug-#*/
            }
        }
    }
    private fun unlockAll(){
//        DebugLog.start().send("-------"+Thread.getName() + ":: " + "request unlock All from " + lock).end()
        synchronized(me()){
            if(lock == LockState.LOCAL_FROM_GLOBAL){
                lock = LockState.NONE
//                DebugLog.start().send("-------"+Thread.getName() + ":: " + "unlocked:" + lock).end()
                me().notify()
            }
            else{
/*#-debug-> DebugException.start().log("was not All locked").end() <-debug-#*/
            }
        }
    }

    fun setTriggerSizeBeforeDownload(size: Int) {
        triggerSizeBeforeDownload = size
    }
    fun setSizeToDownload(size: Int) {
        sizeToDownload = size
    }
    fun setDataProvider(dataProvider: defProviderAsync_K<T>){
        this.dataProvider = dataProvider
    }
    fun getDataProvider(): defProviderAsync_K<T> {
        return dataProvider!!
    }

    private fun setNextIndex(index: Int?, waitCompletion: Boolean): Deferred<Int?> {
        synchronized(me()){setCurrentIndexJob}?.let { deferred ->
            if (waitCompletion) {
                runBlocking(Dispatchers.Unconfined)  {
                    deferred.join()
                }
            }
            return deferred
        }
        ?: let {
            val deferred:CompletableDeferred<Int?> = CompletableDeferred()
            handlerUpdate.postRun(this){
                //NOW should cancel all progressing update head and tail
                if (index == null) {
                    dataProvider!!.getFirstIndex().onComplete {
                        val completed = it.getCompleted()
                        if (completed != null) {
                            lock(LockState.LOCAL)
                            firstIndex = completed
                            lastIndex = firstIndex!! - 1
                            currentIndex = lastIndex
                            dataList = newDataList()
                            unlock()
                            updateHeadAndTail().onComplete {
                                deferred.notifyComplete(index)
                            }
                        }
                        else {
                            lock(LockState.LOCAL)
                            firstIndex = null
                            lastIndex = null
                            currentIndex = null
                            dataList = null
                            unlock()
                            deferred.notifyComplete(index)
                        }
                    }
                }
                else {
                    dataProvider!!.size().onComplete {
                        val size = it.getCompleted()
                        if (index >= size) {
                            deferred.notifyException(IndexOutOfBoundsException("index:" + index + " size" + ":" + size))
                            return@onComplete
                        }
                        lock(LockState.LOCAL)
                        firstIndex = index
                        lastIndex = firstIndex!! - 1
                        currentIndex = lastIndex
                        dataList = newDataList()
                        unlock()
                        updateHeadAndTail().onComplete {
                            deferred.notifyComplete(index)
                        }
                    }
                }
            }
            with(deferred) {
                setCurrentIndexJob = deferred
                onComplete {
                    setCurrentIndexJob = null
                }
            }
            if(waitCompletion) {
                runBlocking(Dispatchers.Unconfined) {
                    deferred.join()
                }
            }
            return deferred
        }
    }
    fun setCurrentIndex(index: Int) {
        lockAll()
        val direction = currentIndex?.let { index - it }
        if(isLoadedNoLock(index)) {
            currentIndex = index
            unlockAll()
            direction?.let {
                if(it > 0){
                    updateTail(false)
                }
                else{
                    updateHead(false)
                }
            }
            return
        }
        unlock()
        direction?.takeIf{Math.abs(direction) < triggerSizeBeforeDownload}?.let {
            if(it>0){
                updateTail(true)
            }
            else{
                updateHead(true)
            }
        }
        lock(LockState.LOCAL)
        if(isLoadedNoLock(index)){
            currentIndex = index
            unlockAll()
            return
        }
        unlock()
        runBlocking(Dispatchers.Unconfined) {
            while(setNextIndex(index, false).await() != index){}
            lock(LockState.LOCAL)
            currentIndex = index
            unlockAll()
        }
    }
    fun initIndex():Deferred<Unit> {
        return RunnableGroup(this, handlerUpdate).build { deferred ->
            addRun {
                setNextIndex(null, false).onComplete {
                    if(it.getCompleted() == null){
                        next()
                    }
                    else{
                        repeat()
                    }
                }
            }
            setOnDoneRun {
                deferred.notifyComplete()
            }
            start()
        }
    }

    private fun updateHeadAndTail(): Deferred<Unit> {
        val deferred = RunnableGroup(this, handlerUpdate).name("updateHeadAndTailNoSync")
        .build<Unit> { deferred ->
            addRun {
                updateTail( false)
                    ?.onComplete {
                        next()
                    }
                    ?: next()
            }
            addRun {
                updateHead( false)
                    ?.onComplete {
                        next()
                    }
                    ?: next()
            }
            setOnDoneRun {
                deferred.notifyComplete()
            }
            start()
        }
        return deferred
    }
    private fun newDataList(): MutableList<T> {
        return ArrayList()
    }

    private fun reduceHeadNoLock() {
        if (currentIndex!! - firstIndex!! <= sizeToDownload) {
            return
        }
        val sizeToRemove = currentIndex!! - firstIndex!! - sizeToDownload
        dataList = dataList!!.subList(sizeToRemove, dataList!!.size)
/*#-debug-> DebugLog.start().send(me(), "reduce head from " + firstIndex + " to " + (firstIndex!! + sizeToRemove))
            .end() <-debug-#*/
        firstIndex = firstIndex!! + sizeToRemove
    }
    private fun updateTail(waitCompletion: Boolean): Deferred<Unit>? {
        synchronized(me()){updateTailJob}?.let { deferred ->
            if (waitCompletion) {
                runBlocking(Dispatchers.Unconfined) {
                    deferred.join()
                }
            }
            return deferred
        }
        ?: let {
            lock(LockState.LOCAL)
            val result = lastIndex!! - currentIndex!! > triggerSizeBeforeDownload
            unlock()
            if (result) {
                return null
            }
            val deferred:CompletableDeferred<Unit> = CompletableDeferred()
            handlerUpdate.postRun(this){
                lock(LockState.LOCAL)
                val index = lastIndex!! + 1
                unlock()
                dataProvider!!.select(index, sizeToDownload).onComplete {
                    it.getCompleted()?.let select@{
                        var newDatas = it
                        if(newDatas.isEmpty()){
                            return@select
                        }
                        lock(LockState.LOCAL)
                        if (index <= lastIndex!!) {
                            val diff = lastIndex!! - index
                            if (index + diff > newDatas.size - 1) {
                                unlock()
                                return@select
                            }
                            newDatas = newDatas.subList(index + diff + 1, newDatas.size)
                        }
                        if(newDatas.isEmpty()) {
                            unlock()
                            return@select
                        }
/*#-debug-> DebugLog.start().send(me(),
                            "increase tail from " + (lastIndex!! + 1) + " to " + (lastIndex!! + newDatas.size)
                        ).end() <-debug-#*/
                        lastIndex = lastIndex!! + newDatas.size
                        dataList!!.addAll(newDatas)
                        reduceHeadNoLock()
                        unlock()
                    }
                    deferred.notifyComplete()
                }
                with(deferred) {
                    updateTailJob = deferred
                    onComplete {
                        updateTailJob = null
                    }
                }
            }
            if(waitCompletion) {
                runBlocking(Dispatchers.Unconfined) {
                    deferred.join()
                }
            }
            return deferred
        }
    }
    override fun hasNext(): Boolean {
        lockAll()
        when {
            currentIndex == null -> {
                unlock()
                setNextIndex(null, true)
                lock(LockState.LOCAL)
            }
            currentIndex!! >= lastIndex!! -> {
                unlock()
                updateTail(true)
                lock(LockState.LOCAL)
            }
            else -> {
                updateTail(false)
            }
        }
        val result = currentIndex != null && currentIndex!! < lastIndex!!
        unlockAll()
        return result
    }
    override fun next(): T {
        lockAll()
        currentIndex = currentIndex!! + 1
        val item =  dataList!![currentIndex!! - firstIndex!!]
        unlockAll()
        return item
    }

    private fun reduceTailNoLock() {
        if (lastIndex!! - currentIndex!! <= sizeToDownload) {
            return
        }
        val sizeToRemove = lastIndex!! - currentIndex!! - sizeToDownload - 1
        dataList = dataList!!.subList(0, dataList!!.size - sizeToRemove)
/*#-debug-> DebugLog.start().send(me(), "reduce tail from " + lastIndex + " to " + (lastIndex!! - sizeToRemove))
            .end() <-debug-#*/
        lastIndex = lastIndex!! - sizeToRemove
    }
    private fun updateHead(waitCompletion: Boolean): Deferred<Unit>? {
        synchronized(me()){updateHeadJob}?.let { deferred ->
            if (waitCompletion) {
                runBlocking(Dispatchers.Unconfined) {
                    deferred.join()
                }
            }
            return deferred
        } ?: let {
            lock(LockState.LOCAL)
            val result = currentIndex!! - firstIndex!! >= triggerSizeBeforeDownload
            unlock()
            if (result) {
                return null
            }
            val deferred:CompletableDeferred<Unit> = CompletableDeferred()
            handlerUpdate.postRun(this){
                lock(LockState.LOCAL)
                val finalIndex = firstIndex!! - sizeToDownload
                unlock()
                dataProvider!!.select(finalIndex, sizeToDownload).onComplete {
                    it.getCompleted()?.let select@{
                        var newDatas = it.toMutableList()
                        if (newDatas.isEmpty()) {
                            return@select
                        }
                        lock(LockState.LOCAL)
                        val index = finalIndex + (sizeToDownload - newDatas.size)
                        if (firstIndex!! < index + sizeToDownload) {
                            val diff = firstIndex!! - index
                            if (diff > 0) {
                                newDatas = newDatas.subList(0, diff)
                            } else {
                                newDatas.clear()
                            }
                        }
                        if (newDatas.isEmpty()) {
                            unlock()
                            return@select
                        }
/*#-debug-> DebugLog.start().send(me(),
                            "increase head from " + (firstIndex!! - 1) + " to " + (firstIndex!! - newDatas.size)
                        ).end() <-debug-#*/
                        firstIndex = firstIndex!! - newDatas.size
                        newDatas.addAll(dataList!!)
                        dataList = newDatas
                        reduceTailNoLock()
                        unlock()
                    }
                    deferred.notifyComplete()
                }
                with(deferred) {
                    updateHeadJob = deferred
                    onComplete {
                        updateHeadJob = null
                    }
                }
            }
            if (waitCompletion) {
                runBlocking(Dispatchers.Unconfined) {
                    deferred.join()
                }
            }
            return deferred
        }
    }
    override fun hasPrevious(): Boolean {
        lockAll()
        when {
            currentIndex == null -> {
                unlock()
                setNextIndex(null, true)
                lock(LockState.LOCAL)
            }
            firstIndex!! >= currentIndex!! -> {
                unlock()
                updateHead(true)
                lock(LockState.LOCAL)
            }
            else -> {
                updateHead(false)
            }
        }
        val result = currentIndex != null && firstIndex!! <= currentIndex!!
        unlockAll()
        return result
    }
    override fun previous(): T {
        lockAll()
        val item = dataList!![currentIndex!! - firstIndex!!]
        currentIndex = currentIndex!! - 1
        unlockAll()
        return item
    }

    fun isLoadedNoLock(index: Int): Boolean {
        return currentIndex != null && index >= firstIndex!! && index <= lastIndex!!
    }
    fun isLoaded(index: Int): Boolean {
        lockAll()
        val result = currentIndex != null && index >= firstIndex!! && index <= lastIndex!!
        unlockAll()
        return result
    }

    fun get(index: Int, setAsCurrentIndex:Boolean = false): T {
        if(setAsCurrentIndex){
            setCurrentIndex(index)
        }
        lock(LockState.LOCAL)
        val item = dataList!![index - firstIndex!!]
        unlock()
        return item
    }

    fun getFromBuffer(predicate: PredicateW<T>): T? {
        return dataList?.find {
            predicate.test(it)
        }
    }
    fun indexOfFromBuffer(predicate: PredicateW<T>): Int? {
        dataList?.forEachIndexed { index, item ->
            if (predicate.test(item)) {
                return index + firstIndex!!
            }
        }
        return null
    }

    override fun remove() {
/*#-debug-> DebugException.start().notImplemented().end() <-debug-#*/
    }
    override fun set(t: T) {
/*#-debug-> DebugException.start().notImplemented().end() <-debug-#*/
    }
    override fun add(t: T) {
/*#-debug-> DebugException.start().notImplemented().end() <-debug-#*/
    }
    override fun nextIndex(): Int {
/*#-debug-> DebugException.start().notImplemented().end() <-debug-#*/
        return NULL_INDEX
    }
    override fun previousIndex(): Int {
/*#-debug-> DebugException.start().notImplemented().end() <-debug-#*/
        return NULL_INDEX
    }

    fun notifyInsert(index: Int, t: T) {
        lockAll()
        when {
            this.isLoadedNoLock(index) -> {
                dataList!!.add(index - firstIndex!!, t)
                lastIndex = lastIndex!! + 1
            }
            currentIndex != null -> {
                if(index < firstIndex!!){
                    firstIndex = firstIndex!! + 1
                    lastIndex = lastIndex!! + 1
                }
            }
            else -> {
                firstIndex = index
                lastIndex = firstIndex
                currentIndex = lastIndex
                dataList = newDataList()
                dataList!!.add(t)
            }
        }
        unlockAll()
    }
    fun notifyUpdate(index: Int, t: T) {
        if (this.isLoadedNoLock(index)) {
            dataList!!.removeAt(index - firstIndex!!)
            dataList!!.add(index - firstIndex!!, t)
        }
    }
    fun notifyUpdateAll() {
        initIndex()
    }
    fun notifyRemove(t: T) {
        lockAll()
        dataList?.forEachIndexed { index, item ->
            if (item == t) {
                notifyRemoveNoLock(index + firstIndex!!)
                return@forEachIndexed
            }
        }
        unlockAll()
    }
    fun notifyRemove(index: Int) {
        lockAll()
        notifyRemoveNoLock(index)
        unlockAll()
    }
    private fun notifyRemoveNoLock(index: Int) {
        when {
            this.isLoadedNoLock(index) -> {
                dataList!!.removeAt(index - firstIndex!!)
                if (index == lastIndex) {
                    currentIndex = currentIndex!! - 1
                }
                lastIndex = lastIndex!! - 1
                if (lastIndex!! < firstIndex!!) {
                    firstIndex = null
                    currentIndex = null
                    lastIndex = null
                    dataList = null
                }
            }
            currentIndex != null -> {
                if(index < firstIndex!!){
                    firstIndex = firstIndex!! - 1
                    lastIndex = lastIndex!! - 1
                }
            }
        }
    }

    protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
    }
}