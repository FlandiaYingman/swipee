# Swipe Enhanced (swipee)

An enhanced replacement for `adb shell input swipe`.

Since the original `input` executable so simple that it doesn't support complex swipes, `swipee` is out! It provides
different commands that support complexer swipes continuously instead of segmenting your swipes.

# Usage

First, compile as dex file in a jar.

```shell
./gradlew compileReleaseDex
# or
# ./gradlew compileDebugDex
```

Then, push it into your device.

```shell
# Push to /data/local/tmp/,
# because you have full permission to execute files under /data/local/tmp
adb push ./app/build/dex/release/swipee.jar /data/local/tmp/swipee.jar
```

Run it using `app_process`!

```shell
# Run this in an Adb Shell! 
app_process -Djava.class.path=/data/local/tmp/swipee.jar /system/bin top.anagke.Swipee [<source>] <command> [<arg>...]

# Usage: app_process [java-options] cmd-dir start-class-name

# Usage: swipee [<source>] <command> [<arg>...]
#     start <x1> <y1> <x2> <y2> <speed(pixels per event)> (Default: touchscreen)
#         Start a swipe, but don't inject the ending ACTION_UP
#     move  <x1> <y1> <x2> <y2> <speed(pixels per event)> (Default: touchscreen)
#         Start a swipe, but inject neither the ending ACTION_UP nor the beginning ACTION_DOWN
#     end   <x1> <y1> <x2> <y2> <speed(pixels per event)> (Default: touchscreen)
#         Start a swipe, but don't inject the beginning ACTION_DOWN
#     exact <x1> <y1> <x2> <y2> <speed(pixels per event)> (Default: touchscreen)
#         Identical to 'start'.
```

You don't need to always end `swipee start` with `swipee end`. You can end it with a `input tap` or other touchscreen
event.

Note that the most significant usage difference between `swipee` and `input swipe` is that, `swipee` accepts a
required `speed` argument at the last while `input swipe` accepts a optional `duration` argument. The reason why I
change this is that, comparing to speed, accuracy is more important to this tool. And you don't need to calculate
different duration for different swiping length. (The recommend speed for `swipee` is ~1)

# License

This project is licensed under Apache License, version 2.0.

The source code of this project is derived from the original AOSP's *Input*, which can be found
at [here](https://android.googlesource.com/platform/frameworks/base/+/android-4.4.2_r1/cmds/input/src/com/android/commands/input/Input.java)
.

```shell
adb push ./app/build/dex/release/swipee.jar /data/local/tmp/swipee.jar && 
adb shell "app_process -Djava.class.path=/data/local/tmp/swipee.jar /system/bin top.anagke.Swipee"
```