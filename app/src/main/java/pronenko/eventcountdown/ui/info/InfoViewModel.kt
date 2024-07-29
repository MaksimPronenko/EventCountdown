package pronenko.eventcountdown.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import pronenko.eventcountdown.data.Repository
import pronenko.eventcountdown.models.Event

class InfoViewModel(
    val repository: Repository
) : ViewModel() {

    var permissionsGranted = false

    var eventName: String? = null

    private var event: Event? = null
    var eventStateFlow = MutableStateFlow(value = event)

    fun loadEvent(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            event = repository.getEvent(name)
            eventStateFlow.value = event
        }
    }
}