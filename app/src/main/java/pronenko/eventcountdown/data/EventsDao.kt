package pronenko.eventcountdown.data

import androidx.room.*
import pronenko.eventcountdown.models.Event

@Dao
interface EventsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEvent(event: Event)

    @Query("SELECT EXISTS (SELECT name FROM events WHERE name = :name)")
    fun isEventExists(name: String): Boolean

    @Update
    fun updateEvent(event: Event)

    @Query("DELETE FROM events WHERE name = :name")
    fun deleteEvent(name: String)

    @Query("SELECT * FROM events WHERE name = :name")
    fun getEvent(name: String): Event?

    @Query("SELECT * FROM events")
    fun getEvents(): List<Event>

}