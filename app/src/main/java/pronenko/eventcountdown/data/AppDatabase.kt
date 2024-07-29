package pronenko.eventcountdown.data

import androidx.room.Database
import androidx.room.RoomDatabase
import pronenko.eventcountdown.models.Event

@Database(
    entities = [
        Event::class
    ], version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventsDao(): EventsDao
}