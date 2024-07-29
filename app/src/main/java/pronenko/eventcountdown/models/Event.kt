package pronenko.eventcountdown.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "reminder_type")
    val reminderType: Boolean,
    @ColumnInfo(name = "item_number")
    val iconNumber: Int,
    @ColumnInfo(name = "color_number")
    val colorNumber: Int
) {
    fun toEventWithState() = EventWithState(
        name = name,
        date = date,
        reminderType = reminderType,
        iconNumber = iconNumber,
        colorNumber = colorNumber,
        eventState = false
    )
}