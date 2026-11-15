# UI Modernization Plan

This document outlines a plan to modernize the DataCleaner desktop UI: fixing the
broken HiDPI scaling story, refreshing the dated look &amp; feel, and evaluating
options for larger overhauls.

## Current state (diagnosis)

| Area | Status | Notes |
|---|---|---|
| Java target | **Java 8** (`pom.xml`, `<source>1.8</source>`) | Root cause of the scaling problems. Java 8 Swing ignores OS display scaling (HiDPI) entirely. |
| Scaling | Custom `WidgetScreenResolutionAdjuster` (`desktop/api/.../util/WidgetScreenResolutionAdjuster.java`) | Scales hardcoded pixel sizes by *screen width* ÷ 1600px, capped at 3x. Used in 17 classes, ~70 call sites. Width is a proxy for DPI, so a 1920×1080 27" monitor gets scaled like a small laptop. Inconsistent by design. |
| Look &amp; Feel | JGoodies `PlasticXPLookAndFeel` (`LookAndFeelManager.java`) | JGoodies Looks is abandoned (~2011, v2.2.2). Around 150 lines of manual `UIManager.put(...)` overrides to patch its colors. |
| Fonts/icons | Bundled Muli/Lato/FontAwesome TTFs; 110 raster PNG icons | Icons blur when scaled; FontAwesome 4.3 icon font is outdated. |
| Other deps | SwingX 1.6.5, custom `DCScrollBarUI`/`DCComboBoxUI`/`DefaultButtonUI` (Metal-based) | Mostly maintenance mode; custom UI delegates will fight any new LaF. |

**Key insight:** the `WidgetScreenResolutionAdjuster` hack exists *because* Java 8
Swing cannot see OS display scaling. Modern Java (9+) handles HiDPI (including
per-monitor scaling) natively in Swing, making the entire mechanism obsolete.

---

## Phase 0 — Low hanging fruit (immediate, low risk)

1. **Adopt [FlatLaf](https://www.formdev.com/flatlaf/) as the look &amp; feel**
   (replaces PlasticXP + the `UIManager` patch block in `LookAndFeelManager`).
   FlatLaf is actively maintained, looks genuinely modern (IntelliJ-family
   styling), has native HiDPI support, and honors the existing `UIManager`
   defaults for easy migration. Start with `FlatLightLaf`, then theme via
   FlatLaf properties (`.properties` files) instead of code.
   - Effort: days. Risk: low. Biggest single visual win available.
2. **Upgrade the Java target to 17** (LTS; 21 also fine). Swing gets:
   - automatic HiDPI scaling on Windows/Linux,
   - per-monitor scale-factor awareness,
   - crisp text rendering at 150%/200% OS scaling.
   - Note: verify bundled native deps (e.g. JIDER) and launcher scripts for the new JRE.
3. **Remove `WidgetScreenResolutionAdjuster`** once on Java 17 + FlatLaf.
   - Make `adjust()`/`adjust(...)` return the input unchanged first (compatibility shim), then delete usages in a sweep of the 17 classes.
   - Replace `adjuster.scale(image)` with proper multi-resolution handling (below).
4. **Font stack refresh**: replace Muli (deprecated) with Inter or keep Muli 2/ Inter via FlatLaf defaults; upgrade FontAwesome 4.3 → use a maintained icon set (see Phase 2).
5. **Dark mode, nearly free**: FlatLaf ships `FlatDarkLaf` + `FlatLaf.setGlobalExtraPreferences(...)`. Add a theme switcher in Options. Huge perceived-modernity win for ~1 day of work.

## Phase 1 — Consistency &amp; assets (short term)

1. **Icons: move to SVG** rendered at runtime (crisp at any DPI, tintable).
   - Options: FlatLaf's built-in `FlatSVGIcon` (recommended), or keep FontAwesome for simple glyphs but render it via `deriveFont(2x)` scaling.
   - Replace the 110 PNGs gradually; prioritize the most visible (tree, toolbar, banners, job graph).
2. **Kill hardcoded pixel sizes** (`setPreferredSize(new Dimension(700, 500))` etc. in dialogs/windows). Use layout managers + `pack()`; where a size hint is needed, scale from font metrics (`UIManager.getFont("Label.font").getSize2D()`), which follows the OS scaling.
3. **Retire custom UI delegates** (`DCScrollBarUI`, `DCComboBoxUI`, `DefaultButtonUI`, `DCPopupFactory`) in favor of FlatLaf's built-ins — fewer places where the LaF is fought.
4. **Spacing/typography system**: define a small set of constants (spacing 4/8/16/24, font scale small/normal/header) in `WidgetUtils` mirroring FlatLaf's `$scale` properties so everything scales from one factor.

## Phase 2 — Big overhaul options (choose one direction)

### Option A: Deep Swing modernization (recommended)
Stay on Swing; build on Phase 0–1 and invest in UX rather than plumbing:
- New window/frame chrome, welcome screen, and job-builder canvas refresh.
- FlatLaf extras: `FlatLaf-IntelliJ-Theme`/`FlatLaf-Extras`, `flatlaf-jide-oss` for components we miss.
- Replace SwingX bits (JXTable/JXMonthView) with plain `JTable` + modern alternatives (`LGoodDatePicker`, `JCalendar`).
- Effort: weeks–a few months. Risk: low–medium. Retains all 100k+ lines of working UI code.

### Option B: JavaFX rewrite (UI layer only)
Rewrite `desktop/ui` in JavaFX (CSS styling, FXML or code, `ControlsFX`, FX canvas for job graph).
- Pros: modern rendering/animations, proper CSS, maintained toolchain.
- Cons: rewrite of essentially the whole desktop module; all custom Swing widgets (tables, trees, graph canvas, property widgets) need ports; result renderers too. Effort: many months, high risk.
- Only worth it if we want to keep a desktop app but shed Swing's legacy.

### Option C: Web-UI (drastic)
Keep the engine (`api`, `engine`, `components`) as-is; serve a local web UI
(REST/GraphQL), and build a browser frontend (React etc.), packaged with
jpackage/Electron/Tauri.
- Pros: biggest reach, easiest theming, no Swing forever.
- Cons: this is a *product* rewrite, not a UI refresh — months of work, new
  team skills, and the job-builder canvas must be rebuilt either way.
- Reasonable long-term direction **if** web delivery is strategically desired;
  otherwise not justified by "dated UI" alone.

### Recommendation
Do Phase 0 immediately (FlatLaf + Java 17 + delete the adjuster) — that alone
solves the scaling inconsistency and makes the app look current. Then Phase 1,
and decide on A vs. B/C only after experiencing the Phase 0 result; the sunk
cost of a rewrite is rarely worth it for a working tool like this.

---

## Suggested order of work

| # | Task | Size |
|---|---|---|
| 1 | Upgrade build to Java 17, fix any compile issues | S–M |
| 2 | Swap PlasticXP → FlatLightLaf; move `LookAndFeelManager` patches to FlatLaf properties | S |
| 3 | Add dark theme toggle | S |
| 4 | Neuter `WidgetScreenResolutionAdjuster` (identity shim), then delete call sites | M |
| 5 | SVG icons via `FlatSVGIcon` for top-used icons | M |
| 6 | Remove hardcoded `Dimension`s in dialogs; layout-manager + `pack()` | M |
| 7 | Replace SwingX/custom UI delegates | M |
| 8 | Decision point: Option A (deep polish) vs. B/C (rewrite) | — |

## Verification checklist (per phase)

- Screenshots on 1080p, 1440p @100%, 125%, 150% Windows scaling, Retina macOS.
- Manual smoke test: welcome dialog, job builder canvas, result windows, options dialog.
- `mvn clean install` with the new compiler level across all modules.