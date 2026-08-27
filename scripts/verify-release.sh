#!/bin/sh
set -eu

apk=${1:?usage: verify-release.sh APK}
test -f "$apk"
bytes=$(wc -c < "$apk" | tr -d ' ')
test "$bytes" -le 512000 || { echo "APK exceeds 500 KiB: $bytes bytes" >&2; exit 1; }

sdk_root=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}}
aapt_bin=$(find "$sdk_root/build-tools" -type f -name aapt 2>/dev/null | sort | tail -n 1)
test -x "$aapt_bin" || aapt_bin=$(command -v aapt)
permissions=$($aapt_bin dump permissions "$apk")
printf '%s\n' "$permissions"
printf '%s\n' "$permissions" | grep -q 'android.permission.INTERNET' && {
  echo 'INTERNET permission is forbidden' >&2
  exit 1
}
echo "Verified: $bytes bytes, no INTERNET permission"
