package pronenko.eventcountdown.models

data class EventWithState(
    val name: String,
    val date: String,
    val reminderType: Boolean,
    val iconNumber: Int,
    val colorNumber: Int,
    var eventState: Boolean
)