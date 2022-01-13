# Swipe Enhanced (swipee)

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
# I choose to push it under /sdcard. You can put it wherever you prefer.
adb push ./app/build/dex/release/swipee.jar /sdcard/swipee.jar
```

Run it using `app_process`!

```shell
# Run this in Adb Shell! 
app_process -Djava.class.path=/sdcard/swipee.jar /system/bin top.anagke.Swipee end [<source>] <command> [<arg>...]

# Usage: app_process [java-options] cmd-dir start-class-name

# Usage: swipee [<source>] <command> [<arg>...]
#
#        start <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)
#                Start a swipe, but don't inject the ending ACTION_UP
#        move  <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)
#                Start a swipe, but inject neither the ending ACTION_UP nor the beginning ACTION_DOWN
#        end   <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)
#                Start a swipe, but don't inject the beginning ACTION_DOWN
#        exact <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)
#                Identical to 'start'.
```

You don't need to always end `swipee start` with `swipee end`. You can end it with a `input tap` or other touchscreen
event.