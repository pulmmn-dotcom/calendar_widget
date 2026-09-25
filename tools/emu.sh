#!/usr/bin/env bash
# 가상 폰(에뮬레이터) 미리보기 도우미. 화면은 1080x2400 픽셀 기준 좌표를 쓴다.
# 사용법: bash tools/emu.sh <명령> [인자]
export ANDROID_HOME="/c/Android/sdk"; export ANDROID_SDK_ROOT="/c/Android/sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"
PKG=com.pulmm.shiftcalendar
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
case "$1" in
  boot)    # 가상 폰이 꺼져 있으면 켠다 (약 80초)
           if adb get-state 2>/dev/null | grep -q device; then echo "이미 켜져 있음"; exit 0; fi
           nohup emulator -avd pixel_test -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect >/dev/null 2>&1 &
           for i in $(seq 1 60); do [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && echo "부팅 완료" && exit 0; sleep 5; done; echo "부팅 실패"; exit 1;;
  install) adb install -r "$APK";;
  fresh)   adb uninstall $PKG >/dev/null 2>&1; adb install "$APK" && adb shell am start -n $PKG/.MainActivity >/dev/null && sleep 3 && bash "$0" seed;;
  start)   adb shell am start -n $PKG/.MainActivity >/dev/null; sleep 3;;
  seed)    adb shell am broadcast -a com.pulmm.shiftcalendar.DEBUG_SEED -n $PKG/$PKG.debug.DebugSeedReceiver >/dev/null; sleep 3; adb shell am force-stop $PKG;;
  home)    adb shell input keyevent KEYCODE_HOME; sleep 1;;
  pin)     # pin day|week|small|month : 홈화면에 위젯 추가 (확인 창의 'Add to home screen'까지 누름)
           adb shell input keyevent KEYCODE_HOME; sleep 1
           adb shell am start -n $PKG/.debug.DebugPinWidgetActivity --es type "$2" >/dev/null; sleep 3
           adb shell input tap 830 2254; sleep 3;;
  shot)    adb exec-out screencap -p > "$2"; echo "$2";;
  tap)     adb shell input tap "$2" "$3"; sleep 1;;
  swipe)   adb shell input swipe "$2" "$3" "$4" "$5" "${6:-300}"; sleep 1;;
  back)    adb shell input keyevent KEYCODE_BACK; sleep 1;;
  *)       echo "명령: boot install fresh start seed home pin shot tap swipe back";;
esac
