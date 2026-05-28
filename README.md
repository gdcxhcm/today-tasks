# 今日事项

今日事项是一款为个人自用设计的离线安卓事项清单 App。它围绕“今天要做什么”展开，把今天、未来计划和完成历史分开管理，同时支持重复任务、桌面小组件、健身快捷添加、历史分组和自定义背景。

这个项目的目标不是做复杂的团队协作工具，而是做一个打开手机就能快速记录、随时回看、不会被账号和同步打扰的本地任务本。

## 功能亮点

- **今天优先**：首页展示今日未完成数量、完成进度和任务列表。
- **未来计划**：未来页按日期展示之后要做的事情。
- **完成历史**：历史页永久保留本机完成记录，并按完成日期展示。
- **健身快捷添加**：新增事项里可快速选择 `练胸`、`练背`、`练肩`、`练腿`、`练腹`。
- **重复任务**：支持每天、每周、完成后每 N 天、完成后每 N 周。
- **跨天延续**：昨天没做完的普通任务会自动进入今天，并标记为已延续。
- **桌面小组件**：桌面展示今天未完成数量和前三项，支持一键新增。
- **本地通知和角标**：今天还有未完成任务时显示低打扰静默通知。
- **自定义背景**：可从手机图片中选择一张作为 App 背景。
- **历史分组**：可以为完成事项建立分组，之后同名事项完成后自动归入对应分组。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Room
- ViewModel / Flow
- Hilt
- AlarmManager
- NotificationManager
- AppWidgetProvider / RemoteViews

## 项目结构

```text
app/src/main/java/com/gdc/todaytasks
├── data        # Room 实体、DAO、Repository、偏好设置
├── di          # Hilt 依赖注入
├── reminder    # 跨天刷新、通知和角标
├── ui          # Compose 页面、对话框和 ViewModel
└── widget      # 桌面小组件
```

## 构建

项目使用 Android Studio 附带的 JDK 21、Android SDK 36.1、AGP 9.1 与 Gradle 9.3.1。

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\gdc\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

生成的 debug APK 位于：

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 使用说明

1. 打开 App 后，在“今天”页点击右下角按钮添加事项。
2. 添加健身事项时，打开“健身事项”开关，再选择训练部位。
3. 在首页点击“放到桌面”，可把今日事项小组件固定到手机桌面。
4. 在右上角设置中选择手机图片作为背景。
5. 在历史页点击“新建分组”，为完成记录设置自动归类规则。

## 隐私说明

- 所有任务数据保存在本机 Room 数据库中。
- 背景图片通过系统文件选择器授权，只在本机读取。
- 通知只显示“今天还有 N 件事”，不会在锁屏通知里展示任务标题。
- 当前版本不包含账号、云同步、团队协作、备份导出或按具体时刻提醒。

## 当前状态

这是一个自用 V1 版本，已经可以构建可安装的 debug APK，并在 Android 模拟器上验证了核心流程。
