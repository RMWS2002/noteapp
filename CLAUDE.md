# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**NoteApp** — Android 个人效率工具，Kotlin + Jetpack Compose + Material 3 + Room。
4 个 Tab：首页 / 待办 / 日程 / 搜索。笔记功能已在 v8 删除。

APK 输出: `C:\Users\21241\Desktop\NoteApp.apk`
GitHub: `RMWS2002/noteapp` master 分支

## Build

```bash
cd C:\Users\21241\Desktop\2\NoteApp
./gradlew assembleDebug
cp app/build/outputs/apk/debug/app-debug.apk C:\Users\21241\Desktop\NoteApp.apk
git add -A && git commit -m "..." && git push
```

签名: `noteapp.jks`，密码 `123456`（`app/build.gradle.kts` release signingConfig）。

---

## Architecture

### Navigation (custom, no Jetpack Navigation)

`ui/navigation/NavGraph.kt` — 4 tabs via `HorizontalPager(rememberPagerState(4))`。

**Overlay sealed class**: `None | TodoEdit(id?) | ScheduleEdit | Settings`
编辑/设置页通过 overlay 控制。overlay != None 时隐藏 pager，`AnimatedVisibility` + `slideInHorizontally` 滑入。返回设 `overlay = Overlay.None`。

**已完成待办**: `ModalBottomSheet` 展示 `CompletedTodosContent`，不占全屏。

**返回手势**: `BackHandler(enabled = overlay !is Overlay.None || showCompletedSheet)` 拦截系统返回。

**tab 切换**: 用 `scrollToPage`（瞬时）不用 `animateScrollToPage`，避免与 `snapshotFlow { currentPage }` 形成竞态。

### Data Layer

```
ViewModel (AndroidViewModel) → Repository → DAO → Entity (Room)
```

- `NoteApp.kt` — Application class，lazy 初始化 database / repositories
- `AppDatabase.kt` — Room，version=4，`fallbackToDestructiveMigration()`，3 张表
- ViewModel 通过 `(application as NoteApp).xxxRepository` 获取数据源，无 DI 框架
- 所有查询返回 `Flow<List<T>>`，ViewModel 用 `.stateIn(WhileSubscribed(5000))`

### Entities

| Entity | Table | Key Fields |
|--------|-------|------------|
| TodoEntity | todos | id, title, isCompleted, dueDate?, hasTime, reminderTime?, tagId?, completedAt?, createdAt |
| ScheduleEntity | schedules | id, title, description, startTime, endTime, syncedToCalendar |
| TagEntity | tags | id, name, color (hex) |

### Key files map

```
ui/navigation/NavGraph.kt          — 4 tab + Overlay + BottomSheet
ui/screens/HomeScreen.kt           — 5-step staggered LazyColumn
ui/screens/TodoListScreen.kt       — allTodos + FAB
ui/screens/TodoEditScreen.kt       — headline title + date presets + SettingRow
ui/screens/ScheduleScreen.kt       — HorizontalDateStrip + timeline (local + system events)
ui/screens/ScheduleEditScreen.kt   — matches TodoEditScreen quality
ui/screens/SearchScreen.kt         — todo-only search
ui/screens/SettingsScreen.kt       — theme / notify / calendar / tags
ui/screens/CompletedTodosScreen.kt — CompletedTodosContent (for BottomSheet)

ui/components/TodoRow.kt           — animated checkbox + drawWithContent strikethrough
ui/components/GreetingHeader.kt    — clean text greeting (NO emoji)
ui/components/DailyQuote.kt        — 72 Chinese quotes, dayOfYear rotation
ui/components/MiniWeekStrip.kt     — 7-day clickable strip with event dots
ui/components/StatsRow.kt          — 2-column todo/schedule stat cards
ui/components/QuickActionChip.kt   — "新待办" + "新日程" chips
ui/components/TimelineView.kt      — legacy timeline (ScheduleScreen no longer uses)
ui/components/EventDetailSheet.kt  — schedule detail ModalBottomSheet

ui/theme/Color.kt                  — warm amber palette tokens
ui/theme/Theme.kt                  — Light/Dark ColorScheme + NoteAppTheme
ui/theme/Type.kt                   — 15-style Typography (FontFamily.Default)

viewmodel/HomeViewModel.kt         — activeTodos, todaySchedules, weekSchedules
viewmodel/TodoViewModel.kt         — CRUD + alarm + completed logic
viewmodel/ScheduleViewModel.kt     — CRUD + calendar sync + system events
viewmodel/SearchViewModel.kt       — flatMapLatest search

data/calendar/CalendarSyncHelper.kt — ContentResolver read/write system calendar
```

### HomeScreen Layout (top → bottom)

```
GreetingHeader  ← "下午好" + date, NO emoji
DailyQuote      ← 72 Chinese quotes, dayOfYear rotation
MiniWeekStrip   ← 7-day clickable strip, dot = has events
StatsRow        ← 2 side-by-side cards (todo progress + schedule count)
QuickActionChip ← "新待办" / "新日程"
今日日程        ← LazyRow compact cards
待办事项        ← max 5 active todos with completion animation
```

### ScheduleScreen

`HorizontalDateStrip` — 14-day horizontal scroll, selected date centered highlighted, event dots below dates.
Timeline `LazyColumn` — local schedules (amber dot) + system calendar events (tertiary dot + 📅 icon). FAB to add.

---

## Design Standards — MANDATORY

The user evaluates every UI change against professional Material Design 3 standards.

### A. Core Principles

1. **Material is the metaphor** — surfaces, edges, shadows. Use elevation for hierarchy, not borders.
2. **Bold, graphic, intentional** — strong typography, generous whitespace, restrained color.
3. **Motion provides meaning** — physics-based easing, 200-300ms, no linear/instant transitions.

### B. Specifications

| Dimension | Standard |
|-----------|----------|
| Touch targets | ≥48dp (icon buttons padded) |
| Contrast | WCAG AA (4.5:1 body, 3:1 large) |
| Grid | 8dp baseline (4/8/12/16/20/24/32) |
| Elevation | bg(0dp) → card(1dp) → dialog(6dp+scrim). NO thick borders |
| Animation | 200-250ms tween/spring, stagger 80ms gaps |
| Corners | Card 16-20dp, Chip 12-14dp, Dot circular |
| Typography | headlineMedium(24sp Bold) / titleMedium(16sp) / bodyLarge(16sp, lh 26sp) |
| States | Cover default/hover/pressed/disabled/error for every component |

### C. Color Palette (Warm Amber)

```
primary          = #B8923C  (amber gold)
primaryContainer = #F5ECD7  (cream)
background       = #FBFAF7  (warm white)
surface          = #FFFFFF
onSurface        = #1D1B18  (deep brown)
surfaceVariant   = #F0EFEA  (warm gray)
outline          = #D6D3CD
error            = #DC3545
```

Dark mode: background = `#111110`, surface = `#1C1C1E`, primary = `#D4AE52`.

### D. What the User Hates (DO NOT)

- **Emoji** 🌻☀️🌤 — cheap, ruins clean aesthetic
- **Blue primary** — too cold, too corporate
- **Thick borders on cards** — use elevation instead
- **English content** — quotes, labels, all must be Chinese
- **Non-interactive UI** — if it's there, it must do something
- **Red number badges** — use filled/outlined icon distinction instead
- **Instant/snapping animations** — strikethrough must grow, elements must ease

### E. What the User Wants (DO)

- Warm amber/gold palette, lots of whitespace, strong typographic hierarchy
- `elevation` (1dp) not `BorderStroke` for card separation
- Physics-based spring animations (`DampingRatioMediumBouncy`)
- Strikethrough animation via `drawWithContent` + `Animatable` (250ms, 0→100% growth)
- All-Chinese quotes (Confucius, Su Shi, Laozi — not Steve Jobs)

### F. Known Compose Pitfalls

| Mistake | What Happens | Correct |
|---------|-------------|---------|
| `.weight()` in `verticalScroll` Column | 0 height | `.heightIn(min = 200.dp)` |
| `LaunchedEffect(tabIndex)` + `snapshotFlow{currentPage}` both active | Tab navigation race | Use `scrollToPage` not `animateScrollToPage` |
| `MaterialTheme.colorScheme` inside `drawWithContent{}` | Compile error (not @Composable) | Capture color value before lambda |
| Multiple Composables in `AnimatedVisibility` without `Column` wrapper | Elements overlap | `AnimatedVisibility{ Column{...} }` |
| `TextDecoration.LineThrough` without animation | Instant strikethrough snap | `drawWithContent` + animated progress bar |
| `remember{}` state lost on LazyColumn item reposition | Animation doesn't replay | `snapTo(1f)` before `animateTo()` in `LaunchedEffect` |

### G. User Communication Style

User feedback is direct and sharp ("丑死了", "你会不会UI设计"). This is design critique — not personal attack. They give detailed guidance when unsatisfied (e.g. full MD3 spec). They allow creative freedom but expect results matching professional standards. Always self-review before asking for feedback — they find bugs the developer should have caught.

---

## Iteration History (v6 → v8)

| Version | Key Changes | Lessons |
|---------|------------|---------|
| v6 | Staggered animations, OverviewCard, completed todos, todo strikethrough redesign | First attempt at strikethrough still ugly (dual system clash) |
| v7 | Bug fixes, quote card, week strip, schedule polish | English quotes rejected; 8 AnimatedVisibility steps too laggy; emoji flowers rejected |
| v8 | Deleted notes module, warm amber redesign, ScheduleScreen rewrite | Cleaner = better; removal > addition; regression risk in rewrites (lost system calendar events, greeting overlap) |
| v8.1 | Fixed calendar sync regression + greeting/quote overlap | Always check what was removed in a rewrite |

---

## Memory Files

Additional context in `~/.claude/projects/c--Users-21241-Desktop-2/memory/`:
- `design-excellence-standards.md` — 7-dimension design checklist + self-review methodology
- `noteapp-project-lessons.md` — detailed 8-iteration bug patterns and design decisions
