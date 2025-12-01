@echo off
adb logcat -c
adb logcat System.out:I *:S
