# NoteVault roadmap

Status: **paused on purpose.** v2.0 design work is done and pushed. The next
step is the rich formatting pass described below. Nothing is half-finished.

## The benchmark we measured against

On 2026-09-26 I opened **MIUI Notes** (the pre-installed Xiaomi app) on the
Redmi Note 11 and catalogued its feature set, so NoteVault has a concrete target
instead of a vague "make it better". Findings are in
`docs/benchmark-miui-notes.md`.

Short version of the gaps:

| Feature | MIUI Notes | NoteVault | Cost to add |
| --- | --- | --- | --- |
| Bold / italic / underline | yes | no | cheap |
| Numbered list | yes | no | cheap |
| Bulleted list | yes | no | cheap |
| Checkbox / task list | yes | no | cheap |
| Text alignment (centre / right) | yes | no | cheap |
| Font size increase / decrease | yes | no | cheap |
| Text colour | yes | no | cheap |
| Image insert | yes | no | medium |
| Voice notes | yes | no | medium |
| Undo / redo | yes | no | cheap |
| Character count in editor | yes | no | cheap |
| Font family picker | **no** | no | expensive |
| Drawing / sketch | **no** | no | expensive |
| Code blocks / tables | **no** | no | expensive |
| Web clipping | no (excerpt reminder only) | no | expensive |

## Proposed next step: the formatting pass

One focused pass covering everything in the "cheap" column, because these all
share the same plumbing and it would be wasteful to build them one at a time.

1. **A real rich-text engine.** This is the actual work. Jetpack Compose has no
   mature built-in rich-text editor, so this is the decision point:
   - *WebView-based editor* - an embedded HTML editor. Fast, reliable, roughly
     one session, slightly "web-ish" looking.
   - *Custom Compose editor* - fully native feel, three to five times the work.
   Recommendation: WebView first, swap the engine later if the look bothers.
2. Formatting bar above the keyboard: B / I / U, three list types, alignment,
   font size -/+, text colour.
3. Undo / redo.
4. Character count in the editor header, matching what MIUI Notes shows.
5. Persist the formatting. This decides the data model: either Markdown text
   (simple, exports cleanly, limits what can be stored) or a styled-document
   format (more capable, more work, needs a migration later).

Not in this pass: images and voice notes. They need file storage, a media
permission story and an attachment table, so they are a separate pass.

## Open questions for Salman

- Rich text: WebView engine or custom Compose?
- Storage: plain Markdown text, or a styled format?
- Next after formatting: image insert, or voice notes?
- Swipe gestures (swipe to delete, swipe to pin) and a custom hand-drawn icon
  set are still outstanding polish items from the v2.0 list.

## Working while the phone is connected

`tools/phone.sh` drives the Redmi Note 11 over ADB from WSL2. Design changes
should be verified against real screenshots rather than guessed at - that is how
the dark-on-dark text bug and the completely broken light mode were found.

One reminder: the vault sets `FLAG_SECURE`, so any screenshot taken inside the
vault is a 0-byte file. That is the feature working, not a bug. To look at those
screens, `TEMP_ALLOW_VAULT_SCREENSHOTS` in `NoteVaultApp.kt` can be flipped
temporarily - it **must** be `false` in any build you install.
