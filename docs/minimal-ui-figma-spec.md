# 今日事项 Minimal UI Figma 设计规格

## 设计关键词

极简、高级、安静、克制、有秩序感、轻量化、专注。

整体参考 Apple iOS 原生设计、Notion、Clean UI、Modern Mobile UI。避免高饱和颜色、复杂渐变和装饰性图案。界面以信息层级、留白、字体和细线建立质感。

## 画板规格

- Frame: iPhone 15 / Android phone, 393 x 852
- Safe area: top 56, bottom 34
- Grid: 4 columns
- Margin: 24
- Gutter: 16
- Base spacing: 4 / 8 / 12 / 16 / 24 / 32

## 色彩系统

```text
Background / Warm White: #F7F4EF
Surface / Card:         #FFFFFF
Surface Soft:           #F1EEE8
Text Primary:           #171717
Text Secondary:         #666666
Text Tertiary:          #9A9A9A
Hairline:               #E7E2DA
Accent Sage:            #7C8B7A
Accent Taupe:           #A28F7A
Accent Mist:            #D8DDD2
Danger Soft:            #B66A5E
```

使用方式：

- 主背景使用 `#F7F4EF` 或 `#FAF9F6`。
- 卡片使用白色，边框使用 `#E7E2DA`，阴影非常轻。
- 主按钮只用黑色或深灰，不用高饱和色。
- 健身标签可使用低饱和鼠尾草绿 `#7C8B7A`。

## 字体

Figma 建议：

- 中文：PingFang SC / Noto Sans SC
- 英文数字：SF Pro Text / Inter

字号建议：

```text
Large Title: 32 / 38, Semibold
Title:       24 / 30, Semibold
Section:     17 / 24, Medium
Body:        15 / 22, Regular
Caption:     12 / 18, Regular
Tab:         11 / 14, Medium
```

文字规则：

- 避免过粗，使用 Medium / Semibold。
- 数字可略大，用于“还有 3 件事”等状态提示。
- 二级信息使用浅灰，降低视觉噪音。

## 组件规范

### 顶部栏

- 高度：56
- 左侧：页面标题 `今日`
- 右侧：线性设置图标
- 背景透明或同页面背景
- 不使用厚重阴影

### 今日状态卡片

- 宽度：铺满内容区
- 圆角：28
- 背景：白色
- 边框：1px `#E7E2DA`
- 阴影：0, 8, 24, 8% black
- 内容：
  - 日期：小号灰字
  - 主状态：`今天还有 3 件事`
  - 进度条：极细线，圆角
  - 小组件入口：浅灰胶囊按钮

### 任务卡片

- 高度：64-76
- 圆角：20
- 背景：白色
- 边框：1px `#E7E2DA`
- 左侧：20px 空心圆 checkbox
- 中部：任务标题 + 辅助标签
- 右侧：线性星标 / 更多
- 完成态：标题灰色 + 细删除线

### 底部导航

- 高度：72
- 背景：半透明白 `#FFFFFF / 86%`
- 顶部细线：`#E7E2DA`
- 三个入口：今天 / 未来 / 历史
- 图标使用线性风格，选中态深黑，未选中态浅灰

### 浮动添加按钮

- 位置：右下角，距离边缘 24
- 圆角：24
- 背景：`#171717`
- 文字：白色
- 图标：线性加号
- 文案：`添加`

## 页面设计

### 1. 今天页

信息顺序：

1. 顶部栏：`今日`
2. 日期与设置入口
3. 今日状态卡片
4. 分区标题：`待完成`
5. 任务列表
6. 底部导航 + 添加按钮

示例内容：

```text
今天还有 3 件事
已完成 2 / 5

待完成
□ 练胸          ☆
□ 写周计划      ☆
□ 整理房间      ☆
```

### 2. 新增事项弹窗 / Sheet

建议使用底部 Sheet，而不是居中 Dialog，更像现代手机 App。

结构：

```text
添加事项
[事项标题输入框]
[健身事项 chip]

健身事项选中后出现：
练胸 / 练背 / 练肩 / 练腿 / 练腹

日期：今天
重要：开关
重复事项：开关

取消        保存
```

视觉：

- Sheet 圆角：顶部 32
- 背景：白色
- 输入框：浅灰底，不使用强边框
- 选中的健身 chip：黑底白字或鼠尾草绿底深字

### 3. 未来页

信息按日期分组，卡片间距比今天页更宽松。

```text
未来

6月15日
□ 复盘训练计划

6月18日
□ 体检预约
```

### 4. 历史页

顶部先展示分组，然后保留原时间列表。

```text
历史
[+ 新建分组]

分组
健身    12 条
学习     6 条

今天
✓ 练胸      分组：健身
✓ 写周计划
```

分组卡片：

- 横向小卡片
- 圆角：20
- 背景：`#F1EEE8`
- 显示名称、数量、匹配名称

### 5. 设置页

建议为极简列表页或 Sheet。

```text
设置

外观
背景图片       选择
恢复默认背景

关于
今日事项 v1.0
```

## Figma 图层命名建议

```text
00 Cover
01 Today
02 Add Task Sheet
03 Future
04 History
05 Settings
Components / Task Card
Components / Navigation Bar
Components / Chips
Styles / Colors
Styles / Typography
```

## 给 Figma AI / 设计师的生成提示词

```text
Design a minimalist premium mobile task app UI for an Android personal memo app named 今日事项.
Style: clean, quiet, restrained, modern, Apple iOS inspired, Notion inspired, lots of whitespace, clear hierarchy.
Color palette: warm off-white background, white cards, black and gray typography, subtle low-saturation sage/taupe accents.
Avoid bright saturated colors, heavy gradients, decorative patterns, crowded layouts.

Create 5 mobile screens:
1. Today screen with date, progress card, task list, bottom navigation, floating add button.
2. Add task bottom sheet with title field, fitness quick chips: 练胸, 练背, 练肩, 练腿, 练腹.
3. Future screen with tasks grouped by date.
4. History screen with top history groups and original date timeline.
5. Settings screen with background image picker.

Use rounded cards, thin dividers, subtle shadows, line icons, modern Chinese typography, generous spacing.
```
