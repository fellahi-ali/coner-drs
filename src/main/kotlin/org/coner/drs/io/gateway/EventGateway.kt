package org.coner.drs.io.gateway

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.coner.drs.domain.entity.Event
import org.coner.drs.domain.entity.RunEvent
import org.coner.drs.io.DrsIoController
import org.coner.drs.io.db.EntityWatchEvent
import org.coner.drs.io.db.entity.EventDbEntity
import org.coner.drs.domain.mapper.EventMapper
import org.coner.drs.io.db.WatchedEntity
import org.coner.snoozle.db.jvm.watchListing
import tornadofx.*

class EventGateway : Controller() {

    val io: DrsIoController by inject()
    private val db = io.model.db!!
    val mapper by lazy { EventMapper(io.model.pathToCrispyFishDatabase!!) }

    fun list(): List<Event> {
        return db.list<EventDbEntity>().map { mapper.toUiEntity(it)!! }
    }

    fun asRunEvent(event: Event): RunEvent? {
        val dbEntity = db.get(EventDbEntity::id to event.id)
        return mapper.toRunEventUiEntity(dbEntity)
    }

    fun save(event: Event) {
        db.put(mapper.toDbEntity(event))
    }

    fun watchList(): Observable<EntityWatchEvent<Event>> = db.watchListing<EventDbEntity>()
            .subscribeOn(Schedulers.io())
            .map { EntityWatchEvent<Event>(
                    watchEvent = it.watchEvent,
                    id = it.id,
                    entity = mapper.toUiEntity(it.entity)
            ) }

}

