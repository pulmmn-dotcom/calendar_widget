---
name: Shift Rhythm Mobile
colors:
  surface: '#f8f9ff'
  surface-dim: '#cbdbf5'
  surface-bright: '#f8f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#eff4ff'
  surface-container: '#e5eeff'
  surface-container-high: '#dce9ff'
  surface-container-highest: '#d3e4fe'
  on-surface: '#0b1c30'
  on-surface-variant: '#444651'
  inverse-surface: '#213145'
  inverse-on-surface: '#eaf1ff'
  outline: '#757682'
  outline-variant: '#c5c5d3'
  surface-tint: '#4059aa'
  primary: '#00236f'
  on-primary: '#ffffff'
  primary-container: '#1e3a8a'
  on-primary-container: '#90a8ff'
  inverse-primary: '#b6c4ff'
  secondary: '#006a61'
  on-secondary: '#ffffff'
  secondary-container: '#86f2e4'
  on-secondary-container: '#006f66'
  tertiary: '#4b1c00'
  on-tertiary: '#ffffff'
  tertiary-container: '#6e2c00'
  on-tertiary-container: '#f39461'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b6c4ff'
  on-primary-fixed: '#00164e'
  on-primary-fixed-variant: '#264191'
  secondary-fixed: '#89f5e7'
  secondary-fixed-dim: '#6bd8cb'
  on-secondary-fixed: '#00201d'
  on-secondary-fixed-variant: '#005049'
  tertiary-fixed: '#ffdbcb'
  tertiary-fixed-dim: '#ffb691'
  on-tertiary-fixed: '#341100'
  on-tertiary-fixed-variant: '#773205'
  background: '#f8f9ff'
  on-background: '#0b1c30'
  surface-variant: '#d3e4fe'
  shift-day: '#2563EB'
  shift-evening: '#F59E0B'
  shift-night: '#7C3AED'
  shift-off: '#64748B'
  shift-special: '#E11D48'
  shift-vacation: '#10B981'
  shift-standby: '#06B6D4'
  shift-training: '#EC4899'
  calendar-sunday: '#DC2626'
  calendar-saturday: '#2563EB'
  calendar-today-bg: '#EFF6FF'
  calendar-today-ring: '#3B82F6'
  memo-dot: '#F59E0B'
  surface-card: '#FFFFFF'
  surface-bg: '#F8FAFC'
  surface-sheet: '#FFFFFF'
  border-subtle: '#E2E8F0'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.02em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.015em
  headline-md:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Inter
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.01em
  shift-tag:
    fontFamily: Inter
    fontSize: 10.5px
    fontWeight: '700'
    lineHeight: 12px
    letterSpacing: -0.01em
  widget-numeral:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '800'
    lineHeight: 32px
    letterSpacing: -0.03em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  margin: 1rem
  margin-mobile: 0.875rem
  space-2xs: 0.125rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
  space-2xl: 2rem
---

## Brand & Style

This design system is engineered specifically for frontline shift workers—nurses, manufacturing operators, paramedics, rail transit operators, and law enforcement officers. These users consult their work schedules in erratic conditions: under low-light hospital wards, during rapid shift turnovers, inside transit cabins, or with half-focused vision after consecutive night duties.

The brand persona is dependable, focused, and fatigue-reducing. It eschews decorative clutter in favor of ultra-high functional legibility, instantaneous pattern recognition, and calm confidence. 

The aesthetic is Modern Functionalist rooted in refined Material 3 principles:
- **Pristine surfaces:** Clean neutral backgrounds with delicate sub-pixel borders that frame complex multi-week schedules without visual vibration.
- **Color-coded operational clarity:** The 8 shift states are chromatic signals engineered for immediate peripheral discrimination.
- **Ergonomic touch rhythm:** Tap targets are generously sized (minimum 48×48dp) with tactile spring interactions and predictable bottom-anchored mod sheets.

## Colors

The palette is anchored by a deep slate navy primary (`#1E3A8A`), providing structural authority and sharp contrast against light slate canvas layers (`#F8FAFC`). 

The functional core is the 8-Color Shift Chromatic System, balanced across luminance channels to ensure distinct readability both as solid badges, tinted background pills, and compact widget underline meters:
1. **Day (주간):** Sapphire Blue (`#2563EB`) — alert, daytime vitality.
2. **Evening (오후):** Warm Amber (`#F59E0B`) — late afternoon sun, sunset vigilance.
3. **Night (야간):** Royal Violet (`#7C3AED`) — deep nocturnal focus, high contrast against white/dark surfaces.
4. **Off (휴무):** Cool Slate Gray (`#64748B`) — neutral rest state, de-emphasized.
5. **Special / On-Call (당직/특근):** Vivid Crimson (`#E11D48`) — urgent, priority intervention.
6. **Vacation (연차/휴가):** Emerald Green (`#10B981`) — replenishment, recovery.
7. **Standby (대기/콜):** Bright Cyan (`#06B6D4`) — technical readiness.
8. **Training / Other (교육/기타):** Magenta Rose (`#EC4899`) — scheduled administrative variation.

Calendar dates use conventional Sunday Red (`#DC2626`) and Saturday Blue (`#2563EB`) markers. Today's cell is highlighted with an airy tint (`#EFF6FF`) and an active inset ring token (`#3B82F6`).

## Typography

The typography scale utilizes **Inter** (fully compatible with Pretendard metrics in mobile implementations) to provide tabular figures, crisp vertical alignments, and uniform legibility across compact grid matrices.

Key rules:
- **Calendar Numerical Cells:** Date numerals use `body-md` (or `label-md` for the current month) with tabular figures enabled (`tnum`), preventing horizontal shifting across double-digit dates (10–31).
- **Shift Indicators:** Shift labels embedded in grid cells use `shift-tag` (10.5px Bold), optimized for quick visual scanning even in 6-row dense monthly layouts.
- **Widget Numerals:** For the 2×2 and 4×2 home screen widgets, `widget-numeral` delivers instantaneous glanceability from arm’s length.

## Layout & Spacing

The layout is built around mobile device constraints with strict edge budgeting:
- **Calendar Grid:** Spans a strict 7-column fluid grid. Column gutters inside the calendar grid are set to 1px via subtle borders (`#F1F5F9`), preserving maximum surface area for the 35–42 date cells.
- **Horizontal Margins:** Top-level screens maintain a strict `16px` (`margin`) outer padding, shrinking to `14px` on narrow screens under 360dp width.
- **Bottom Navigation Clearance:** Main views reserve `80dp` of bottom padding to ensure FABs and sheet triggers clear the system navigation bar and 4-tab bar.
- **Widget Micro-Spacings:** Widget templates use sub-scale gaps (`space-xs` = 4px and `space-2xs` = 2px) to maximize data density inside Android 70dp cell increments.

## Elevation & Depth

Visual hierarchy uses precise border delineations supplemented by ambient, low-spread shadows rather than heavy skeuomorphic drops:

- **Level 0 (Flat / Canvas):** Surface background (`#F8FAFC`). No shadow.
- **Level 1 (Cards, Pattern Rows, Widget Tiles):** Surface `#FFFFFF` enclosed by a 1px border (`#E2E8F0`), elevated by `0 1px 3px rgba(15, 23, 42, 0.05)`.
- **Level 2 (Active Today Highlight / Selected Shift Chip):** Inset ring `0 0 0 1.5px #3B82F6` and elevation `0 2px 6px rgba(37, 99, 235, 0.12)`.
- **Level 3 (Date Edit Bottom Sheet & Modals):** Backdrop blur `4px` on scrim (`rgba(15, 23, 42, 0.4)`), sheet surface `#FFFFFF` with top edge shadow `0 -4px 16px rgba(15, 23, 42, 0.08)`.
- **Level 4 (Floating Action Buttons):** Primary blue container (`#1E3A8A`) with shadow `0 4px 12px rgba(30, 58, 138, 0.28)`.

## Shapes

The design system adheres to a balanced `roundedness: 2` (8px base radius), producing a modern yet structured industrial-grade interface:

- **Calendar Day Cells:** `rounded-sm` (6px) internal hover/active target shapes.
- **Shift Badges & Shift Selection Chips:** Pill-shaped (`rounded-full` / 9999px) to communicate tappability and visual distinction from rectangular structural blocks.
- **Bottom Sheets & Modal Dialogs:** Rounded top edges with `rounded-t-2xl` (16px to 20px) creating an inviting tactile drawer.
- **Home Screen Widgets:** Follow native Android 12+ standard radius constraints (16px to 24px corner radius matching host launcher curvature).

## Components

### 1. Calendar Grid Cell
- **Container:** Minimum 44dp height. Vertical stack: date number (top-left or centered), memo indicator dot (3.5px amber circle at top-right), shift tag badge (center or bottom).
- **Today State:** Light blue fill (`#EFF6FF`), blue outline (`#3B82F6`), bold date text.
- **Past / Future Other Month Days:** Text dimmed to `#94A3B8`, shift tags rendered at 40% opacity.
- **Deleted Shift State:** Struck-through or gray outlined pill with text "삭제됨" (`#94A3B8`).

### 2. Shift Badges & Chips
- **Display Tag (Calendar):** Solid tint fill (15% opacity of shift color) with 100% saturated text in the same hue, or solid filled pill with white text for maximum contrast on primary shifts.
- **Selection Chip (Editor Sheet):** 40dp minimum height pill with 10-12dp colored circle avatar on the left, shift title, and subtle active checkmark when selected.

### 3. Bottom Sheet (Date Detail & Override)
- **Header:** Full Korean date display ("2026년 10월 15일 목요일"), close button, and dismiss grab-handle at top center.
- **Shift Selector Grid:** Horizontal scrolling or wrapping grid of all 8 shift chips. Tapping updates immediately with haptic pulse.
- **Revert Action:** Text button with refresh icon ("패턴 기본값으로 복귀"), disabled if the date already matches pattern.
- **Memo Field:** Outlined text area with 3-line max preview, auto-save state indicator on blur.

### 4. Bottom Navigation Bar
- **Tabs:** 4 items (`달력`, `근무 종류`, `근무 패턴`, `위젯 안내`).
- **Icons:** Dual-tone outlined/filled Material style icons. Active tab displays color tint (`#1E3A8A`) and active pill indicator.
- **Height:** 64dp standard bar + device navigation inset.

### 5. Shift Pattern Builder
- **Step List:** Horizontal sequential chip chain linked by directional arrow icons (`주간 → 주간 → 야간 → 휴무`).
- **Action Shelf:** Tap-to-add shift chips at bottom; counter badge showing cumulative cycle length ("4일 주기").

### 6. Widget Card Previews
- **Widget Preview Cards:** Realistically scaled mockups depicting the 4 sizes (하루 2×2, 일주일 4×2, 작은한달 3×2, 한달 4×4) with simulated live data, opacity slider preview, and size badge.
- **Empty State Indicator:** Prominent banner on widgets and preview when no pattern is bound ("근무 패턴을 등록해주세요" with tap-to-open app deep link).