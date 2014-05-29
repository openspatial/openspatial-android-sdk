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

To install the Bluetooth connector (more on this later)
```
# adb install release/OpenSpatialBluetoothConnector.apk
```

#### How to run

In order to run the emulator and the app, you will need two Android devices. One device will run the example app and a 'connector' app, the other will run the emulator. Let's call the device running the example app the target device and the device running the emulator the source. To run the setup, follow the steps below:

1. Pair the source and target devices.
2. Install the example app and the BluetoothConnector app on the target device.
3. Install the emulator on the source device.
4. Start the connector app on the target device from the launcher first, then launch the example app.
5. Start the Emulator on the source device. Select the target device from the device list.
6. Play with buttons, tactiles, slider etc. on the emulator app and watch logs for events using ```adb logcat```

#### How to generate JavaDoc

To generate the API documentation replace `/path/to/sdk` to appropriate location and modify Android API version as needed.

```
javadoc -d release/javadoc -sourcepath src/ -classpath /path/to/sdk/platforms/android-18/android.jar net.openspatial
```

## Open Spatial SDK for iOS

For information on Open Spatial SDK for iOS see (https://github.com/openspatial/iOS-SDK)
