package pronenko.eventcountdown.data

import pronenko.eventcountdown.models.Event

class Repository (val dao: EventsDao) {

    suspend fun addOrUpdateEvent(event: Event) {
        val isEventExists = dao.isEventExists(name = event.name)
        if (isEventExists) dao.updateEvent(event)
        else dao.insertEvent(event)
    }

    suspend fun deleteEvent(name: String) = dao.deleteEvent(name)

    suspend fun getEvents(): List<Event> = dao.getEvents()

    suspend fun getEvent(name: String): Event? = dao.getEvent(name)

}