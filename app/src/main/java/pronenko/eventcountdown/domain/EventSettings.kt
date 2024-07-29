package pronenko.eventcountdown.domain

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import pronenko.eventcountdown.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val datePattern = "dd.MM.yyyy"

@Composable
fun getIconByNumber(number: Int): Painter = when (number) {
    1 -> painterResource(id = R.drawable.jealous)
    2 -> painterResource(id = R.drawable.stupid)
    3 -> painterResource(id = R.drawable.sinister_smile)
    4 -> painterResource(id = R.drawable.happy)
    5 -> painterResource(id = R.drawable.think)
    6 -> painterResource(id = R.drawable.happy_2)
    7 -> painterResource(id = R.drawable.tongue)
    8 -> painterResource(id = R.drawable.in_love)
    9 -> painterResource(id = R.drawable.dizziness)
    10 -> painterResource(id = R.drawable.get_ill)
    11 -> painterResource(id = R.drawable.cool)
    else -> painterResource(id = R.drawable.poker_face)
}

@Composable
fun getColorByNumber(number: Int): Color = when (number) {
    1 -> colorResource(id = R.color.pink)
    2 -> colorResource(id = R.color.yellow)
    3 -> colorResource(id = R.color.purple)
    4 -> colorResource(id = R.color.red)
    5 -> colorResource(id = R.color.black)
    6 -> colorResource(id = R.color.green_light)
    7 -> colorResource(id = R.color.teal)
    8 -> colorResource(id = R.color.green)
    9 -> colorResource(id = R.color.crimson)
    10 -> colorResource(id = R.color.blue)
    11 -> colorResource(id = R.color.maroon)
    else -> colorResource(id = R.color.orange)
}

fun getNotificationDelay(eventDate: String, reminderType: Boolean): List<Long> {
    val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
    val parsedDate: Date = dateFormat.parse(eventDate) ?: return listOf(0L, 0L)
    val currentTime = Date()
    val notificationPeriod = if (reminderType) 604800000L else 86400000L
    val eventDelayInMillis = parsedDate.time - currentTime.time
    val notificationDelayInMillis = eventDelayInMillis - notificationPeriod
    Log.d("Notification", "notificationDelay = $notificationDelayInMillis; eventDelay = $eventDelayInMillis")
    return listOf(notificationDelayInMillis, eventDelayInMillis)
}

fun numberTo2Digits(number: Int): String =
    if (number < 9) "0$number" else number.toString()

fun isEventArrived(eventDate: String): Boolean {
    val dateFormat = SimpleDateFormat(datePattern, Locale.getDefault())
    val parsedDate: Date = dateFormat.parse(eventDate) ?: return false
    val currentTime = Date()
    return currentTime.time > parsedDate.time
}