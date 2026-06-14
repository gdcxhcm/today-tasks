package com.gdc.todaytasks.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.net.toUri
import com.gdc.todaytasks.data.HistoryGroupEntity
import com.gdc.todaytasks.data.RecurrenceDraft
import com.gdc.todaytasks.data.RecurrenceKind
import com.gdc.todaytasks.data.RecurrenceRules
import com.gdc.todaytasks.data.RecurrenceTemplateEntity
import com.gdc.todaytasks.data.TaskDraft
import com.gdc.todaytasks.data.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private enum class Destination(val title: String) { TODAY("今天"), FUTURE("未来"), HISTORY("历史") }

private val FitnessOptions = listOf("练胸", "练背", "练肩", "练腿", "练腹")

private val MinimalBackground = Color(0xFFF7F4EF)
private val MinimalSurface = Color.White
private val MinimalSurfaceSoft = Color(0xFFF1EEE8)
private val MinimalInk = Color(0xFF171717)
private val MinimalMuted = Color(0xFF666666)
private val MinimalTertiary = Color(0xFF9A9A9A)
private val MinimalLine = Color(0xFFE7E2DA)
private val MinimalSage = Color(0xFF7C8B7A)
private val MinimalTaupe = Color(0xFFA28F7A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayTasksApp(
    viewModel: TaskViewModel,
    editorRequest: Int = 0,
    onPinWidget: () -> Unit = {},
    onPickBackground: () -> Unit = {}
) {
    val today by viewModel.today.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val futureTasks by viewModel.futureTasks.collectAsStateWithLifecycle()
    val historyTasks by viewModel.historyTasks.collectAsStateWithLifecycle()
    val historyGroups by viewModel.historyGroups.collectAsStateWithLifecycle()
    val incompleteCount by viewModel.incompleteCount.collectAsStateWithLifecycle()
    val backgroundUri by viewModel.backgroundUri.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var destination by remember { mutableStateOf(Destination.TODAY) }
    var editorTask by remember { mutableStateOf<TaskEntity?>(null) }
    var editorTemplate by remember { mutableStateOf<RecurrenceTemplateEntity?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showGroupEditor by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { task ->
            snackbarHost.currentSnackbarData?.dismiss()
            if (snackbarHost.showSnackbar(
                    message = "已删除“${task.title}”",
                    actionLabel = "撤销",
                    duration = SnackbarDuration.Short
                ) == androidx.compose.material3.SnackbarResult.ActionPerformed
            ) {
                viewModel.undoDelete(task)
            }
        }
    }

    fun openNew() {
        editorTask = null
        editorTemplate = null
        showEditor = true
    }

    LaunchedEffect(editorRequest) {
        if (editorRequest > 0) openNew()
    }

    Box(Modifier.fillMaxSize().background(MinimalBackground)) {
        BackgroundImage(backgroundUri)
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHost) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "今日事项",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MinimalInk,
                        actionIconContentColor = MinimalInk
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MinimalSurface.copy(alpha = 0.94f),
                    tonalElevation = 0.dp
                ) {
                    Destination.entries.forEach { item ->
                        val icon = when (item) {
                            Destination.TODAY -> Icons.Default.CheckCircle
                            Destination.FUTURE -> Icons.Default.Event
                            Destination.HISTORY -> Icons.Default.History
                        }
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = { Icon(icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MinimalInk,
                                selectedTextColor = MinimalInk,
                                indicatorColor = MinimalSurfaceSoft,
                                unselectedIconColor = MinimalTertiary,
                                unselectedTextColor = MinimalTertiary
                            )
                        )
                    }
                }
            },
            floatingActionButton = {
                if (destination != Destination.HISTORY) {
                    ExtendedFloatingActionButton(
                        onClick = ::openNew,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("添加") },
                        containerColor = MinimalInk,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        ) { padding ->
            when (destination) {
                Destination.TODAY -> TodayScreen(
                    modifier = Modifier.padding(padding),
                    today = today,
                    tasks = todayTasks,
                    incompleteCount = incompleteCount,
                    onPinWidget = onPinWidget,
                    onToggle = viewModel::toggleComplete,
                    onStar = viewModel::toggleStar,
                    onDelete = viewModel::delete,
                    onEdit = { task ->
                        scope.launch {
                            editorTask = task
                            editorTemplate = viewModel.templateFor(task)
                            showEditor = true
                        }
                    },
                    onMove = viewModel::moveTask
                )

                Destination.FUTURE -> FutureScreen(
                    modifier = Modifier.padding(padding),
                    tasks = futureTasks,
                    onToggle = viewModel::toggleComplete,
                    onStar = viewModel::toggleStar,
                    onDelete = viewModel::delete,
                    onEdit = { task ->
                        scope.launch {
                            editorTask = task
                            editorTemplate = viewModel.templateFor(task)
                            showEditor = true
                        }
                    }
                )

                Destination.HISTORY -> HistoryScreen(
                    modifier = Modifier.padding(padding),
                    tasks = historyTasks,
                    groups = historyGroups,
                    onAddGroup = { showGroupEditor = true },
                    onCreateGroupFromTask = { task ->
                        viewModel.createHistoryGroup(task.title, task.title)
                    },
                    onAssignGroup = viewModel::assignToHistoryGroup
                )
            }
        }
    }

    if (showEditor) {
        TaskEditorDialog(
            existing = editorTask,
            template = editorTemplate,
            defaultDate = today,
            onDismiss = { showEditor = false },
            onStopRepeating = { editorTask?.let(viewModel::stopRepeating); showEditor = false },
            onSave = { draft ->
                viewModel.save(draft, editorTask)
                showEditor = false
            }
        )
    }

    if (showSettings) {
        SettingsDialog(
            hasBackground = backgroundUri != null,
            onPickBackground = onPickBackground,
            onClearBackground = { viewModel.setBackgroundUri(null) },
            onDismiss = { showSettings = false }
        )
    }

    if (showGroupEditor) {
        HistoryGroupEditorDialog(
            completedTitles = historyTasks.map { it.title }.distinct().sorted(),
            onDismiss = { showGroupEditor = false },
            onSave = { name, matchTitle ->
                viewModel.createHistoryGroup(name, matchTitle)
                showGroupEditor = false
            }
        )
    }
}

@Composable
private fun BackgroundImage(uriString: String?) {
    if (uriString == null) return
    val context = LocalContext.current
    var image by remember(uriString) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(uriString) {
        image = withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openInputStream(uriString.toUri())?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    image?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.14f
        )
        Box(Modifier.fillMaxSize().background(MinimalBackground.copy(alpha = 0.82f)))
    }
}

@Composable
private fun TodayScreen(
    modifier: Modifier,
    today: LocalDate,
    tasks: List<TaskEntity>,
    incompleteCount: Int,
    onPinWidget: () -> Unit,
    onToggle: (TaskEntity) -> Unit,
    onStar: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onMove: (TaskEntity, Int) -> Unit
) {
    val completed = tasks.count { it.isCompleted }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp, 18.dp, 24.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeaderCard(today, incompleteCount, completed, tasks.size, onPinWidget)
            Spacer(Modifier.height(28.dp))
            SectionHeader("待完成", "${tasks.count { !it.isCompleted }} 项")
        }
        if (tasks.isEmpty()) {
            item { EmptyMessage("今天还没有事项。留白也很好，想起什么再写。") }
        }
        items(tasks, key = { it.id }) { task ->
            TaskCard(task, onToggle, onStar, onDelete, onEdit, onMove)
        }
    }
}

@Composable
private fun HeaderCard(today: LocalDate, incomplete: Int, completed: Int, total: Int, onPinWidget: () -> Unit) {
    val progress = if (total == 0) 1f else completed.toFloat() / total.toFloat()
    MinimalCard {
        Column(
            Modifier.fillMaxWidth().padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                today.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.bodyMedium,
                color = MinimalMuted
            )
            Text(
                if (incomplete == 0) "今天完成啦" else "今天还有 $incomplete 件事",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MinimalInk
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = MinimalInk,
                    trackColor = MinimalSurfaceSoft
                )
                Text("已完成 $completed / $total", style = MaterialTheme.typography.bodySmall, color = MinimalTertiary)
            }
            TextButton(
                onClick = onPinWidget,
                colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk),
                shape = RoundedCornerShape(999.dp)
            ) {
                Icon(Icons.Default.Widgets, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("放到桌面")
            }
        }
    }
}

@Composable
private fun FutureScreen(
    modifier: Modifier,
    tasks: List<TaskEntity>,
    onToggle: (TaskEntity) -> Unit,
    onStar: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit
) {
    val groups = tasks.groupBy { it.scheduledDate }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PageTitle("未来", "安静地安排之后的事")
        }
        if (tasks.isEmpty()) item { EmptyMessage("未来没有安排。") }
        groups.forEach { (date, group) ->
            item { SectionTitle(LocalDate.parse(date).displayDate()) }
            items(group, key = { it.id }) { task ->
                TaskCard(task, onToggle, onStar, onDelete, onEdit, null)
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    modifier: Modifier,
    tasks: List<TaskEntity>,
    groups: List<HistoryGroupEntity>,
    onAddGroup: () -> Unit,
    onCreateGroupFromTask: (TaskEntity) -> Unit,
    onAssignGroup: (TaskEntity, Long) -> Unit
) {
    val dateGroups = tasks.groupBy {
        it.completedAt?.let { time -> Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate() }
            ?: LocalDate.parse(it.scheduledDate)
    }
    var assigningTask by remember { mutableStateOf<TaskEntity?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 34.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PageTitle("历史", "完成过的事都会留下痕迹", Modifier.weight(1f))
                TextButton(onClick = onAddGroup, colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)) {
                    Icon(Icons.Default.Folder, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("新建")
                }
            }
        }
        item { HistoryGroupsPanel(groups, tasks) }
        if (tasks.isEmpty()) item { EmptyMessage("完成事项后，这里会保留你的记录。") }
        dateGroups.forEach { (date, group) ->
            item { SectionTitle(date.displayDate()) }
            items(group, key = { it.id }) { task ->
                HistoryTaskCard(
                    task = task,
                    groupName = groups.firstOrNull { it.id == task.historyGroupId }?.name,
                    hasGroups = groups.isNotEmpty(),
                    onCreateGroup = { onCreateGroupFromTask(task) },
                    onChooseGroup = { assigningTask = task }
                )
            }
        }
    }

    assigningTask?.let { task ->
        AssignGroupDialog(
            task = task,
            groups = groups,
            onDismiss = { assigningTask = null },
            onAssign = { groupId ->
                onAssignGroup(task, groupId)
                assigningTask = null
            }
        )
    }
}

@Composable
private fun HistoryGroupsPanel(groups: List<HistoryGroupEntity>, tasks: List<TaskEntity>) {
    MinimalCard(containerColor = MinimalSurfaceSoft) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader("分组", if (groups.isEmpty()) "未创建" else "${groups.size} 个")
            if (groups.isEmpty()) {
                Text(
                    "可以把“练胸”这类完成记录建成分组，以后同名事项完成后会自动归入。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MinimalMuted
                )
            } else {
                groups.forEach { group ->
                    val count = tasks.count { it.historyGroupId == group.id }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(group.name, Modifier.weight(1f), fontWeight = FontWeight.Medium, color = MinimalInk)
                        Text("$count 条", color = MinimalMuted)
                    }
                    Text("匹配：${group.matchTitle}", style = MaterialTheme.typography.bodySmall, color = MinimalTertiary)
                }
            }
        }
    }
}

@Composable
private fun HistoryTaskCard(
    task: TaskEntity,
    groupName: String?,
    hasGroups: Boolean,
    onCreateGroup: () -> Unit,
    onChooseGroup: () -> Unit
) {
    MinimalCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MinimalSage, modifier = Modifier.size(22.dp))
                Text(
                    task.title,
                    Modifier.padding(start = 12.dp).weight(1f),
                    textDecoration = TextDecoration.LineThrough,
                    color = MinimalMuted
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (groupName != null) {
                    MinimalAssistChip("分组：$groupName", onChooseGroup)
                } else if (hasGroups) {
                    MinimalAssistChip("选择分组", onChooseGroup)
                }
                MinimalAssistChip("按此名称建组", onCreateGroup)
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onToggle: (TaskEntity) -> Unit,
    onStar: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onMove: ((TaskEntity, Int) -> Unit)?
) {
    var dragAmount by remember(task.id) { mutableFloatStateOf(0f) }
    val dragModifier = if (onMove != null && !task.isCompleted) {
        Modifier.pointerInput(task.id) {
            detectDragGesturesAfterLongPress(
                onDragEnd = { dragAmount = 0f },
                onDragCancel = { dragAmount = 0f },
                onDrag = { change, distance ->
                    change.consume()
                    dragAmount += distance.y
                    if (dragAmount > 44f) {
                        onMove(task, 1)
                        dragAmount = 0f
                    } else if (dragAmount < -44f) {
                        onMove(task, -1)
                        dragAmount = 0f
                    }
                }
            )
        }
    } else Modifier

    MinimalCard(modifier = Modifier.fillMaxWidth().then(dragModifier)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) },
                colors = CheckboxDefaults.colors(
                    checkedColor = MinimalInk,
                    uncheckedColor = MinimalTertiary,
                    checkmarkColor = Color.White
                )
            )
            Column(Modifier.weight(1f).padding(start = 2.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (task.isStarred) FontWeight.SemiBold else FontWeight.Normal,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) MinimalTertiary else MinimalInk
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (task.isCarried) Label("已延续", MinimalTaupe)
                    if (task.recurrenceTemplateId != null) Label("重复", MinimalSage)
                }
            }
            IconButton(onClick = { onStar(task) }) {
                Icon(
                    if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "星标",
                    tint = if (task.isStarred) MinimalTaupe else MinimalTertiary
                )
            }
            IconButton(onClick = { onEdit(task) }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MinimalMuted)
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "删除", tint = MinimalMuted)
            }
            if (onMove != null && !task.isCompleted) {
                Icon(Icons.Default.DragHandle, contentDescription = "长按拖动", tint = MinimalTertiary)
            }
        }
    }
}

@Composable
private fun Label(text: String, color: Color) {
    Text(
        text,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorDialog(
    existing: TaskEntity?,
    template: RecurrenceTemplateEntity?,
    defaultDate: LocalDate,
    onDismiss: () -> Unit,
    onStopRepeating: () -> Unit,
    onSave: (TaskDraft) -> Unit
) {
    var title by remember(existing?.id) { mutableStateOf(existing?.title.orEmpty()) }
    var date by remember(existing?.id) { mutableStateOf(existing?.scheduledDate?.let(LocalDate::parse) ?: defaultDate) }
    var starred by remember(existing?.id) { mutableStateOf(existing?.isStarred ?: false) }
    var repeats by remember(existing?.id, template?.id) { mutableStateOf(template != null) }
    var kind by remember(template?.id) {
        mutableStateOf(template?.kind?.let(RecurrenceKind::valueOf) ?: RecurrenceKind.FIXED_DAILY)
    }
    var interval by remember(template?.id) { mutableIntStateOf(template?.interval ?: 1) }
    var weekdays by remember(template?.id) {
        mutableStateOf(template?.weekdays?.let(RecurrenceRules::textToWeekdays) ?: setOf(date.dayOfWeek))
    }
    var choosingDate by remember { mutableStateOf(false) }
    var fitnessMode by remember(existing?.id) { mutableStateOf(existing?.title in FitnessOptions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MinimalSurface,
        shape = RoundedCornerShape(28.dp),
        title = { Text(if (existing == null) "添加事项" else "编辑事项", fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("事项标题") },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp)
                    )
                }
                item {
                    MinimalFilterChip(
                        selected = fitnessMode,
                        onClick = { fitnessMode = !fitnessMode },
                        text = "健身事项",
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
                if (fitnessMode) {
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FitnessOptions.forEach { option ->
                                MinimalFilterChip(
                                    selected = title == option,
                                    onClick = { title = option },
                                    text = option
                                )
                            }
                        }
                    }
                }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MinimalAssistChip(date.displayDate()) { choosingDate = true }
                        MinimalFilterChip(selected = starred, onClick = { starred = !starred }, text = "重要")
                    }
                }
                item { HorizontalDivider(color = MinimalLine) }
                item {
                    MinimalFilterChip(
                        selected = repeats,
                        onClick = { repeats = !repeats },
                        text = "重复事项",
                        icon = { Icon(Icons.Default.Repeat, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
                if (repeats) {
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                RecurrenceKind.FIXED_DAILY to "每天",
                                RecurrenceKind.FIXED_WEEKLY to "每周",
                                RecurrenceKind.ROLLING_DAYS to "完成后 N 天",
                                RecurrenceKind.ROLLING_WEEKS to "完成后 N 周"
                            ).forEach { (option, label) ->
                                MinimalFilterChip(selected = kind == option, onClick = { kind = option }, text = label)
                            }
                        }
                    }
                    if (kind == RecurrenceKind.FIXED_WEEKLY) {
                        item {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                DayOfWeek.entries.forEach { day ->
                                    MinimalFilterChip(
                                        selected = day in weekdays,
                                        onClick = {
                                            weekdays = if (day in weekdays && weekdays.size > 1) weekdays - day else weekdays + day
                                        },
                                        text = day.shortName()
                                    )
                                }
                            }
                        }
                    }
                    if (kind == RecurrenceKind.ROLLING_DAYS || kind == RecurrenceKind.ROLLING_WEEKS) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("间隔", color = MinimalMuted)
                                TextButton(onClick = { if (interval > 1) interval-- }, colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)) {
                                    Text("-")
                                }
                                Text("$interval", fontWeight = FontWeight.SemiBold, color = MinimalInk)
                                TextButton(onClick = { interval++ }, colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)) {
                                    Text("+")
                                }
                                Text(if (kind == RecurrenceKind.ROLLING_DAYS) "天" else "周", color = MinimalMuted)
                            }
                        }
                    }
                    if (existing != null && template != null) {
                        item {
                            TextButton(onClick = onStopRepeating, colors = ButtonDefaults.textButtonColors(contentColor = MinimalTaupe)) {
                                Text("停止以后重复")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    if (!repeats && existing != null && template != null) {
                        onStopRepeating()
                    }
                    onSave(
                        TaskDraft(
                            title = title.trim(),
                            date = date,
                            isStarred = starred,
                            recurrence = if (repeats) RecurrenceDraft(kind, interval, weekdays) else null
                        )
                    )
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MinimalMuted)) {
                Text("取消")
            }
        }
    )

    if (choosingDate) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { choosingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                    }
                    choosingDate = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { choosingDate = false }) { Text("取消") } }
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun SettingsDialog(
    hasBackground: Boolean,
    onPickBackground: () -> Unit,
    onClearBackground: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MinimalSurface,
        shape = RoundedCornerShape(28.dp),
        title = { Text("设置", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("外观", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Button(
                    onClick = onPickBackground,
                    colors = ButtonDefaults.buttonColors(containerColor = MinimalInk, contentColor = Color.White),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("从手机图片选择背景")
                }
                if (hasBackground) {
                    TextButton(onClick = onClearBackground, colors = ButtonDefaults.textButtonColors(contentColor = MinimalMuted)) {
                        Icon(Icons.Default.Close, contentDescription = null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("恢复默认背景")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)) { Text("完成") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HistoryGroupEditorDialog(
    completedTitles: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var matchTitle by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MinimalSurface,
        shape = RoundedCornerShape(28.dp),
        title = { Text("新建历史分组", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分组名称，例如：健身") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
                OutlinedTextField(
                    value = matchTitle,
                    onValueChange = { matchTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("匹配事项名称，例如：练胸") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
                if (completedTitles.isNotEmpty()) {
                    Text("从已完成事项选择", color = MinimalMuted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        completedTitles.take(12).forEach { title ->
                            MinimalAssistChip(title) {
                                matchTitle = title
                                if (name.isBlank()) name = title
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = matchTitle.isNotBlank(),
                onClick = { onSave(name.ifBlank { matchTitle }, matchTitle) },
                colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MinimalMuted)) { Text("取消") } }
    )
}

@Composable
private fun AssignGroupDialog(
    task: TaskEntity,
    groups: List<HistoryGroupEntity>,
    onDismiss: () -> Unit,
    onAssign: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MinimalSurface,
        shape = RoundedCornerShape(28.dp),
        title = { Text("选择分组", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("把“${task.title}”放入：", color = MinimalMuted)
                groups.forEach { group ->
                    MinimalAssistChip("${group.name}（匹配：${group.matchTitle}）") { onAssign(group.id) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = MinimalInk)) { Text("关闭") } }
    )
}

@Composable
private fun MinimalCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MinimalSurface,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MinimalLine),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun PageTitle(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = MinimalInk)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MinimalMuted)
    }
}

@Composable
private fun SectionHeader(title: String, meta: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MinimalInk)
        Text(meta, style = MaterialTheme.typography.bodySmall, color = MinimalTertiary)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        Modifier.padding(top = 16.dp, bottom = 2.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        color = MinimalMuted
    )
}

@Composable
private fun EmptyMessage(text: String) {
    MinimalCard(containerColor = MinimalSurface.copy(alpha = 0.72f)) {
        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Text(text, color = MinimalMuted)
        }
    }
}

@Composable
private fun MinimalAssistChip(text: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MinimalSurface,
            labelColor = MinimalMuted
        ),
        border = BorderStroke(1.dp, MinimalLine),
        shape = RoundedCornerShape(999.dp)
    )
}

@Composable
private fun MinimalFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: @Composable (() -> Unit)? = null
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = icon,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MinimalSurface,
            labelColor = MinimalMuted,
            iconColor = MinimalMuted,
            selectedContainerColor = MinimalInk,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MinimalLine,
            selectedBorderColor = MinimalInk,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        ),
        shape = RoundedCornerShape(999.dp)
    )
}

private fun LocalDate.displayDate(): String =
    format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.CHINA))

private fun DayOfWeek.shortName(): String = when (this) {
    DayOfWeek.MONDAY -> "一"
    DayOfWeek.TUESDAY -> "二"
    DayOfWeek.WEDNESDAY -> "三"
    DayOfWeek.THURSDAY -> "四"
    DayOfWeek.FRIDAY -> "五"
    DayOfWeek.SATURDAY -> "六"
    DayOfWeek.SUNDAY -> "日"
}
