package com.tezov.gofo.retrofit.unsplash

import com.tezov.lib_java.debug.DebugLog
import com.tezov.lib_java.debug.DebugException
import com.tezov.lib_java.debug.DebugTrack
import com.tezov.lib_java.type.primitive.ObjectTo
import com.tezov.gofo.application.AppConfig
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.build
import com.tezov.lib_java.async.Handler
import com.tezov.lib_java.util.UtilsList.NULL_INDEX
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import com.tezov.gofo.application.AppHandler
import com.tezov.gofo.BuildConfig
import com.tezov.lib_java.async.notifier.Notifier
import com.tezov.lib_java.async.notifier.observable.ObservableValue
import com.tezov.lib_java.async.notifier.observer.value.ObserverValue
import com.tezov.lib_java.type.defEnum.Event
import com.tezov.lib_java.type.runnable.RunnableGroup
import com.tezov.lib.adapterJavaToKotlin.async.Completable
import com.tezov.lib.adapterJavaToKotlin.async.Completable.notifyComplete
import com.tezov.lib.adapterJavaToKotlin.async.Completable.onComplete
import com.tezov.lib.adapterJavaToKotlin.async.IteratorBufferAsync_K
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.addRun
import com.tezov.lib.adapterJavaToKotlin.runnable.RunnableGroup_Kext.setOnDoneRun
import com.tezov.lib.adapterJavaToKotlin.async.defProviderAsync_K
import com.tezov.gofo.room.data.ItemPhoto
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterFeed
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterRandom
import com.tezov.gofo.room.data.dataFilterUnsplash.DataFilterSearch
import com.tezov.gofo.room.database.Database

class UnsplashCacheProvider {
    companion object {

        private fun myClass():Class<UnsplashCacheProvider>{
            return UnsplashCacheProvider::class.java
        }

        private var networkProvider:UnsplashNetworkProvider? = null
        fun getNetworkProvider():UnsplashNetworkProvider{
            return networkProvider!!
        }
        fun init(){
            networkProvider = UnsplashNetworkProvider(AppHandler.getNetworkProviderHandler())
        }
        fun quit(){
            networkProvider = null
        }
        fun newIterator(cacheProvider: defProviderAsync_K<ItemPhoto>): IteratorBufferAsync_K<ItemPhoto> {
            val iterator = IteratorBufferAsync_K<ItemPhoto>(AppHandler.getCacheIteratorHandler(), null)
            iterator.apply {
                setTriggerSizeBeforeDownload(AppConfig.ITERATOR_PHOTO_TRIGGER)
                setSizeToDownload(AppConfig.ITERATOR_PHOTO_BUFFER_SIZE)
                setDataProvider(cacheProvider)
            }
            return iterator
        }
    }
    class Random(val handlerCallback: Handler, val filter:DataFilterRandom):
        defProviderAsync_K<ItemPhoto> {
    private var photoCount:Int? = null
        private val notifier:Notifier<Void>

    init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
        notifier = Notifier(ObservableValue<Event.Is>(), false)
    }

    private fun post(event:Event.Is){
        val access = notifier.obtainAccess<ObservableValue<Event.Is>.Access>(this, null)
        access.value = event
    }
    fun observe(observer:ObserverValue<Event.Is>):Notifier.Subscription{
        return notifier.register(observer)
    }
    fun unobserve(observer:ObserverValue<Event.Is>){
        notifier.unregister(observer)
    }
    fun unobserveAll(){
            return notifier.unregisterAll()
        }

    fun resetPageCounter(){
        photoCount = null
    }

    private fun loadPage():Deferred<Int> {
        val deferred:CompletableDeferred<Int> = CompletableDeferred()
        photoCount?:let {
            photoCount = Database.photos.count(filter.getId())
        }
        getNetworkProvider().selectRandom(filter.getQuery(), orientation = filter.orientation).onComplete {
            it.getCompleted()?.takeIf{it.isNotEmpty()}?.let { result ->
                var count = 0
                Database.lock(this)
                val lastPosition = Database.photos.getMaxPosition(filter.getId())?.let {it+1}?:0
                result.forEachIndexed() { index, dataPhoto ->
                    val id = Database.photos.insertIfPositionNotBusy(
                        ItemPhoto().apply { setDataPhoto(index + lastPosition , 0, dataPhoto, filter.getId()) })
                    if (id != NULL_INDEX.toLong()) {
                        count++
                    }
                }
                Database.unlock(this)
                photoCount = photoCount!! + count
/*#-debug-> DebugLog.start().send(myClass(), "Random photoCount ${photoCount}::added:" + count + " :: ignored:" + (it.getCompleted()?.size?.minus(count)) + " :: total:" + it.getCompleted()?.size).end() <-debug-#*/
                if(count > 0){
                    post(Event.ON_CHANGE)
                }
                deferred.notifyComplete(count)
            }
            if(!it.isCompleted){
                deferred.notifyComplete(0)
            }
        }
        return deferred
    }

    override fun getFirstIndex():Deferred<Int> {
        return  Completable.notifyComplete(handlerCallback, 0)
    }
    override fun getLastIndex():Deferred<Int> {
        return  Completable.notifyComplete(handlerCallback,Int.MAX_VALUE-1)
    }
    override fun size():Deferred<Int> {
        return Completable.notifyComplete(handlerCallback,Int.MAX_VALUE)
    }
    override fun get(index:Int):Deferred<ItemPhoto?> {
        val deferred:CompletableDeferred<ItemPhoto?> = CompletableDeferred()
        if(index < 0){
            deferred.notifyComplete(handlerCallback,null)
            return deferred
        }
        fun getItem():ItemPhoto?{
            return Database.photos.getWithPosition(filter.getId(), position = index, positionBase = 0)
        }
        getItem()?.let {
            deferred.notifyComplete(handlerCallback,it)
        }
        ?: let {
            if(BuildConfig.DEBUG_ONLY){
                Database.photos.getMaxPosition(filter.getId())?.let {
                    if(index > (it + UnsplashNetworkProvider.REQUEST_COUNT)){
/*#-debug-> DebugException.start().log("try to get ${index} but max that can be reached is ${it + UnsplashNetworkProvider.REQUEST_COUNT}").end() <-debug-#*/
                    }
                }
            }
            loadPage().onComplete {
                deferred.notifyComplete(handlerCallback,getItem())
            }
        }
        return deferred
    }
    override fun select(offset: Int,length: Int): Deferred<List<ItemPhoto>?>{
        if((offset < 0) || length <= 0){
            return Completable.notifyComplete(handlerCallback,null)
        }
        return RunnableGroup(this,  Handler.fromMyLooper()).name("Random.select()")
        .build { deferred ->
            addRun {
                if(!Database.photos.isPositionBusy(filter.getId(), position = offset, positionBase = 0)){
                    if(BuildConfig.DEBUG_ONLY){
                        Database.photos.getMaxPosition(filter.getId())?.let {
                            if(offset > (it + UnsplashNetworkProvider.REQUEST_COUNT)){
/*#-debug-> DebugException.start().log("try to get ${offset} but max that can be reached is ${it + UnsplashNetworkProvider.REQUEST_COUNT}").end() <-debug-#*/
                            }
                        }
                    }
                    loadPage().onComplete {
                        next()
                    }
                }
                else{
                    next()
                }
            }
            val part = ((length-1)/UnsplashNetworkProvider.REQUEST_COUNT)+1
            for(i in 1..part){
                val position = offset + (UnsplashNetworkProvider.REQUEST_COUNT*i) - 1
                addRun {
                    if(!Database.photos.isPositionBusy(filter.getId(), position = position, positionBase = 0)){
                        loadPage().onComplete {
                            next()
                        }
                    }
                    else{
                        next()
                    }
                }
            }
            setOnDoneRun {
                deferred.notifyComplete(handlerCallback,Database.photos.select(filter.getId(), offset = offset, limit = length, positionBase = 0))
            }
            start()
        }
    }
    override fun indexOf(t: ItemPhoto): Deferred<Int?> {
        throw NotImplementedError()
    }
    override fun toDebugLog(pageStart:Int?, pageEnd:Int?): Deferred<Unit> {
        if(pageStart != null && pageEnd != null && pageStart>pageEnd){
/*#-debug-> DebugLog.start().send(myClass(), "pageStart > pageEnd").end() <-debug-#*/
            return Completable.notifyComplete(handlerCallback)
        }
        return RunnableGroup(this, Handler.fromMyLooper()).build { deferred ->
            putValue(((pageStart?:1)-1) * UnsplashNetworkProvider.REQUEST_COUNT)
            addRun{
                var offset:Int = getValue()
                select(offset, UnsplashNetworkProvider.REQUEST_COUNT).onComplete {
                    it.getCompleted()?.takeIf { it.isNotEmpty() }
                    ?.let {
                        it.forEach {
                            it.toDebugLog()
                        }
                        offset += UnsplashNetworkProvider.REQUEST_COUNT
                        putValue(offset)
                        repeat()
                    }
                    ?:let {
/*#-debug-> DebugLog.start().send(myClass(), "offset ${offset}::length ${UnsplashNetworkProvider.REQUEST_COUNT} is null").end() <-debug-#*/
                        next()
                    }
                }
            }
            setOnDoneRun{
                deferred.notifyComplete(handlerCallback)
            }
            start()
        }
    }
}
    class Search(val handlerCallback: Handler, val filter:DataFilterSearch):
        defProviderAsync_K<ItemPhoto> {
        private var pageCount:Int? = null
        private var photoCount:Int = 0
        private val notifier:Notifier<Void>

        init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
            notifier = Notifier(ObservableValue<Event.Is>(), false)
        }

        private fun post(event:Event.Is){
            val access = notifier.obtainAccess<ObservableValue<Event.Is>.Access>(this, null)
            access.value = event
        }
        fun observe(observer:ObserverValue<Event.Is>):Notifier.Subscription{
            return notifier.register(observer)
        }
        fun unobserve(observer:ObserverValue<Event.Is>){
            notifier.unregister(observer)
        }
        fun unobserveAll(){
            return notifier.unregisterAll()
        }

        fun resetPageCounter(){
            pageCount = null
            photoCount = 0
        }

        private fun loadFirstPage():Deferred<Int> {
            resetPageCounter()
            return loadPage(1)
        }
        private fun loadPage(pageNumber:Int):Deferred<Int>{
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            pageCount?.let {
                if(pageNumber > it){
/*#-debug-> DebugLog.start().send(myClass(), "Search::page number end").end() <-debug-#*/
                    deferred.notifyComplete(0)
                    return deferred
                }
            }
            filter.getQuery()?.let { query ->
                getNetworkProvider().selectSearch(query, pageNumber = pageNumber, order = filter.order, orientation = filter.orientation, color = filter.color)
                .onComplete {
                    it.getCompleted()?.takeIf{ (it.photos?.isNotEmpty() == true) && (it.pageCount != null) }?.let { result ->
                        var count = 0
                        pageCount = result.pageCount
                        photoCount = result.photoCount!!
                        val positionOffset = (pageNumber -1) * UnsplashNetworkProvider.REQUEST_COUNT
                        Database.lock(this)
                        result.photos!!.forEachIndexed { index, dataPhoto ->
                            val id = Database.photos.insertIfPositionNotBusy(
                                ItemPhoto().apply { setDataPhoto(index + positionOffset,  (photoCount -1), dataPhoto, filter.getId()) })
                            if (id != NULL_INDEX.toLong()) {
                                count++
                            }
                        }
                        Database.unlock(this)
/*#-debug-> DebugLog.start().send(myClass(), "Search page ${pageNumber}/${pageCount}::added:" + count + " :: ignored:" + (it.getCompleted()?.photos?.size?.minus(count)) + " :: total:" + it.getCompleted()?.photos?.size).end() <-debug-#*/
                        if(count > 0){
                            post(Event.ON_CHANGE)
                        }
                        deferred.notifyComplete(count)
                    }
                    if(!it.isCompleted){
                        deferred.notifyComplete(0)
                    }
                }
            }?:let {
                deferred.notifyComplete(0)
            }
            return deferred
        }
        override fun getFirstIndex():Deferred<Int> {
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            deferred.notifyComplete(handlerCallback, 0)
            return deferred
        }
        override fun getLastIndex():Deferred<Int> {
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            pageCount?.let{
                deferred.notifyComplete(handlerCallback, photoCount-1)
            }
            ?: loadFirstPage().onComplete {
                deferred.notifyComplete(handlerCallback, photoCount-1)
            }
            return deferred
        }
        override fun size():Deferred<Int> {
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            pageCount?.let{
                deferred.notifyComplete(handlerCallback, photoCount)
            }
                ?: loadFirstPage().onComplete {
                    deferred.notifyComplete(handlerCallback, photoCount)
                }
            return deferred
        }
        override fun get(index:Int):Deferred<ItemPhoto?> {
            if(index < 0){
                return Completable.notifyComplete(handlerCallback, null)
            }
            return RunnableGroup(this, Handler.fromMyLooper()).name("Search.get()").build { deferred ->
                pageCount?: addRun {
                    loadPage(Math.floor(index.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                        next()
                    }
                }
                addRun {
                    fun getItem():ItemPhoto?{
                        return Database.photos.getWithPosition(filter.getId(), position = index, positionBase = (photoCount -1))
                    }
                    getItem()?.let {
                        putValue(it)
                        next()
                    }
                    ?: loadPage(Math.floor(index.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                        putValue(getItem())
                        next()
                    }
                }
                setOnDoneRun {
                    deferred.notifyComplete(handlerCallback, getValue())
                }
                start()
            }
        }
        override fun select(offset: Int,length: Int): Deferred<List<ItemPhoto>?>  {
            if((offset < 0) || length <= 0){
                return Completable.notifyComplete(handlerCallback, null)
            }
            return RunnableGroup(this, Handler.fromMyLooper()).name("Search.select()").build { deferred ->
                addRun {
                    if((pageCount == null) || !Database.photos.isPositionBusy(filter.getId(), position = offset, positionBase = (photoCount-1))){
                        loadPage(Math.floor(offset.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                            next()
                        }
                    }
                    else{
                        next()
                    }
                }
                val part = ((length-1)/UnsplashNetworkProvider.REQUEST_COUNT)+1
                for(i in 1..part){
                    val position = offset + (UnsplashNetworkProvider.REQUEST_COUNT*i) - 1
                    addRun {
                        if(!Database.photos.isPositionBusy(filter.getId(), position = position, positionBase = (photoCount-1))){
                           loadPage(Math.floor(position.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                               next()
                           }
                        }
                        else{
                           next()
                        }
                    }
                }
                setOnDoneRun {
                    deferred.notifyComplete(handlerCallback, Database.photos.select(filter.getId(), offset = offset, limit = length, positionBase = (photoCount-1)))
                }
                start()
            }
        }
        override fun indexOf(t: ItemPhoto): Deferred<Int?> {
            throw NotImplementedError()
        }
        override fun toDebugLog(pageStart:Int?, pageEnd:Int?): Deferred<Unit> {
            if(pageStart != null && pageEnd != null && pageStart>pageEnd){
/*#-debug-> DebugLog.start().send(myClass(), "pageStart > pageEnd").end() <-debug-#*/
                return Completable.notifyComplete(handlerCallback)
            }
            return RunnableGroup(this, Handler.fromMyLooper()).name("toDebugLog()").build { deferred ->
                pageCount?: addRun {
                    loadPage(pageStart?:1).onComplete {
                        next()
                    }
                }
                putValue(((pageStart?:1)-1) * UnsplashNetworkProvider.REQUEST_COUNT)
                addRun {
                    var offset:Int = getValue()
                    select(offset, UnsplashNetworkProvider.REQUEST_COUNT).onComplete {
                        it.getCompleted()?.takeIf { it.isNotEmpty() }
                            ?.run {
                                forEach {
                                    it.toDebugLog()
                                }
                                offset += UnsplashNetworkProvider.REQUEST_COUNT
                                putValue(offset)
                                if(offset < pageCount!!){
                                    repeat()
                                }
                                else{
                                    next()
                                }
                            }
                            ?: let {
/*#-debug-> DebugLog.start().send(myClass(), "offset ${offset}::length ${UnsplashNetworkProvider.REQUEST_COUNT} is null").end() <-debug-#*/
                                next()
                            }
                    }

                }
                setOnDoneRun {
                    deferred.notifyComplete(handlerCallback)
                }
                start()
            }
        }
        protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
        }

    }
    class Feed(val handlerCallback: Handler, val filter:DataFilterFeed):
        defProviderAsync_K<ItemPhoto> {
        private var pageCount:Int? = null
        private var photoCount:Int = 0
        private val notifier:Notifier<Void>

        init {
/*#-debug-> DebugTrack.start().create(this).end() <-debug-#*/
            notifier = Notifier(ObservableValue<Event.Is>(), false)
        }

        private fun post(event:Event.Is){
            val access = notifier.obtainAccess<ObservableValue<Event.Is>.Access>(this, null)
            access.value = event
        }
        fun observe(observer:ObserverValue<Event.Is>):Notifier.Subscription{
            return notifier.register(observer)
        }
        fun unobserve(observer:ObserverValue<Event.Is>){
            notifier.unregister(observer)
        }
        fun unobserveAll(){
            return notifier.unregisterAll()
        }

        fun resetPageCounter(){
            pageCount = null
            photoCount = 0
        }

        private fun loadFirstPage():Deferred<Int> {
            resetPageCounter()
            return loadPage(1)
        }
        private fun loadPage(pageNumber:Int):Deferred<Int> {
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            pageCount?.let {
                if(pageNumber > it){
/*#-debug-> DebugLog.start().send(myClass(), "Feed::page number end").end() <-debug-#*/
                    deferred.notifyComplete(0)
                    return deferred
                }
            }
            getNetworkProvider().selectFeed(pageNumber = pageNumber, order = filter.order).onComplete {
                it.getCompleted()?.takeIf{ (it.photos?.isNotEmpty() == true) && (it.pageCount != null) }?.let { result ->
                    var count = 0
                    pageCount = result.pageCount
                    photoCount = result.photoCount!!
                    val positionOffset = (pageNumber -1) * UnsplashNetworkProvider.REQUEST_COUNT
                    Database.lock(this)
                    result.photos!!.forEachIndexed { index, dataPhoto ->
                        val id = Database.photos.insertIfPositionNotBusy(
                            ItemPhoto().apply { setDataPhoto(index + positionOffset, (photoCount -1), dataPhoto, filter.getId()) })
                        if (id != NULL_INDEX.toLong()) {
                            count++
                        }
                    }
                    Database.unlock(this)
/*#-debug-> DebugLog.start().send(myClass(), "Feed page ${pageNumber}/${pageCount}::added:" + count + " :: ignored:" + (it.getCompleted()?.photos?.size?.minus(count)) + " :: total:" + it.getCompleted()?.photos?.size).end() <-debug-#*/
                    if(count > 0){
                        post(Event.ON_CHANGE)
                    }
                    deferred.notifyComplete(count)
                }
                if(!it.isCompleted){
                    deferred.complete(0)
                }
            }
            return deferred
        }

        override fun getFirstIndex():Deferred<Int> {
            return Completable.notifyComplete(handlerCallback, 0)
        }
        override fun getLastIndex():Deferred<Int> {
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            pageCount?.let{
                deferred.notifyComplete(handlerCallback,photoCount-1)
            }
            ?: loadFirstPage().onComplete {
                deferred.notifyComplete(handlerCallback,photoCount-1)
            }
            return deferred
        }
        override fun size():Deferred<Int> {
            val deferred:CompletableDeferred<Int> = CompletableDeferred()
            pageCount?.let{
                deferred.notifyComplete(handlerCallback,photoCount)
            }
            ?: loadFirstPage().onComplete {
                deferred.notifyComplete(handlerCallback,photoCount)
            }
            return deferred
        }
        override fun get(index:Int):Deferred<ItemPhoto?> {
            if(index < 0){
                return Completable.notifyComplete(handlerCallback,null)
            }
            return RunnableGroup(this, Handler.fromMyLooper()).name("Feed.get()").build { deferred ->
                pageCount?:addRun {
                    loadPage(Math.floor(index.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                        next()
                    }
                }
                addRun {
                    fun getItem():ItemPhoto?{
                        return Database.photos.getWithPosition(filter.getId(), position = index, positionBase = (photoCount -1))
                    }
                    getItem()?.let {
                        putValue(it)
                        next()
                    }
                    ?: loadPage(Math.floor(index.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                        putValue(getItem())
                        next()
                    }
                }
                setOnDoneRun {
                    deferred.notifyComplete(handlerCallback,getValue())
                }
                start()
            }
        }

        override fun select(offset: Int,length: Int): Deferred<List<ItemPhoto>?>  {
            if((offset < 0) || length <= 0){
                return Completable.notifyComplete(handlerCallback,null)
            }
            return RunnableGroup(this, Handler.fromMyLooper()).name("Feed.select()").build { deferred ->
                addRun {
                    if((pageCount == null) || !Database.photos.isPositionBusy(filter.getId(), position = offset, positionBase = (photoCount-1))){
                        loadPage(Math.floor(offset.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                            next()
                        }
                    }
                    else{
                        next()
                    }
                }
                val part = ((length-1)/UnsplashNetworkProvider.REQUEST_COUNT)+1
                for(i in 1..part){
                    val position = offset + (UnsplashNetworkProvider.REQUEST_COUNT*i) - 1
                    addRun {
                        if(!Database.photos.isPositionBusy(filter.getId(), position = position, positionBase = (photoCount-1))){
                            loadPage(Math.floor(position.toDouble() / UnsplashNetworkProvider.REQUEST_COUNT).toInt() + 1).onComplete {
                                next()
                            }
                        }
                        else{
                            next()
                        }
                    }
                }
                setOnDoneRun {
                    deferred.notifyComplete(handlerCallback ,Database.photos.select(filter.getId(), offset = offset, limit = length, positionBase = (photoCount-1)))
                }
                start()
            }
        }
        override fun indexOf(t: ItemPhoto): Deferred<Int?> {
            throw NotImplementedError()
        }

        override fun toDebugLog(pageStart:Int?, pageEnd:Int?): Deferred<Unit> {
            if(pageStart != null && pageEnd != null && pageStart>pageEnd){
/*#-debug-> DebugLog.start().send(myClass(), "pageStart > pageEnd").end() <-debug-#*/
                return Completable.notifyComplete(handlerCallback)
            }
            return RunnableGroup(this, Handler.fromMyLooper()).name("toDebugLog()").build { deferred ->
                pageCount?:addRun {
                    loadPage(pageStart?:1).onComplete {
                        next()
                    }
                }
                putValue(((pageStart?:1)-1) * UnsplashNetworkProvider.REQUEST_COUNT)
                addRun {
                    var offset:Int = getValue()
                    select(offset, UnsplashNetworkProvider.REQUEST_COUNT).onComplete {
                        it.getCompleted()?.takeIf { it.isNotEmpty() }
                            ?.run {
                                forEach {
                                    it.toDebugLog()
                                }
                                offset += UnsplashNetworkProvider.REQUEST_COUNT
                                putValue(offset)
                                if(offset < pageCount!!){
                                    repeat()
                                }
                                else{
                                    next()
                                }
                            }
                            ?: let {
/*#-debug-> DebugLog.start().send(myClass(), "offset ${offset}::length ${UnsplashNetworkProvider.REQUEST_COUNT} is null").end() <-debug-#*/
                                next()
                            }
                    }
                }
                setOnDoneRun {
                    deferred.notifyComplete(handlerCallback)
                }
                start()
            }
        }

        protected fun finalize() {
/*#-debug-> DebugTrack.start().destroy(this).end() <-debug-#*/
        }
    }
}