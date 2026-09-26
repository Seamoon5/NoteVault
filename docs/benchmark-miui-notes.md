# Benchmark: MIUI Notes (com.miui.notes)

Measured on the Redmi Note 11 (Android 13, MIUI) on 2026-09-26 by driving the
app over ADB and reading the real UI with `uiautomator dump`, not from memory.
This is the reference NoteVault is being measured against.

## Editor formatting bar (8 buttons, single row)

Button labels below are the actual `content-desc` values reported by Android.

| id | content-desc |
| --- | --- |
| `reduce_font` | Smaller text |
| `bold` | Bold |
| `italic` | Italic |
| `underline` | Underline |
| `bullet` | Checkbox |
| `center` | Centre |
| `right` | Right |
| `audio` | Voice |

Note on the list buttons: visually the row shows **three** list-style icons
(numbered, bulleted, checkbox), but only one is exposed to accessibility, so the
exact mapping of the other two is inferred from the icons, not confirmed.

## Second row: insert and AI tools

- Tools drawer (opens a panel, two pages)
- Image / sticker insert
- GIF insert
- Settings gear
- Google Translate (operates on selected text)
- Colour palette
- Microphone (voice recording)

## Tools drawer, page 1 (6 tiles)

- Clipboard
- Emoji
- One-handed (shrinks the UI to one side of the screen)
- Text editing - a cursor D-pad with Select all, Select, Copy, Paste, indent,
  outdent and delete
- Share Gboard
- Resize (window resize, for split screen / foldables)

A second page exists (the panel shows two page dots).

## Editor chrome

- Undo, redo and done in the top bar, plus share
- Title is a separate field from the body
- Live character count, date and time shown under the title

## Settings

- **Cloud services** - Xiaomi Cloud sync, "Synced with the cloud", deleted notes
  in the cloud
- **Style** - Font size (Small / Medium / Large), Sort (by modification date, etc.)
- **Quick features** - Quick notes: an edge-of-screen shortcut for jotting a
  note or task, which also saves copied URLs, text and images straight into Notes
- **Excerpt reminders** - offers to create an excerpt when a URL is copied
- **Reminders** - high-priority reminders, play sound even in silent mode

## What it does *not* have

Worth knowing, because it stops us chasing features that are not the bar:

- No font family picker - only size
- No drawing or sketch tool
- No code blocks, no tables
- No web clipping (only the excerpt reminder)

## Permission note

Tapping the voice button triggers an "Allow Notes to record audio?" prompt. It
was dismissed without answering, and `RECORD_AUDIO` was confirmed still
`granted=false` afterwards.
