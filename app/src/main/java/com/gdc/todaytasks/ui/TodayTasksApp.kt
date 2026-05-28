package com.gdc.todaytasks.ui

import android.graphics.BitmapFactory
import androidx.core.net.toUri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

private val fitnessOptions = listOf("练胸", "练背", "练肩", "练腿", "练腹")

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
            if (snackbarHost.showSnackbar("已删除“${task.title}”", actionLabel = "撤销") ==
                androidx.compose.material3.SnackbarResult.ActionPerformed
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

    Box(Modifier.fillMaxSize()) {
        BackgroundImage(backgroundUri)
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHost) },
            topBar = {
                TopAppBar(
                    title = { Text("今日事项") },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
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
                            label = { Text(item.title) }
                        )
                    }
                }
            },
            floatingActionButton = {
                if (destination != Destination.HISTORY) {
                    ExtendedFloatingActionButton(
                        onClick = ::openNew,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("添加事项") }
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
            alpha = 0.22f
        )
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background.copy(alpha = 0.72f)))
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
        contentPadding = PaddingValues(16.dp, 18.dp, 16.dp, 92.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            HeaderCard(today, incompleteCount, completed, tasks.size, onPinWidget)
            Spacer(Modifier.height(16.dp))
            Text("今天的事项", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (tasks.isEmpty()) {
            item { EmptyMessage("今天还没有事项，添加一件开始吧。") }
        }
        items(tasks, key = { it.id }) { task ->
            TaskCard(task, onToggle, onStar, onDelete, onEdit, onMove)
        }
    }
}

@Composable
private fun HeaderCard(today: LocalDate, incomplete: Int, completed: Int, total: Int, onPinWidget: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                today.format(DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                if (incomplete == 0) "今天完成啦" else "还有 $incomplete 件事",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text("已完成 $completed / $total", color = MaterialTheme.colorScheme.onPrimaryContainer)
            TextButton(onClick = onPinWidget) {
                Icon(Icons.Default.Widgets, contentDescription = null, Modifier.size(18.dp))
                Text("  放到桌面")
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
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 92.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Text("未来安排", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        if (tasks.isEmpty()) item { EmptyMessage("未来没有安排，给之后的自己留下一项计划吧。") }
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
        contentPadding = PaddingValues(16.dp, 20.dp, 16.dp, 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("完成历史", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                TextButton(onClick = onAddGroup) {
                    Icon(Icons.Default.Folder, contentDescription = null, Modifier.size(18.dp))
                    Text(" 新建分组")
                }
            }
        }
        item {
            HistoryGroupsPanel(groups, tasks)
        }
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("分组", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (groups.isEmpty()) {
                Text("还没有分组。可以把“练胸”这类完成记录建成分组，以后同名事项完成后会自动归入。")
            } else {
                groups.forEach { group ->
                    val count = tasks.count { it.historyGroupId == group.id }
                    Text("${group.name}：$count 条（匹配：${group.matchTitle}）")
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                    task.title,
                    Modifier.padding(start = 12.dp).weight(1f),
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (groupName != null) {
                    AssistChip(onClick = onChooseGroup, label = { Text("分组：$groupName") })
                } else if (hasGroups) {
                    AssistChip(onClick = onChooseGroup, label = { Text("选择分组") })
                }
                AssistChip(onClick = onCreateGroup, label = { Text("按此名称建组") })
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, Modifier.padding(top = 14.dp, bottom = 2.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun EmptyMessage(text: String) {
    Box(Modifier.fillMaxWidth().padding(top = 36.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    Card(
        modifier = Modifier.fillMaxWidth().then(dragModifier),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isStarred) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle(task) })
            Column(Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (task.isStarred) FontWeight.SemiBold else FontWeight.Normal,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (task.isCarried) Label("已延续", MaterialTheme.colorScheme.primary)
                    if (task.recurrenceTemplateId != null) Label("重复", MaterialTheme.colorScheme.secondary)
                }
            }
            IconButton(onClick = { onStar(task) }) {
                Icon(
                    if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "星标",
                    tint = if (task.isStarred) Color(0xFFC67A00) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onEdit(task) }) { Icon(Icons.Default.Edit, contentDescription = "编辑") }
            IconButton(onClick = { onDelete(task) }) { Icon(Icons.Default.Delete, contentDescription = "删除") }
            if (onMove != null && !task.isCompleted) {
                Icon(Icons.Default.DragHandle, contentDescription = "长按拖动", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Label(text: String, color: Color) {
    Text(
        text,
        modifier = Modifier.background(color.copy(alpha = 0.13f), RoundedCornerShape(5.dp)).padding(horizontal = 5.dp, vertical = 2.dp),
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
    var fitnessMode by remember(existing?.id) { mutableStateOf(existing?.title in fitnessOptions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "添加事项" else "编辑事项") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("事项标题") },
                        singleLine = true
                    )
                }
                item {
                    FilterChip(
                        selected = fitnessMode,
                        onClick = { fitnessMode = !fitnessMode },
                        label = { Text("健身事项") },
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
                if (fitnessMode) {
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            fitnessOptions.forEach { option ->
                                FilterChip(
                                    selected = title == option,
                                    onClick = { title = option },
                                    label = { Text(option) }
                                )
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = { choosingDate = true },
                            label = { Text(date.displayDate()) },
                            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, Modifier.size(18.dp)) }
                        )
                        FilterChip(
                            selected = starred,
                            onClick = { starred = !starred },
                            label = { Text("重要") },
                            leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, Modifier.size(18.dp)) }
                        )
                    }
                }
                item { HorizontalDivider() }
                item {
                    FilterChip(
                        selected = repeats,
                        onClick = { repeats = !repeats },
                        label = { Text("重复事项") },
                        leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
                if (repeats) {
                    item {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                RecurrenceKind.FIXED_DAILY to "每天",
                                RecurrenceKind.FIXED_WEEKLY to "每周",
                                RecurrenceKind.ROLLING_DAYS to "完成后 N 天",
                                RecurrenceKind.ROLLING_WEEKS to "完成后 N 周"
                            ).forEach { (option, label) ->
                                FilterChip(selected = kind == option, onClick = { kind = option }, label = { Text(label) })
                            }
                        }
                    }
                    if (kind == RecurrenceKind.FIXED_WEEKLY) {
                        item {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                DayOfWeek.entries.forEach { day ->
                                    FilterChip(
                                        selected = day in weekdays,
                                        onClick = {
                                            weekdays = if (day in weekdays && weekdays.size > 1) weekdays - day else weekdays + day
                                        },
                                        label = { Text(day.shortName()) }
                                    )
                                }
                            }
                        }
                    }
                    if (kind == RecurrenceKind.ROLLING_DAYS || kind == RecurrenceKind.ROLLING_WEEKS) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("间隔 ")
                                TextButton(onClick = { if (interval > 1) interval-- }) { Text("-") }
                                Text("$interval", fontWeight = FontWeight.Bold)
                                TextButton(onClick = { interval++ }) { Text("+") }
                                Text(if (kind == RecurrenceKind.ROLLING_DAYS) " 天" else " 周")
                            }
                        }
                    }
                    if (existing != null && template != null) {
                        item { TextButton(onClick = onStopRepeating) { Text("停止以后重复") } }
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
                }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
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
        title = { Text("设置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("背景")
                Button(onClick = onPickBackground) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, Modifier.size(18.dp))
                    Text("  从手机图片选择背景")
                }
                if (hasBackground) {
                    TextButton(onClick = onClearBackground) { Text("恢复默认背景") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } }
    )
}

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
        title = { Text("新建历史分组") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("分组名称，例如：健身") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = matchTitle,
                    onValueChange = { matchTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("匹配事项名称，例如：练胸") },
                    singleLine = true
                )
                if (completedTitles.isNotEmpty()) {
                    Text("从已完成事项选择")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        completedTitles.take(12).forEach { title ->
                            AssistChip(
                                onClick = {
                                    matchTitle = title
                                    if (name.isBlank()) name = title
                                },
                                label = { Text(title) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = matchTitle.isNotBlank(),
                onClick = { onSave(name.ifBlank { matchTitle }, matchTitle) }
            ) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
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
        title = { Text("选择分组") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("把“${task.title}”放入：")
                groups.forEach { group ->
                    AssistChip(
                        onClick = { onAssign(group.id) },
                        label = { Text("${group.name}（匹配：${group.matchTitle}）") }
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } }
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
