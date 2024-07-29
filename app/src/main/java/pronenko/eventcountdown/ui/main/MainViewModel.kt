package pronenko.eventcountdown.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pronenko.eventcountdown.data.Repository
import pronenko.eventcountdown.models.Event
import pronenko.eventcountdown.models.EventWithState

private const val TAG = "MainSwipe"

class MainViewModel(
    val repository: Repository
) : ViewModel() {

    var permissionsGranted = false

    private var events: MutableList<EventWithState> =
        mutableListOf()
    var eventsStateFlow = MutableStateFlow(value = events.toList())

    fun loadEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            events = eventsToEventsWithState(repository.getEvents())
            eventsStateFlow.value = events.toList()
        }
    }

    fun deleteEvent(name: String) {
        Log.d(TAG, "deleteEvent($name)")
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(name)
            events = eventsToEventsWithState(repository.getEvents())
            eventsStateFlow.value = events.toList()
        }
    }

    fun changeEventBoxState(name: String, targetType: Boolean) {
        Log.d(TAG, "changeEventBoxState($name, $targetType)")
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "events = $events")
            val updatedEvents = events.map { event ->
                if (event.name == name) {
                    event.copy(eventState = targetType)
                } else {
                    event
                }
            }
            Log.d(TAG, "updatedEvents = $updatedEvents")
            events = updatedEvents.toMutableList()
            eventsStateFlow.value = updatedEvents
        }
    }

    private fun eventsToEventsWithState(events: List<Event>): MutableList<EventWithState> {
        val eventWithStateList = mutableListOf<EventWithState>()
        events.forEach {
            eventWithStateList.add(it.toEventWithState())
        }
        return eventWithStateList
    }
}