package pronenko.eventcountdown.ui.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import pronenko.eventcountdown.R
import pronenko.eventcountdown.domain.getColorByNumber
import pronenko.eventcountdown.domain.getIconByNumber
import pronenko.eventcountdown.models.Event
import pronenko.eventcountdown.ui.editor.EVENT_NAME
import pronenko.eventcountdown.ui.editor.TitleOrange
import pronenko.eventcountdown.ui.main.ButtonOrange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlinx.datetime.toJavaZoneId
import pronenko.eventcountdown.domain.isEventArrived
import pronenko.eventcountdown.ui.editor.EditorFragment
import pronenko.eventcountdown.ui.main.MainFragment
import pronenko.eventcountdown.ui.main.PERMISSIONS
import java.time.format.DateTimeFormatter
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

class InfoFragment : Fragment() {

    private val viewModel by viewModel<InfoViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext())
        val receivedName = arguments?.getString(EVENT_NAME)
        if (receivedName != null) {
            if (receivedName != viewModel.eventName)
                viewModel.loadEvent(receivedName)
        }
        viewModel.permissionsGranted = arguments?.getBoolean(PERMISSIONS) ?: false
        view.setContent {
            val eventState = viewModel.eventStateFlow.collectAsState()
            Info(
                event = eventState.value,
                editEvent = { name ->
                    val editorFragment = EditorFragment()
                    val bundle = Bundle()
                    bundle.putString(EVENT_NAME, name)
                    bundle.putBoolean(PERMISSIONS, viewModel.permissionsGranted)
                    editorFragment.arguments = bundle
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.container, editorFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                },
                back = {
                    val mainFragment = MainFragment()
                    val bundle = Bundle()
                    bundle.putBoolean(PERMISSIONS, viewModel.permissionsGranted)
                    mainFragment.arguments = bundle
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.container, mainFragment)
//                            .addToBackStack(null)
                            .commit()
                    }
                }
            )
        }
        return view
    }
}

@Composable
fun Info(
    event: Event?,
    editEvent: (String) -> Unit,
    back: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(color = colorResource(id = R.color.grey_background))
            .fillMaxHeight()
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (event != null) {
            EventBoxStatic(
                event = event
            )
            TitleOrange(
                text = stringResource(id = R.string.timer_before_event),
                topPadding = 32.dp,
                startPadding = 32.dp,
                endPadding = 32.dp
            )
            Timer(eventDate = event.date)
            Spacer(modifier = Modifier.weight(1f))
            ButtonOrange(
                buttonText = stringResource(id = R.string.edit),
                paddingBottom = 0.dp,
                eventName = event.name,
                onClick = editEvent,
                enabled = true
            )
        } else Spacer(modifier = Modifier.weight(1f))
        ButtonWhiteBack(
            paddingBottom = 48.dp,
            onClick = back
        )
    }
}

@Composable
fun ButtonWhiteBack(paddingBottom: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 58.dp, end = 58.dp, top = 19.dp, bottom = paddingBottom)
            .fillMaxWidth()
            .height(70.dp)
            .border(
                width = 1.dp,
                color = colorResource(R.color.orange),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.back),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp),
            fontFamily = FontFamily(Font(R.font.poppins_black_900)),
            fontSize = 17.sp,
            color = colorResource(id = R.color.orange)
        )
    }
}

@Composable
fun EventBoxStatic(
    event: Event
) {
    Box(
        modifier = Modifier
            .padding(top = 107.dp, start = 57.dp, end = 58.dp)
            .background(
                color = colorResource(id = R.color.white),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.padding(
                start = 15.dp,
                end = 15.dp,
                top = 21.dp,
                bottom = 21.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(60.dp)
                    .background(
                        color = getColorByNumber(event.colorNumber),
                        shape = RoundedCornerShape(30.dp)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = getIconByNumber(number = event.iconNumber),
                    contentDescription = null,
                    modifier = Modifier,
                    contentScale = ContentScale.Fit
                )
            }
            Column(
                modifier = Modifier,
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = event.name,
                    modifier = Modifier
                        .padding(start = 18.dp, end = 18.dp),
                    fontFamily = FontFamily(Font(R.font.poppins_semibold_600)),
                    fontSize = 20.sp,
                    color = colorResource(id = R.color.black)
                )
                Text(
                    text = if (isEventArrived(event.date)) stringResource(id = R.string.event_arrived)
                        else stringResource(id = R.string.start_date) + " " + event.date +
                            stringResource(id = R.string.year_abbreviated),
                    modifier = Modifier
                        .padding(start = 18.dp, end = 18.dp, top = 13.dp),
                    fontFamily = FontFamily(Font(R.font.poppins_medium_500)),
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.orange_event_text)
                )
            }
        }
    }
}

@Composable
fun Timer(eventDate: String) {
    var remainingTime by remember { mutableStateOf(calculateRemainingTime(eventDate)) }

    LaunchedEffect(eventDate) {
        while (true) {
            remainingTime = calculateRemainingTime(eventDate)
            delay(1000)
        }
    }

    val totalSeconds = if (isEventArrived(eventDate)) 0L else remainingTime.inWholeSeconds

    val years = totalSeconds / (365 * 24 * 60 * 60)
    val months = (totalSeconds % (365 * 24 * 60 * 60)) / (30 * 24 * 60 * 60)
    val weeks = (totalSeconds % (30 * 24 * 60 * 60)) / (7 * 24 * 60 * 60)
    val days = (totalSeconds % (7 * 24 * 60 * 60)) / (24 * 60 * 60)
    val hours = (totalSeconds % (24 * 60 * 60)) / (60 * 60)
    val minutes = (totalSeconds % (60 * 60)) / 60
    val seconds = totalSeconds % 60

    Column(
        modifier = Modifier.padding(26.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier
        ) {
            TimerNumberBox(
                number = years.toInt(),
                description = stringResource(id = R.string.years)
            )
            TimerNumberBox(
                startPadding = 14.dp,
                number = months.toInt(),
                description = stringResource(id = R.string.months)
            )
            TimerNumberBox(
                startPadding = 14.dp,
                number = weeks.toInt(),
                description = stringResource(id = R.string.weeks)
            )
            TimerNumberBox(
                startPadding = 14.dp,
                number = days.toInt(),
                description = stringResource(id = R.string.days)
            )
            TimerNumberBox(
                startPadding = 14.dp,
                number = hours.toInt(),
                description = stringResource(id = R.string.hours)
            )
        }
        Row(
            modifier = Modifier.padding(top = 20.dp)
        ) {
            TimerNumberBox(
                number = minutes.toInt(),
                description = stringResource(id = R.string.minutes)
            )
            TimerNumberBox(
                startPadding = 14.dp,
                number = seconds.toInt(),
                description = stringResource(id = R.string.seconds)
            )
        }
    }
}

fun calculateRemainingTime(targetDate: String): Duration {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val targetDateTime = java.time.LocalDate.parse(targetDate, formatter)
        .atStartOfDay(TimeZone.currentSystemDefault().toJavaZoneId())
        .toInstant()
        .toKotlinInstant()
    val now = Clock.System.now()
    return targetDateTime.minus(now).toJavaDuration().toKotlinDuration()
}

@Composable
fun TimerNumberBox(startPadding: Dp = 0.dp, number: Int, description: String) {
    Column(
        modifier = Modifier.padding(start = startPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = colorResource(id = R.color.white),
                    shape = RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                modifier = Modifier,
                fontFamily = FontFamily(Font(R.font.roboto_regular_400)),
                fontSize = 24.sp,
                color = colorResource(id = R.color.black)
            )
        }
        Text(
            text = description,
            modifier = Modifier.padding(top = 5.dp),
            fontFamily = FontFamily(Font(R.font.roboto_regular_400)),
            fontSize = 12.sp,
            color = colorResource(id = R.color.black)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewEditor() {
    Info(
        event = Event(
            name = "Событие 4",
            date = "25.07.2024",
            reminderType = false,
            iconNumber = 3,
            colorNumber = 6
        ),
        editEvent = {},
        back = {}
    )
}