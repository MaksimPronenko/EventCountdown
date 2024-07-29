package pronenko.eventcountdown.ui.editor

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Divider
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.koin.androidx.viewmodel.ext.android.viewModel
import pronenko.eventcountdown.R
import pronenko.eventcountdown.domain.NotificationWorker
import pronenko.eventcountdown.domain.getColorByNumber
import pronenko.eventcountdown.domain.getIconByNumber
import pronenko.eventcountdown.domain.getNotificationDelay
import pronenko.eventcountdown.domain.numberTo2Digits
import pronenko.eventcountdown.ui.info.InfoFragment
import pronenko.eventcountdown.ui.main.ButtonOrange
import pronenko.eventcountdown.ui.main.MainFragment
import pronenko.eventcountdown.ui.main.PERMISSIONS
import pronenko.eventcountdown.ui.main.Title
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val EVENT_NAME = "name"

private const val TAG = "IdCheck"

class EditorFragment : Fragment() {

    private val viewModel by viewModel<EditorViewModel>()

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
            val nameState = viewModel.nameStateFlow.collectAsState()
            val dateState = viewModel.dateStateFlow.collectAsState()
            val reminderTypeState = viewModel.reminderTypeStateFlow.collectAsState()
            val iconNumberState = viewModel.iconNumberStateFlow.collectAsState()
            val colorNumberState = viewModel.colorNumberStateFlow.collectAsState()
            Editor(
                context = requireContext(),
                name = nameState.value,
                saveName = { name -> viewModel.saveName(name) },
                date = dateState.value,
                saveDate = { date -> viewModel.saveDate(date) },
                reminderType = reminderTypeState.value,
                saveReminderType = { reminderType -> viewModel.saveReminderType(reminderType) },
                iconNumber = iconNumberState.value,
                saveIconNumber = { iconNumber -> viewModel.saveIconNumber(iconNumber) },
                colorNumber = colorNumberState.value,
                saveColorNumber = { colorNumber -> viewModel.saveColorNumber(colorNumber) },
                saveEvent = {
                    viewModel.saveEvent(requireContext())
                    val notificationDelay = getNotificationDelay(
                        eventDate = dateState.value,
                        reminderType = reminderTypeState.value
                    )
                    val uniqueId1 = viewModel.getUniqueId()
                    if (notificationDelay[0] > 0) {
                        val inputData = Data.Builder()
                            .putString("title", nameState.value)
                            .putString(
                                "message",
                                if (reminderTypeState.value) getString(R.string.in_1_week)
                                else getString(R.string.in_1_day)
                            )
                            .putInt("id", uniqueId1)
                            .build()
                        val alarmWorkRequest =
                            OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                                .addTag("${nameState.value}_notification")
                                .setInputData(inputData)
                                .setInitialDelay(notificationDelay[0], TimeUnit.MILLISECONDS)
                                .build()
                        WorkManager.getInstance(requireContext()).enqueue(alarmWorkRequest)
                    }
                    val uniqueId2 = viewModel.getUniqueId()
                    if (notificationDelay[1] > 0) {
                        val inputData = Data.Builder()
                            .putString("title", nameState.value)
                            .putString(
                                "message",
                                getString(R.string.event_arrived)
                            )
                            .putInt("id", uniqueId2)
                            .build()
                        val alarmWorkRequest =
                            OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                                .addTag("${nameState.value}_event")
                                .setInputData(inputData)
                                .setInitialDelay(notificationDelay[1], TimeUnit.MILLISECONDS)
                                .build()
                        WorkManager.getInstance(requireContext()).enqueue(alarmWorkRequest)
                    }
                    Log.d(TAG, "uniqueId1 = $uniqueId1, uniqueId2 = $uniqueId2")
                    val infoFragment = InfoFragment()
                    val bundle = Bundle()
                    bundle.putString(EVENT_NAME, nameState.value)
                    bundle.putBoolean(PERMISSIONS, viewModel.permissionsGranted)
                    infoFragment.arguments = bundle
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.container, infoFragment)
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
                },
                permissionsGranted = viewModel.permissionsGranted
            )
        }
        return view
    }
}

@Composable
fun Editor(
    context: Context,
    permissionsGranted: Boolean,
    name: String,
    saveName: (String) -> Unit,
    date: String,
    saveDate: (String) -> Unit,
    reminderType: Boolean,
    saveReminderType: (Boolean) -> Unit,
    iconNumber: Int,
    saveIconNumber: (Int) -> Unit,
    colorNumber: Int,
    saveColorNumber: (Int) -> Unit,
    saveEvent: (String) -> Unit,
    back: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.grey_background))
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 226.dp)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            Title(title = stringResource(id = R.string.add_edit_event))
            NameEditField(
                topPadding = 30.dp,
                name = name,
                saveName = saveName
            )
            TitleOrange(
                text = stringResource(id = R.string.event_icon),
                topPadding = 25.dp,
                startPadding = 32.dp,
                endPadding = 32.dp
            )
            ImageGrid(iconNumber = iconNumber, saveIconNumber = saveIconNumber)
            DateEditField(
                context = context,
                topPadding = 25.dp,
                date = date,
                saveDate = saveDate
            )
            TitleOrange(
                text = stringResource(id = R.string.reminder),
                topPadding = 28.dp,
                startPadding = 32.dp,
                endPadding = 32.dp
            )
            ReminderTypeSelector(
                reminderType = reminderType,
                saveReminderType = saveReminderType
            )
            TitleOrange(
                text = stringResource(id = R.string.color),
                topPadding = 35.dp,
                startPadding = 32.dp,
                endPadding = 32.dp
            )
            ColorGrid(
                colorNumber = colorNumber,
                saveColorNumber = saveColorNumber
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            ButtonOrange(
                buttonText = stringResource(id = R.string.save),
                paddingTop = 19.dp,
                paddingBottom = 0.dp,
                eventName = name,
                onClick = saveEvent,
                enabled = permissionsGranted && name.isNotBlank() && date.isNotBlank()
            )
            ButtonWhiteBack(
                paddingBottom = 48.dp,
                onClick = back
            )
        }
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
fun NameEditField(
    topPadding: Dp,
    name: String?,
    saveName: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = topPadding)
    ) {
        TitleOrange(text = stringResource(id = R.string.event_name))
        TextField(
            value = name ?: "",
            onValueChange = { newText -> saveName(newText) },
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .height(56.dp)
                .border(
                    BorderStroke(1.dp, colorResource(id = R.color.orange)),
                    RoundedCornerShape(12.dp)
                ),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 17.sp, // Задаем размер шрифта
                fontFamily = FontFamily(Font(R.font.poppins_medium_500)), // Задаем тип шрифта
                color = colorResource(id = R.color.black) // Задаем цвет шрифта
            ),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = colorResource(id = R.color.transparent),
                unfocusedIndicatorColor = colorResource(id = R.color.transparent),
                focusedContainerColor = colorResource(id = R.color.transparent),
                unfocusedContainerColor = colorResource(id = R.color.transparent),
                focusedTextColor = colorResource(id = R.color.black),
                unfocusedTextColor = colorResource(id = R.color.black),
                disabledTextColor = colorResource(id = R.color.black),
                errorTextColor = colorResource(id = R.color.black),
                focusedLabelColor = colorResource(id = R.color.orange),
                unfocusedLabelColor = colorResource(id = R.color.orange),
                disabledLabelColor = colorResource(id = R.color.orange),
                errorLabelColor = colorResource(id = R.color.orange),
                cursorColor = colorResource(id = R.color.black)
            )
        )
    }
}

@Composable
fun TitleOrange(
    text: String,
    topPadding: Dp = 0.dp,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp
) {
    Text(
        text = text,
        modifier = Modifier.padding(top = topPadding, start = startPadding, end = endPadding),
        fontFamily = FontFamily(Font(R.font.poppins_medium_500)),
        textAlign = TextAlign.Start,
        fontSize = 17.sp,
        color = colorResource(id = R.color.orange)
    )
}

@Composable
fun DateEditField(
    context: Context,
    topPadding: Dp,
    date: String?,
    saveDate: (String) -> Unit
) {
    val selectedDate = remember { mutableStateOf(date ?: "") }

    Column(
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = topPadding)
    ) {
        TitleOrange(text = stringResource(id = R.string.event_start_date))
        Text(
            text = selectedDate.value,
            modifier = Modifier
                .padding(top = 4.dp)
                .height(56.dp)
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, colorResource(id = R.color.orange)),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
                .clickable {
                    val calendar = Calendar.getInstance()
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)

                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    val minDate = calendar.timeInMillis

                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, selectedYear, selectedMonth, selectedDay ->
                            selectedDate.value = "${numberTo2Digits(selectedDay)}.${numberTo2Digits(selectedMonth + 1)}.$selectedYear"
                            saveDate(selectedDate.value)
                        }, year, month, day
                    ).apply {
                        datePicker.minDate = minDate
                    }
                    datePickerDialog.show()
                }
            ,
            fontFamily = FontFamily(Font(R.font.poppins_medium_500)),
            fontSize = 17.sp,
            color = colorResource(id = R.color.black)
        )
    }
}

@Composable
fun ImageGrid(
    iconNumber: Int,
    saveIconNumber: (Int) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 13.dp)
            .height(98.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(12) { index ->
            Image(
                painter = getIconByNumber(index),
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer(alpha = if (iconNumber == index) 0.2f else 1f)
                    .clickable {
                        saveIconNumber(index)
                    },
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ReminderTypeSelector(
    reminderType: Boolean,
    saveReminderType: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 32.dp, end = 36.dp, top = 15.dp)
            .height(156.dp)
            .background(
                color = colorResource(id = R.color.white),
                shape = RoundedCornerShape(20.dp)
            )
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .height(78.dp)
                .fillMaxWidth()
                .clickable { saveReminderType(false) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReminderTypeCheckbox(state = !reminderType)
            ReminderTypeText(text = stringResource(id = R.string.day_before))
        }
        Divider(
            modifier = Modifier
                .padding(start = 37.dp, end = 37.dp)
                .alpha(0.3f),
            thickness = 0.5.dp,
            color = colorResource(id = R.color.black)
        )
        Row(
            modifier = Modifier
                .height(78.dp)
                .fillMaxWidth()
                .clickable { saveReminderType(true) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReminderTypeCheckbox(state = reminderType)
            ReminderTypeText(text = stringResource(id = R.string.week_before))
        }
    }
}

@Composable
fun ReminderTypeCheckbox(state: Boolean) {
    Box(
        modifier = Modifier
            .padding(start = 21.dp)
            .size(15.dp)
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.orange),
                shape = RoundedCornerShape(7.5.dp)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (state)
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = colorResource(id = R.color.orange),
                        shape = RoundedCornerShape(3.5.dp)
                    )
            )
    }
}

@Composable
fun ReminderTypeText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(start = 17.dp, end = 17.dp),
        fontFamily = FontFamily(Font(R.font.poppins_regular_400)),
        fontSize = 17.sp,
        color = colorResource(id = R.color.black)
    )
}

@Composable
fun ColorGrid(
    colorNumber: Int,
    saveColorNumber: (Int) -> Unit
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 32.dp, end = 32.dp, top = 13.dp, bottom = 32.dp)
            .height(99.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(19.dp)
    ) {
        items(12) { index ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = getColorByNumber(number = index))
                    .clickable {
                        saveColorNumber(index)
                    },
            ) {
                if (index == colorNumber) {
                    Image(
                        painter = painterResource(id = R.drawable.chosen),
                        contentDescription = null,
                        modifier = Modifier.padding(start = 17.dp, top = 5.dp),
                        colorFilter = ColorFilter.tint(
                            when (index) {
                                3, 5, 11 -> colorResource(id = R.color.grey_text)
                                else -> colorResource(id = R.color.grey_dark)
                            }
                        ),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}