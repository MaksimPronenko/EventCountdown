package pronenko.eventcountdown.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.WorkManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import pronenko.eventcountdown.R
import pronenko.eventcountdown.domain.getColorByNumber
import pronenko.eventcountdown.domain.getIconByNumber
import pronenko.eventcountdown.domain.isEventArrived
import pronenko.eventcountdown.models.EventWithState
import pronenko.eventcountdown.ui.editor.EVENT_NAME
import pronenko.eventcountdown.ui.editor.EditorFragment
import pronenko.eventcountdown.ui.info.InfoFragment

const val PERMISSIONS = "permissions"

private const val TAG = "MainSwipeFragment"

class MainFragment : Fragment() {

    private val viewModel by viewModel<MainViewModel>()

    companion object {
        fun newInstance() = MainFragment()

        private val REQUEST_PERMISSIONS: Array<String> = buildList {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.values.all { it }) {
                viewModel.permissionsGranted = true
//                Toast.makeText(context, getString(R.string.permissions_granted), Toast.LENGTH_SHORT).show()
            } else {
                viewModel.permissionsGranted = false
                Toast.makeText(context, getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.permissionsGranted = arguments?.getBoolean(PERMISSIONS) ?: false
        viewModel.loadEvents()
        val view = ComposeView(requireContext())
        checkPermissions()
        view.setContent {
            val eventsState = viewModel.eventsStateFlow.collectAsState()
            Main(
                events = eventsState.value,
                onClick = { name ->
                    val infoFragment = InfoFragment()
                    val bundle = Bundle()
                    bundle.putString(EVENT_NAME, name)
                    bundle.putBoolean(PERMISSIONS, viewModel.permissionsGranted)
                    infoFragment.arguments = bundle
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.container, infoFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                },
                changeEventBoxState = { name, targetType ->
                    viewModel.changeEventBoxState(name, targetType)
                },
                deleteEvent = { name ->
                    viewModel.deleteEvent(name)
                    WorkManager.getInstance(requireContext()).cancelAllWorkByTag(name) },
                addEvent = { _ ->
                    val editorFragment = EditorFragment()
                    val bundle = Bundle()
                    bundle.putBoolean(PERMISSIONS, viewModel.permissionsGranted)
                    editorFragment.arguments = bundle
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.container, editorFragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }
            )
        }
        return view
    }

    private fun checkPermissions() {
        val isAllGranted = REQUEST_PERMISSIONS.all { permission ->
            context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    permission
                )
            } == PackageManager.PERMISSION_GRANTED
        }
        if (isAllGranted) {
            viewModel.permissionsGranted = true
//            Toast.makeText(context, getString(R.string.permissions_granted), Toast.LENGTH_SHORT).show()
        } else {
            launcher.launch(REQUEST_PERMISSIONS)
        }
    }
}

@Composable
fun Main(
    events: List<EventWithState>,
    onClick: (String) -> Unit,
    changeEventBoxState: (String, Boolean) -> Unit,
    deleteEvent: (String) -> Unit,
    addEvent: (String) -> Unit,
) {
    if (events.isEmpty()) NoEvents(addEvent)
    else EventsList(events, onClick, changeEventBoxState, deleteEvent, addEvent)
}

@Composable
fun NoEvents(
    addEvent: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .background(color = colorResource(id = R.color.grey_background))
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Title(title = stringResource(id = R.string.main))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.main_no_events),
                contentDescription = null,
                modifier = Modifier
                    .width(408.dp)
                    .height(271.dp)
                    .padding(start = 11.dp, end = 11.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = stringResource(id = R.string.no_events),
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, top = 15.dp)
                    .alpha(0.57f),
                fontFamily = FontFamily(Font(R.font.poppins_regular_400)),
                fontSize = 17.sp,
                color = colorResource(id = R.color.black)
            )
        }
        ButtonOrange(
            buttonText = stringResource(id = R.string.add_event),
            paddingBottom = 99.dp,
            eventName = "",
            onClick = addEvent,
            enabled = true
        )
    }
}

@Composable
fun EventsList(
    events: List<EventWithState>,
    onClick: (String) -> Unit,
    changeEventBoxState: (String, Boolean) -> Unit,
    deleteEvent: (String) -> Unit,
    addEvent: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.grey_background))
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 188.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title(title = stringResource(id = R.string.main))
            Column(
                modifier = Modifier
                    .padding(top = 70.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                events.forEach { event ->
                    Log.d(
                        TAG,
                        "Обновление списка: name = ${event.name}, state = ${event.eventState}"
                    )
                    EventBox(
                        event = event,
                        onClick = onClick,
                        changeEventBoxState = changeEventBoxState,
                        deleteEvent = deleteEvent
                    )
                    Spacer(modifier = Modifier.padding(top = 17.dp))
                }
                Spacer(modifier = Modifier.padding(top = 15.dp))
            }
        }
        Box(
            Modifier.align(Alignment.BottomCenter)
        ) {
            ButtonOrange(
                buttonText = stringResource(id = R.string.add_event),
                paddingBottom = 99.dp,
                eventName = "",
                onClick = addEvent,
                enabled = true
            )
        }
    }
}

@Composable
fun Title(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = 24.dp,
            end = 24.dp,
            top = 45.dp
        ),
        fontFamily = FontFamily(Font(R.font.poppins_medium_500)),
        textAlign = TextAlign.Center,
        fontSize = 18.sp,
        color = colorResource(id = R.color.black)
    )
}

@Composable
fun ButtonOrange(
    buttonText: String,
    paddingTop: Dp = 0.dp,
    paddingBottom: Dp,
    eventName: String,
    onClick: (String) -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .padding(start = 58.dp, end = 58.dp, top = paddingTop, bottom = paddingBottom)
            .fillMaxWidth()
            .height(70.dp)
            .background(
                color = if (enabled) colorResource(R.color.orange) else colorResource(R.color.grey),
                shape = RoundedCornerShape(30.dp)
            )
            .clickable { if (enabled) onClick(eventName) },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = buttonText,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp),
            fontFamily = FontFamily(Font(R.font.poppins_black_900)),
            fontSize = 17.sp,
            color = colorResource(id = R.color.grey_text)
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBox(
    event: EventWithState,
    onClick: (String) -> Unit,
    changeEventBoxState: (String, Boolean) -> Unit,
    deleteEvent: (String) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(event.name) {
        offsetX = 0f
    }
    Box(
        modifier = Modifier
            .padding(start = 62.dp, end = 16.dp)
            .fillMaxWidth()
            .pointerInput(event.name) {
                detectHorizontalDragGestures { change, dragAmount ->
                    offsetX += dragAmount
                    if(offsetX > 0f) offsetX = 0f
                    change.consume()
                    if (dragAmount > 0) {
                        Log.d(
                            TAG,
                            "name = ${event.name}; offsetX = $offsetX; dragAmount = $dragAmount"
                        )
                        changeEventBoxState(event.name, false)
                    } else {
                        Log.d(
                            TAG,
                            "id = ${event.name}; offsetX = $offsetX; dragAmount = $dragAmount"
                        )
                        if (offsetX < -200)
                            deleteEvent(event.name)
                        else
                            changeEventBoxState(event.name, true)
                    }
                }
            }
            .offset { IntOffset(offsetX.toInt(), 0) }
            .clickable { onClick(event.name) }
    ) {
        Box(
            modifier = Modifier
                .padding(end = 37.dp)
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
        if (event.eventState) {
            Box(
                modifier = Modifier
                    .width(37.dp)
                    .padding(end = 0.dp)
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.delete),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(colorResource(id = R.color.grey_dark)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMain() {
    Main(
        events = listOf(
            EventWithState(
                name = "Событие 3",
                date = "24.07.2024",
                reminderType = false,
                iconNumber = 1,
                colorNumber = 5,
                eventState = true
            )
        ),
        onClick = {},
        changeEventBoxState = { _, _ -> },
        deleteEvent = {},
        addEvent = {}
    )
}