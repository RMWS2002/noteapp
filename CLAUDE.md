# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**NoteApp** — Android 个人效率工具（待办 + 日程 + 搜索），Kotlin + Jetpack Compose + Material 3 + Room。

APK 输出到 `C:\Users\21241\Desktop\NoteApp.apk`。GitHub: `RMWS2002/noteapp` master 分支。

## Build

```bash
cd C:\Users\21241\Desktop\2\NoteApp
./gradlew assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk C:\Users\21241\Desktop\NoteApp.apk
```

签名配置在 `app/build.gradle.kts`（release signingConfig 使用 `noteapp.jks`，密码 `123456`）。

## Architecture

### Navigation (custom, no Jetpack Navigation)

4 tabs: 首页 / 待办 / 日程 / 搜索。`NavGraph.kt` 用 `HorizontalPager` + `rememberPagerState(4)` 切换。

**Overlay 模式**：编辑页 / 设置页通过 `sealed class Overlay` 控制。当前 overlay != None 时隐藏 pager，通过 `AnimatedVisibility` + `slideInHorizontally` 滑入编辑页。返回时设 `overlay = Overlay.None`。

**已完成待办**用 `ModalBottomSheet` 展示，不占全屏。

**返回手势**：`NavGraph` 层有 `BackHandler(enabled = overlay !is Overlay.None || showCompletedSheet)` 拦截系统返回，防止退出 app。

### Data Layer

Room 数据库 `AppDatabase` (version=4, `fallbackToDestructiveMigration`)。3 张表：`todos`, `schedules`, `tags`。

ViewModel → Repository → DAO → Entity。所有 ViewModel 继承 `AndroidViewModel`，通过 `(application as NoteApp)` 获取 Repository。无 DI 框架。

### Key Entities

- **TodoEntity**: id, title, isCompleted, dueDate?, hasTime, reminderTime?, tagId?, completedAt?, createdAt
- **ScheduleEntity**: id, title, description, startTime, endTime, syncedToCalendar
- **TagEntity**: id, name, color (hex string)

### HomeScreen Layout (top to bottom)

```
GreetingHeader (纯文字问候，无 emoji)
DailyQuote    (72条中文名言，按 dayOfYear 轮换)
MiniWeekStrip (7天可点击周条，有日程的日期显示实心圆点)
StatsRow      (双列统计卡片：待办进度 + 今日日程数)
QuickActionChip (新待办 / 新日程)
今日日程      (LazyRow 紧凑卡片)
待办事项      (最多5条，带完成动画)
```

### ScheduleScreen

顶部 `HorizontalDateStrip`（14天横向滚动，当前选中日期居中高亮，有事件的日期显示圆点）。下方 `LazyColumn` 时间线：本地日程 + 系统日历事件（📅 图标区分）。FAB 添加新日程。

## Design Standards (CRITICAL)

This user has extremely high design standards. Review `C:\Users\21241\.claude\projects\c--Users-21241-Desktop-2\memory\design-excellence-standards.md` and `noteapp-project-lessons.md` before any UI work.

### Quick Reference

```
配色   → primary #B8923C (琥珀金) | background #FBFAF7 (暖白) | onSurface #1D1B18 (深棕)
间距   → 8dp 栅格: 4/8/12/16/20/24
圆角   → Card 16dp | Chip 14dp | Badge 圆形
层级   → 0dp bg → 1dp card → 6dp dialog + scrim
动效   → 200-250ms tween/spring | stagger 80ms 间隔
排版   → headlineMedium(24sp) / titleMedium(16sp) / bodyLarge(16sp, lh 26sp) / labelSmall(11sp)
```

### DO NOT

- Use emoji (🌻☀️🌤) — cheapens the design
- Use blue as primary color — too cold, too "enterprise"
- Use thick borders on cards — use elevation (1dp) instead
- Put English content in quotes — all Chinese
- Use `Modifier.weight()` inside `verticalScroll` — causes height=0
- Use `MaterialTheme.colorScheme` inside `drawWithContent {}` — not a `@Composable` context
- Use `animateScrollToPage` + `snapshotFlow { currentPage }` together — causes nav race

### DO

- Use warm amber/gold tones, generous whitespace, strong typography contrast
- Use `elevation` (1-3dp) rather than `BorderStroke` for visual hierarchy
- Animate strikethrough with `drawWithContent` + `Animatable` (250ms EaseOut, 0→100%)
- Pre-capture `MaterialTheme.colorScheme.*` values outside `drawWithContent`
- Use `scrollToPage` (instant) for programmatic tab switches to avoid race with `snapshotFlow`
- Test on real device (Xiaomi) before reporting success

### Known Bug Patterns

| Pattern | Symptom | Fix |
|---------|---------|-----|
| `.weight()` in scrollable Column | Element height=0 | `.heightIn(min = 200.dp)` |
| `LaunchedEffect` x2 bidirectional sync | Wrong tab on click | `scrollToPage` not `animateScrollToPage` |
| `TextDecoration.LineThrough` alone | Strikethrough snaps | `drawWithContent` + animated progress |
| Multiple Composables without `Column`/`Row` wrapper | Elements overlap | Wrap in `Column {}` |
| Missing `@Composable` context in `drawWithContent` | Compile error | Capture color before lambda |

### User Feedback Patterns

User communicates bluntly ("丑死了", "你会不会UI设计") — this is design critique, not personal. They give detailed direction when unsatisfied. They value: clean minimalism, warm tones, meaningful animation, Chinese-only content, interactive elements (no dead UI). They dislike: emoji, cold blue, dense layouts, English text, and non-interactive "shell" components.
