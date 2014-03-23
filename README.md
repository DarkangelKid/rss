Download
===

You can find Simple RSS on the [F-Droid repository](https://f-droid.org/).

Or if you prefer to not use the F-Droid client, you can [download](https://f-droid.org/repository/browse/?fdfilter=simple%20rss&fdid=com.poloure.simplerss) the apk from their repository.

Screenshots
===

Click for full size images.

[![ScreenShot](screenshots/thumbnails/one_preview.png)](https://raw.github.com/poloure/rss/master/screenshots/one.png)
[![ScreenShot](screenshots/thumbnails/two_preview.png)](https://raw.github.com/poloure/rss/master/screenshots/two.png)
[![ScreenShot](screenshots/thumbnails/three_preview.png)](https://raw.github.com/poloure/rss/master/screenshots/three.png)

[![ScreenShot](screenshots/thumbnails/four_preview.png)](https://raw.github.com/poloure/rss/master/screenshots/four.png)
[![ScreenShot](screenshots/thumbnails/five_preview.png)](https://raw.github.com/poloure/rss/master/screenshots/five.png)
[![ScreenShot](screenshots/thumbnails/six_preview.png)](https://raw.github.com/poloure/rss/master/screenshots/six.png)


Permissions
===
```
    <uses-permission android:name="android.permission.INTERNET"/>
```

INTERNET - To download RSS feeds and images from the feeds.

```
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
```

WAKE_LOCK - To update feeds when the app is closed mid update and when scheduled to update (can be completely disabled
in settings).

```
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```

WRITE_EXTERNAL_STORAGE - Only used when user clicks save image (to external storage).

Code Style
===

Use Allman indent style with four space indents (not tabs). No spaces after method names, and
use right hand comparisons (to avoid x = 3 typos).


    while(3 == x)
    {
        method(x, 6 > x);
    }

Building
===

Make sure to have the Android SDK Platform API 19 installed.

To build the code using Gradle:

```
    ./gradlew assembleDebug
```

Install the apk to a device connected with adb:

```
    adb install -r build/apk/SimpleRss-debug-unaligned.apk
```