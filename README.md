# note-widget

A deliberately dumb Android app: **one note, one home screen widget.**

- The widget shows a single note and scrolls with a swipe. Tap it to edit.
- No notes list, no accounts, no internet permission. The note is stored locally in `SharedPreferences`.
- Saved automatically when you leave the editor.
- Minimal formatting: heading (`H`), bold (`B`), paragraph (`¶`).
- Four independent colors (`●` button in the editor): widget background, widget text, app background, app text.
  Each takes `#RRGGBB` or `sys` for Material You dynamic colors; an empty text color means automatic contrast.

Requires Android 8.0+ (API 26). Material You colors and the exact background sizing need Android 12+.

## Note format

The note is plain text. A line starting with `# ` is a heading, `**text**` is bold.

## Build

### Command line (no Gradle)

Needs `aapt`, a JDK (8+), `d8` or `dx`, `zipalign`, `apksigner`, `keytool` and an `android.jar` (API 31+).

```bash
export ANDROID_JAR=$ANDROID_HOME/platforms/android-34/android.jar
./scripts/build.sh          # -> build/Note.apk
```

The APK is signed with the standard Android debug keystore (`~/.android/debug.keystore`,
created on first run). Set `KEYSTORE=/path/to/your.keystore` to use another one.
Every commit in this repository was built this way.

### Gradle / Android Studio

The project also has a standard Gradle layout (`app/`), so Android Studio can open it.
The Gradle configuration has not been run; the script above is the verified path.

## Layout

| Path | What |
|---|---|
| `app/src/main/java/app/note/NoteWidget.java` | widget provider |
| `.../NoteService.java` | feeds the note to the widget as scrollable rows (and paints their background) |
| `.../EditActivity.java` | full-screen editor, toolbar, color dialog |
| `.../Fmt.java` | tiny markup (`# `, `**`) to styled text |
| `.../Pal.java`, `Store.java` | colors and storage |

## License

MIT
