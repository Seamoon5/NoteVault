#!/usr/bin/env bash
#
# NoteVault phone helper (WSL2 -> real phone over USB).
#
# Why this uses the WINDOWS adb.exe:
#   WSL2 cannot see USB devices directly, so a Linux adb would never find the
#   phone. The Windows adb.exe can, and WSL can run it. That is the whole trick.
#
# Setup on the phone (one time):
#   Settings > About phone > tap "MIUI version" 7 times  (unlocks Developer options)
#   Settings > Additional settings > Developer options
#     - turn ON "USB debugging"
#     - turn ON "USB debugging (Security settings)"  (MIUI extra, may ask for Mi account)
#   Plug in the USB cable, pick "File transfer (MTP)" when asked
#   Accept "Allow USB debugging?" -> tick "Always allow" -> Allow
#
# Usage:
#   ./phone.sh devices              list connected phones
#   ./phone.sh install              install (or reinstall) the debug APK
#   ./phone.sh launch               open the app
#   ./phone.sh shot out.png         save a screenshot of the phone
#   ./phone.sh tap X Y              tap at screen coordinates
#   ./phone.sh swipe X1 Y1 X2 Y2    swipe
#   ./phone.sh text "hello"         type into the focused field
#   ./phone.sh key back|enter|home  send a key
#   ./phone.sh logs                 recent app crash output
#   ./phone.sh watch                live app log (Ctrl+C to stop)
#   ./phone.sh clear                wipe the log buffer
#   ./phone.sh size                 phone screen size + Android version
#   ./phone.sh uninstall            remove the app and all its data
#
set -u

ADB="/mnt/c/Users/Alauddin/AppData/Local/Android/Sdk/platform-tools/adb.exe"
PKG="com.seamoon5.notevault"
ACTIVITY="$PKG/.MainActivity"
PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APK="$PROJ/app/build/outputs/apk/debug/app-debug.apk"

if [ ! -x "$ADB" ]; then
  echo "ERROR: adb.exe not found at $ADB" >&2
  exit 1
fi

# Run adb and make sure a phone is actually there.
require_phone() {
  local out
  # tr -d '\r' matters: adb.exe is a Windows binary and emits CRLF, so
  # awk's $2 would be "device\r" and never match.
  out="$("$ADB" devices | tr -d '\r' | sed '1d' | awk '$2=="device" {print $1; exit}')"
  if [ -z "$out" ]; then
    echo "No phone connected." >&2
    echo "Run '$0 devices' to see what adb reports." >&2
    echo "If the list is empty: enable Developer options > USB debugging on the" >&2
    echo "phone, plug in the cable, and accept the 'Allow USB debugging' prompt." >&2
    exit 1
  fi
  echo "$out"
}

cmd="${1:-help}"
shift || true

case "$cmd" in
  devices)
    "$ADB" devices -l
    ;;

  install)
    require_phone >/dev/null
    if [ ! -f "$APK" ]; then
      echo "APK not found: $APK" >&2
      echo "Build it first:  gradle -p $PROJ assembleDebug" >&2
      exit 1
    fi
    echo "Installing $APK ..."
    # -r = reinstall, keep data.  -t = allow test/debug builds.
    if "$ADB" install -r -t "$APK"; then
      echo "Installed OK."
    else
      echo "Install failed. Common causes:" >&2
      echo "  - 'Install blocked by policy' -> allow this source in phone Settings" >&2
      echo "  - INSTALL_FAILED_UPDATE_INCOMPATIBLE -> run '$0 uninstall' then retry" >&2
      echo "  - signature mismatch -> run '$0 uninstall' then retry" >&2
      exit 1
    fi
    ;;

  launch)
    require_phone >/dev/null
    "$ADB" shell am start -n "$ACTIVITY"
    ;;

  shot)
    out="${1:-/tmp/opencode/phone-shot.png}"
    mkdir -p "$(dirname "$out")"
    require_phone >/dev/null
    # Screencap to the phone, then pull. adb.exe is a Windows binary, so piping
    # raw PNG bytes through WSL can corrupt them; pulling a real file cannot.
    if "$ADB" exec-out screencap -p > "$out.tmp" 2>/dev/null &&
       [ -s "$out.tmp" ] &&
       head -c 8 "$out.tmp" | od -An -tx1 | grep -qi '89 50 4e 47'; then
      mv "$out.tmp" "$out"
    else
      rm -f "$out.tmp"
      "$ADB" shell screencap -p /sdcard/_nv_shot.png
      "$ADB" pull /sdcard/_nv_shot.png "$out" >/dev/null
      "$ADB" shell rm -f /sdcard/_nv_shot.png
    fi
    echo "Saved $out ($(stat -c %s "$out") bytes)"
    ;;

  tap)
    require_phone >/dev/null
    "$ADB" shell input tap "$1" "$2"
    ;;

  swipe)
    require_phone >/dev/null
    "$ADB" shell input swipe "$1" "$2" "$3" "$4" "${5:-300}"
    ;;

  text)
    require_phone >/dev/null
    # adb input text needs %s for spaces
    esc="${*// /%s}"
    "$ADB" shell input text "$esc"
    ;;

  key)
    require_phone >/dev/null
    case "${1:-back}" in
      back)  k=KEYCODE_BACK ;;
      enter) k=KEYCODE_ENTER ;;
      home)  k=KEYCODE_HOME ;;
      del)   k=KEYCODE_DEL ;;
      *) echo "keys: back enter home del" >&2; exit 1 ;;
    esac
    "$ADB" shell input keyevent "$k"
    ;;

  logs)
    "$ADB" logcat -d -b crash,main 2>/dev/null | tail -"${1:-80}"
    ;;

  watch)
    "$ADB" logcat -c
    echo "Streaming $PKG logs. Press Ctrl+C to stop."
    "$ADB" logcat --pid="$("$ADB" shell pidof -s "$PKG" 2>/dev/null)" 2>/dev/null
    ;;

  clear)
    "$ADB" logcat -c
    echo "Log buffer cleared."
    ;;

  size)
    require_phone >/dev/null
    "$ADB" shell wm size
    "$ADB" shell wm density
    "$ADB" shell getprop ro.build.version.release | sed 's/^/Android /'
    "$ADB" shell getprop ro.product.model | sed 's/^/Model /'
    ;;

  uninstall)
    require_phone >/dev/null
    "$ADB" uninstall "$PKG"
    ;;

  *)
    sed -n '3,30p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
    ;;
esac
