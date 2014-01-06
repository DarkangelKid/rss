SimpleRss
===

News Aggregator for Android 4.+.

To clone the repository:
```
   https://github.com/poloure/rss.git
```

To build the code using Gradle:
```
   cd rss
   ./gradlew assembleDebug
```

To Install the apk to a device connected with adb:
```
   adb install -r SimpleRss/build/apk/SimpleRss-debug-unaligned.apk
```

If you experience any crashes, connect to usb enable USB Debugging in
Androids Settings:Developer options and use:
```
   adb logcat
```

If you do not have Developer options, rapidly tap
Settings:About phone:Build number until you are one.

This will print out lots of text to the terminal. Reproduce the crash
while running this and include the log in a git issue with instructions
on how to reproduce and which device you are using.
