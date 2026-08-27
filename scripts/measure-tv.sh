#!/bin/sh
set -eu

serial=${1:-192.168.1.67:5555}
package=io.github.ozkanceng.ozlauncher
mkdir -p performance-results
adb -s "$serial" shell am force-stop "$package"
adb -s "$serial" shell am start -W -a android.intent.action.MAIN -c android.intent.category.HOME "$package/.LauncherActivity" | tee performance-results/startup.txt
sleep 10
adb -s "$serial" shell dumpsys meminfo "$package" | tee performance-results/meminfo.txt
adb -s "$serial" shell top -b -n 1 | grep "$package" | tee performance-results/top.txt || true
