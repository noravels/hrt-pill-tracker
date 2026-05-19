package app.transition.hrt.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TransBlue = Color(0xFF5BCEFA)
private val TransPink = Color(0xFFF5A9B8)
private val TransWhite = Color(0xFFFFFFFF)
private val Ink = Color(0xFF15171D)
private val Paper = Color(0xFFFFFDFB)
private val Muted = Color(0xFF5D6068)
private val SoftBlue = Color(0xFFDFF6FF)
private val SoftPink = Color(0xFFFFE6EC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HrtTrackerApp() }
    }
}

private enum class AppScreen { Today, Routine, Setup }

@Composable
fun HrtTrackerApp() {
    MaterialTheme {
        var screen by remember { mutableStateOf(AppScreen.Today) }
        var taken by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Surface(color = Paper, modifier = Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxSize()) {
                SideIdentityRail(color = TransBlue)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    StatusRow()
                    when (screen) {
                        AppScreen.Today -> TodayScreen(
                            taken = taken,
                            onMarkTaken = {
                                taken = true
                                Toast.makeText(context, context.getString(R.string.taken_toast), Toast.LENGTH_SHORT).show()
                            },
                            onSetup = { screen = AppScreen.Setup },
                            onRoutine = { screen = AppScreen.Routine },
                        )

                        AppScreen.Setup -> SetupScreen(onBack = { screen = AppScreen.Today })
                        AppScreen.Routine -> RoutineScreen(onBack = { screen = AppScreen.Today })
                    }
                    BottomNav(selected = screen, onSelect = { screen = it })
                }
                SideIdentityRail(color = TransPink)
            }
        }
    }
}

@Composable
private fun TodayScreen(
    taken: Boolean,
    onMarkTaken: () -> Unit,
    onSetup: () -> Unit,
    onRoutine: () -> Unit,
) {
    Header(
        title = stringResource(R.string.today_title),
        subtitle = stringResource(R.string.routine_subtitle),
        action = stringResource(R.string.setup_short),
        onAction = onSetup,
    )
    DoseCard(taken = taken, onMarkTaken = onMarkTaken)
    InfoCard(title = stringResource(R.string.active_routine_title), tint = SoftBlue, action = stringResource(R.string.edit_action), onAction = onSetup) {
        RoutineRow(time = "09/21", title = stringResource(R.string.estradiol_name), detail = stringResource(R.string.fixed_anchor_schedule), badge = "2×", badgeColor = TransBlue)
        RoutineRow(time = stringResource(R.string.weekday_friday), title = stringResource(R.string.blocker_name), detail = stringResource(R.string.optional_hidden), badge = stringResource(R.string.badge_optional), badgeColor = TransPink)
    }
    InfoCard(title = stringResource(R.string.next_24_hours_title), tint = SoftPink, action = stringResource(R.string.all_action), onAction = onRoutine) {
        RoutineRow(time = stringResource(R.string.dose_time), title = stringResource(R.string.take_window_title), detail = stringResource(R.string.no_guilt_copy), badge = stringResource(R.string.badge_now), badgeColor = TransBlue)
        RoutineRow(time = "09:00", title = stringResource(R.string.tomorrow_title), detail = stringResource(R.string.next_anchor_copy), badge = stringResource(R.string.badge_next), badgeColor = TransWhite)
    }
}

@Composable
private fun SetupScreen(onBack: () -> Unit) {
    Header(title = stringResource(R.string.setup_title), subtitle = stringResource(R.string.local_only_label), action = stringResource(R.string.back_action), onAction = onBack)
    FormField(label = stringResource(R.string.medication_label), value = stringResource(R.string.estradiol_name), tint = SoftBlue)
    FormField(label = stringResource(R.string.dose_label), value = stringResource(R.string.estradiol_dose), tint = SoftPink)
    FormField(label = stringResource(R.string.interval_label), value = stringResource(R.string.interval_value), tint = Paper)
    FormField(label = stringResource(R.string.privacy_label), value = stringResource(R.string.privacy_value), tint = SoftBlue)
    BlockButton(text = stringResource(R.string.save_local_routine), color = TransPink, onClick = onBack)
}

@Composable
private fun RoutineScreen(onBack: () -> Unit) {
    Header(title = stringResource(R.string.routine_title), subtitle = stringResource(R.string.routine_subtitle_week), action = stringResource(R.string.back_action), onAction = onBack)
    InfoCard(title = stringResource(R.string.fixed_anchors_title), tint = SoftBlue, action = stringResource(R.string.no_streaks), onAction = {}) {
        RoutineRow(time = stringResource(R.string.daily_label), title = "09:00 · 21:00", detail = stringResource(R.string.estradiol_reminders), badge = stringResource(R.string.badge_on), badgeColor = TransBlue)
        RoutineRow(time = stringResource(R.string.weekday_friday), title = stringResource(R.string.dose_time), detail = stringResource(R.string.optional_blocker_reminder), badge = stringResource(R.string.badge_optional), badgeColor = TransPink)
        RoutineRow(time = stringResource(R.string.any_time_label), title = stringResource(R.string.missed_window_title), detail = stringResource(R.string.missed_window_copy), badge = stringResource(R.string.badge_log), badgeColor = TransWhite)
    }
    BlockButton(text = stringResource(R.string.today_tab), color = TransBlue, onClick = onBack)
}

@Composable
private fun StatusRow() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.app_name), color = Ink, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Text(stringResource(R.string.local_only_label), color = Ink, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
private fun Header(title: String, subtitle: String, action: String, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black, lineHeight = 30.sp)
            Text(subtitle, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        BlockButton(text = action, color = TransWhite, compact = true, onClick = onAction)
    }
}

@Composable
private fun DoseCard(taken: Boolean, onMarkTaken: () -> Unit) {
    StructuralCard(background = TransWhite, modifier = Modifier.fillMaxWidth()) {
        Row {
            DoseIdentityRail()
            Column(Modifier.padding(18.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelText(stringResource(R.string.next_dose_label))
                Text(stringResource(R.string.dose_time), color = Ink, fontSize = 80.sp, lineHeight = 74.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.estradiol_dose), color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(stringResource(R.string.dose_details), color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                BlockButton(
                    text = if (taken) stringResource(R.string.taken_state) else stringResource(R.string.mark_dose_taken),
                    color = TransBlue,
                    onClick = onMarkTaken,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, tint: Color, action: String, onAction: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    StructuralCard(background = tint, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Black)
                TextButtonLabel(text = action, onClick = onAction)
            }
            content()
        }
    }
}

@Composable
private fun FormField(label: String, value: String, tint: Color) {
    InfoCard(title = label, tint = tint, action = "", onAction = {}) {
        Box(Modifier.fillMaxWidth().border(2.dp, Ink, RoundedCornerShape(17.dp)).background(Paper, RoundedCornerShape(17.dp)).padding(15.dp)) {
            Text(value, color = Ink, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun RoutineRow(time: String, title: String, detail: String, badge: String, badgeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Ink, RoundedCornerShape(17.dp))
            .background(Paper, RoundedCornerShape(17.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(time, modifier = Modifier.width(56.dp), color = Ink, fontWeight = FontWeight.Black, fontSize = 13.sp)
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.Black, fontSize = 14.sp)
            Text(detail, color = Muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Badge(text = badge, color = badgeColor)
    }
}

@Composable
private fun BottomNav(selected: AppScreen, onSelect: (AppScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(3.dp, Ink, RoundedCornerShape(24.dp))
            .background(Paper, RoundedCornerShape(24.dp))
            .padding(7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        NavItem(text = stringResource(R.string.today_tab), active = selected == AppScreen.Today, onClick = { onSelect(AppScreen.Today) }, modifier = Modifier.weight(1f))
        NavItem(text = stringResource(R.string.routine_tab), active = selected == AppScreen.Routine, onClick = { onSelect(AppScreen.Routine) }, modifier = Modifier.weight(1f))
        NavItem(text = stringResource(R.string.history_tab), active = false, onClick = {}, modifier = Modifier.weight(1f))
        NavItem(text = stringResource(R.string.setup_tab), active = selected == AppScreen.Setup, onClick = { onSelect(AppScreen.Setup) }, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NavItem(text: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (active) TransBlue else Color.Transparent, contentColor = if (active) Ink else Muted),
        elevation = null,
    ) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.Black) }
}

@Composable
private fun BlockButton(text: String, color: Color, compact: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 16.dp else 19.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Ink),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        modifier = if (compact) Modifier.size(width = 58.dp, height = 48.dp) else Modifier.fillMaxWidth().height(58.dp),
    ) { Text(text, fontWeight = FontWeight.Black, fontSize = if (compact) 12.sp else 16.sp) }
}

@Composable
private fun StructuralCard(background: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .shadow(0.dp, RoundedCornerShape(30.dp))
            .border(3.dp, Ink, RoundedCornerShape(30.dp))
            .background(background, RoundedCornerShape(30.dp)),
    ) { content() }
}

@Composable
private fun DoseIdentityRail() {
    Column(
        modifier = Modifier
            .width(34.dp)
            .height(272.dp)
            .border(0.dp, Ink)
    ) {
        listOf(TransBlue, TransPink, TransWhite, TransPink, TransBlue).forEach { color ->
            Box(Modifier.weight(1f).fillMaxWidth().background(color))
        }
    }
}

@Composable
private fun SideIdentityRail(color: Color) {
    Box(Modifier.width(18.dp).fillMaxHeight().background(color))
}

@Composable
private fun LabelText(text: String) {
    Text(text.uppercase(), color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.6.sp)
}

@Composable
private fun TextButtonLabel(text: String, onClick: () -> Unit) {
    if (text.isBlank()) return
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Ink),
        elevation = null,
    ) { Text(text, fontWeight = FontWeight.Black, fontSize = 12.sp) }
}

@Composable
private fun Badge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .border(2.dp, Ink, RoundedCornerShape(999.dp))
            .background(color, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Ink, fontWeight = FontWeight.Black, fontSize = 12.sp)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HrtTrackerAppPreview() {
    HrtTrackerApp()
}
