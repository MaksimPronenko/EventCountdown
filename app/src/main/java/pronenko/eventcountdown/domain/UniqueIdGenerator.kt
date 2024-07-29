package pronenko.eventcountdown.domain

import android.content.Context

class UniqueIdGenerator(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun getNextId(): Int {
        val currentId = sharedPreferences.getInt("currentId", 0)
        val newId = currentId + 1
        editor.putInt("currentId", newId)
        editor.apply()
        return newId
    }
}