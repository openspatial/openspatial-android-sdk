## Open Spatial SDK for Android

[Open Spatial](http://openspatial.net) SDK for Android contains:

| Components         | Notes
| :---------         | :----
| SDK Sources        | Sources for Open Spatial SDK APIs
| OpenSpatialSDK.jar | Used to build applications against the APIs, added for convenience
| An emulator APK    | Generates events, e.g., button, rotation events etc., to aid in application development
| An example app     | Demostrates how to use the APIs to register and log events

Sources needed to build OpenSpatialSDK.jar are in the net.openspatial package and sources for example app is in the com.example.openspatial package.

#### License
The SDK is distributed under Apache2.0 license (see LICENSE.md).

#### Build setup

Open Spatial SDK sources are currently set up to be built with [buck](http://facebook.github.io/buck).  Support for buidling with Eclipse IDE will be added in the future.

* Install Android SDK - [instructions](http://developer.android.com/sdk/index.html?hl=sk)
* Install buck - [instructions](http://facebook.github.io/buck/setup/quick_start.html)
* Set up buck with the location of Android SDK.  You can either
  - set `ANDROID_HOME` or `ANDROID_SDK` environment variables, or
  - define a local.properties file with a property named 'sdk.dir' that points to the absolute path of your Android SDK directory
  ```
  # export ANDROID_SDK=/path/to/android/sdk
    or
  # echo "sdk.dir=/path/to/android/sdk" > local.properties
  ```

#### How to build

After the one time setup, to build simply use buck to build the example app.
```
# buck build example
```

#### How to install

To install the example app on an Android device, use buck install commands or adb.

```
# buck install example
  or
# adb install buck-out/gen/apps/OpenSpatialExample/OpenSpatialExample.apk
```

To install the emulator,
```
# adb install release/OpenSpatialEmulator.apk
```

#### How to run

Install both emulator and the example app and start them from launcher.  Play with buttons, tactiles, slider etc. on the emulator app and watch logs for events.

```# adb logcat```

**NOTE**: Currently both emulator and example app run on the same Android device.  Support for them being on more than one device will be added later.

#### How to generate JavaDoc

To generate the API documentation replace `/path/to/sdk` to appropriate location and modify Android API version as needed.

```
javadoc -d release/javadoc -sourcepath src/ -classpath /path/to/sdk/platforms/android-18/android.jar net.openspatial
```
