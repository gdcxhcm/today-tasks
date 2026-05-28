# 今日事项

个人离线安卓任务清单 App，以“今天”为首页，支持未来安排、完成历史和重复事项。

## 已实现

- 今天 / 未来 / 历史三个页面
- 添加、编辑、删除、撤销删除、完成、星标与今天列表拖动排序
- 未完成任务跨天自动延续并显示标记
- 每天、每周指定日、完成后每 N 天 / 每 N 周的重复任务
- 今日未完成事项的静默数量通知与系统桌面角标来源
- 桌面小组件：展示今日未完成数量和前三项，支持一键打开新增事项
- 首页“放到桌面”按钮可直接向支持该功能的启动器申请固定小组件
- Android 13+ 通知权限请求，本地 Room 数据存储

## 构建

项目使用 Android Studio 附带的 JDK 21、Android SDK 36.1、AGP 9.1 与 Gradle 9.3.1。

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='C:\Users\gdc\AppData\Local\Android\Sdk'
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

生成的 APK 位于：

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 提醒说明

- 通知仅显示今天尚未完成的数量，不显示事项标题。
- 桌面红点或数字角标由手机桌面启动器控制；支持角标的启动器会根据持续通知显示提示，具体样式因手机而异。
- 首版未加入账号同步、备份导出或按时刻闹钟提醒。
