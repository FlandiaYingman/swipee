/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.anagke;

import static android.os.SystemClock.uptimeMillis;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import android.hardware.input.InputManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Command that sends key events to the device, either by their keycode, or by
 * desired character output.
 */
@SuppressWarnings({"JavaReflectionMemberAccess", "SameParameterValue", "ConstantConditions"})
public class Swipee {

    public static final int INJECT_INPUT_EVENT_MODE_ASYNC = 0; // see InputDispatcher.h

    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = 1;  // see InputDispatcher.h

    public static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;  // see InputDispatcher.h


    private static final String TAG = "SwipeE";
    private static final String INVALID_ARGUMENTS = "Error: Invalid arguments for command: ";
    private static final Map<String, Integer> SOURCES = new HashMap<String, Integer>() {{
        put("keyboard", InputDevice.SOURCE_KEYBOARD);
        put("dpad", InputDevice.SOURCE_DPAD);
        put("gamepad", InputDevice.SOURCE_GAMEPAD);
        put("touchscreen", InputDevice.SOURCE_TOUCHSCREEN);
        put("mouse", InputDevice.SOURCE_MOUSE);
        put("stylus", InputDevice.SOURCE_STYLUS);
        put("trackball", InputDevice.SOURCE_TRACKBALL);
        put("touchpad", InputDevice.SOURCE_TOUCHPAD);
        put("touchnavigation", InputDevice.SOURCE_TOUCH_NAVIGATION);
        put("joystick", InputDevice.SOURCE_JOYSTICK);
    }};

    /**
     * Command-line entry point.
     *
     * @param args The command-line arguments
     */
    public static void main(String[] args) {
        try {
            (new Swipee()).run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run(String[] args) {
        if (args.length < 1) {
            showUsage();
            return;
        }
        int index = 0;
        String command = args[index];
        int inputSource = InputDevice.SOURCE_UNKNOWN;
        if (SOURCES.containsKey(command)) {
            inputSource = SOURCES.get(command);
            index++;
            command = args[index];
        }
        try {
            inputSource = getSource(inputSource, InputDevice.SOURCE_TOUCHSCREEN);
            float x1 = parseFloat(args[index + 1]);
            float y1 = parseFloat(args[index + 2]);
            float x2 = parseFloat(args[index + 3]);
            float y2 = parseFloat(args[index + 4]);
            float speed = parseFloat(args[index + 5]);
            switch (command) {
                case "exact":
                    sendSwipeExact(inputSource, x1, y1, x2, y2, speed);
                    return;
                case "start":
                    sendSwipeStart(inputSource, x1, y1, x2, y2, speed);
                    return;
                case "move":
                    sendSwipeMove(inputSource, x1, y1, x2, y2, speed);
                    return;
                case "end":
                    sendSwipeEnd(inputSource, x1, y1, x2, y2, speed);
                    return;
                default:
                    System.err.println("Error: Unknown command: " + command);
                    showUsage();
                    return;

            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        System.err.println(INVALID_ARGUMENTS + command);
        showUsage();
    }

    private void doSwipe(int inputSource, float x1, float y1, float x2, float y2, float speed) {
        float fullDistance = (float) sqrt(abs(x1 - x2) + abs(y1 - y2));
        for (float movedDistance = 0.0f; movedDistance < fullDistance; movedDistance += speed) {
            float alpha = movedDistance / fullDistance;
            injectMotionEvent(inputSource, ACTION_MOVE, uptimeMillis(), lerp(x1, x2, alpha), lerp(y1, y2, alpha), 1.0f);
        }
    }

    private void sendSwipeExact(int inputSource, float x1, float y1, float x2, float y2, float speed) {
        injectMotionEvent(inputSource, ACTION_DOWN, uptimeMillis(), x1, y1, 1.0f);
        doSwipe(inputSource, x1, y1, x2, y2, speed);
        injectMotionEvent(inputSource, ACTION_MOVE, uptimeMillis(), x2, y2, 1.0f);
    }

    private void sendSwipeStart(int inputSource, float x1, float y1, float x2, float y2, float speed) {
        injectMotionEvent(inputSource, ACTION_DOWN, uptimeMillis(), x1, y1, 1.0f);
        doSwipe(inputSource, x1, y1, x2, y2, speed);
        injectMotionEvent(inputSource, ACTION_MOVE, uptimeMillis(), x2, y2, 1.0f);
    }

    private void sendSwipeMove(int inputSource, float x1, float y1, float x2, float y2, float speed) {
        injectMotionEvent(inputSource, ACTION_MOVE, uptimeMillis(), x1, y1, 1.0f);
        doSwipe(inputSource, x1, y1, x2, y2, speed);
        injectMotionEvent(inputSource, ACTION_MOVE, uptimeMillis(), x2, y2, 1.0f);
    }

    private void sendSwipeEnd(int inputSource, float x1, float y1, float x2, float y2, float speed) {
        injectMotionEvent(inputSource, ACTION_MOVE, uptimeMillis(), x1, y1, 1.0f);
        doSwipe(inputSource, x1, y1, x2, y2, speed);
        injectMotionEvent(inputSource, ACTION_UP, uptimeMillis(), x2, y2, 1.0f);
    }

    private int getInputDeviceId(int inputSource) {
        final int DEFAULT_DEVICE_ID = 0;
        int[] devIds = InputDevice.getDeviceIds();
        for (int devId : devIds) {
            InputDevice inputDev = InputDevice.getDevice(devId);
            if (inputDev.supportsSource(inputSource)) {
                return devId;
            }
        }
        return DEFAULT_DEVICE_ID;
    }

    /**
     * Builds a MotionEvent and injects it into the event stream.
     *
     * @param inputSource the InputDevice.SOURCE_* sending the input event
     * @param action      the MotionEvent.ACTION_* for the event
     * @param when        the value of SystemClock.uptimeMillis() at which the event happened
     * @param x           x coordinate of event
     * @param y           y coordinate of event
     * @param pressure    pressure of event
     */
    private void injectMotionEvent(int inputSource, int action, long when, float x, float y, float pressure) {
        final float DEFAULT_SIZE = 1.0f;
        final int DEFAULT_META_STATE = 0;
        final float DEFAULT_PRECISION_X = 1.0f;
        final float DEFAULT_PRECISION_Y = 1.0f;
        final int DEFAULT_EDGE_FLAGS = 0;
        MotionEvent event = MotionEvent.obtain(when, when, action, x, y, pressure, DEFAULT_SIZE,
                DEFAULT_META_STATE, DEFAULT_PRECISION_X, DEFAULT_PRECISION_Y,
                getInputDeviceId(inputSource), DEFAULT_EDGE_FLAGS);
        event.setSource(inputSource);
        Log.i(TAG, "injectMotionEvent: " + event);

        injectInputEvent(event, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private static float lerp(float a, float b, float alpha) {
        return (b - a) * alpha + a;
    }

    private static int getSource(int inputSource, int defaultSource) {
        return inputSource == InputDevice.SOURCE_UNKNOWN ? defaultSource : inputSource;
    }

    private static void showUsage() {
        System.err.println("Usage: swipee [<source>] <command> [<arg>...]");
        System.err.println();
        System.err.println("The commands and default sources are:");
        System.err.println("\tstart <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)");
        System.err.println("\t\tStart a swipe, but don't inject the ending ACTION_UP");
        System.err.println("\tmove  <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)");
        System.err.println("\t\tStart a swipe, but inject neither the ending ACTION_UP nor the beginning ACTION_DOWN");
        System.err.println("\tend   <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)");
        System.err.println("\t\tStart a swipe, but don't inject the beginning ACTION_DOWN");
        System.err.println("\texact <x1> <y1> <x2> <y2> <speed(pixels)> (Default: touchscreen)");
        System.err.println("\t\tIdentical to 'start'.");
        System.err.println();
        System.err.println("The sources are: ");
        for (String src : SOURCES.keySet()) {
            System.err.println("      " + src);
        }
    }

    private static void injectInputEvent(InputEvent event, int mode) {
        try {
            Class<InputManager> clazz = InputManager.class;
            Method getInstance = clazz.getMethod("getInstance");
            InputManager im = (InputManager) getInstance.invoke(clazz);

            Method injectInputEvent = clazz.getMethod("injectInputEvent", InputEvent.class, int.class);
            injectInputEvent.invoke(im, event, mode);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}