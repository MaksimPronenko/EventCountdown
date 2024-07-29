package pronenko.eventcountdown.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pronenko.eventcountdown.data.Repository
import pronenko.eventcountdown.domain.UniqueIdGenerator
import pronenko.eventcountdown.models.Event

class EditorViewModel(
    val repository: Repository,
    val uniqueIdGenerator: UniqueIdGenerator
) : ViewModel() {

    var permissionsGranted = false

    var eventName: String? = null
    private var loadedEventName: String = ""

    var nameStateFlow = MutableStateFlow(value = "")

    var dateStateFlow = MutableStateFlow(value = "")

    var reminderTypeStateFlow = MutableStateFlow(value = false)

    var iconNumberStateFlow = MutableStateFlow(value = 0)

    var colorNumberStateFlow = MutableStateFlow(value = 0)

    fun loadEvent(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val event: Event? = repository.getEvent(name)
            if (event != null) {
                loadedEventName = event.name
                nameStateFlow.value = event.name
                dateStateFlow.value = event.date
                reminderTypeStateFlow.value = event.reminderType
                iconNumberStateFlow.value = event.iconNumber
                colorNumberStateFlow.value = event.colorNumber
            }
        }
    }

    fun saveName(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            nameStateFlow.value = name
        }
    }

    fun saveDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dateStateFlow.value = date
        }
    }

    fun saveReminderType(reminderType: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderTypeStateFlow.value = reminderType
        }
    }

    fun saveIconNumber(iconNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            iconNumberStateFlow.value = iconNumber
        }
    }

    fun saveColorNumber(colorNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            colorNumberStateFlow.value = colorNumber
        }
    }

    fun saveEvent(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addOrUpdateEvent(
                Event(
                    name = nameStateFlow.value,
                    date = dateStateFlow.value,
                    reminderType = reminderTypeStateFlow.value,
                    iconNumber = iconNumberStateFlow.value,
                    colorNumber = colorNumberStateFlow.value
                )
            )
            if (loadedEventName != nameStateFlow.value) {
                repository.deleteEvent(loadedEventName)
                WorkManager.getInstance(context)
                    .cancelAllWorkByTag("${loadedEventName}_notification")
                WorkManager.getInstance(context).cancelAllWorkByTag("${loadedEventName}_event")
            }
        }
    }

    fun getUniqueId(): Int = uniqueIdGenerator.getNextId()
}