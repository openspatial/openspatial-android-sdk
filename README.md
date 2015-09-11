## Open Spatial SDK for Android

[Open Spatial](http://openspatial.net) SDK for Android contains:

| Components         | Notes
| :---------         | :----
| SDK Sources        | Sources for OpenSpatial SDK APIs
| OpenSpatialSDK.jar | Used to build applications against the APIs (added for convenience)
| An example app     | Demonstrates how to use the APIs to register and log events

Sources needed to build the OpenSpatialSDK.jar are in the net.openspatial package. Sources for the example app are in the com.example.openspatial package.

#### License
The SDK is distributed under Apache2.0 license (see LICENSE.md).

#### Build setup

OpenSpatial SDK sources are built with [buck](http://facebook.github.io/buck).

* Install Android SDK - [instructions](http://developer.android.com/sdk/index.html?hl=sk)
* Install Oracle JDK 7 - [instructions](http://docs.oracle.com/javase/7/docs/webnotes/install/index.html)
* Install Python 2.7 - [instructions](https://www.python.org/download/releases/2.7.7/)
* Install Ant - [instructions](http://ant.apache.org/bindownload.cgi)
* Install git - [instructions](http://git-scm.com/downloads)
* Install buck - [instructions](http://facebook.github.io/buck/setup/quick_start.html)

Set the locations of various tools (in the instructions below, replace "path_to_android_sdk" and "path_to_openspatial_sdk" with the actual pathname where you unpacked the Android and OpenSpatial SDKs):

  ```
  $ export ANDROID_SDK=path_to_android_sdk
  $ export PATH=$PATH:$ANDROID_SDK/sdk/platform-tools
  $ export OPENSPATIAL_SDK=path_to_openspatial_sdk
  ```

#### How to build

After the one time setup, use buck to build the example app.

```
$ cd $OPENSPATIAL_SDK
$ buck build example
```

#### How to install

Connect the host computer to the Android device using a USB cable.

To install the example app on an Android device, use the "buck install" or the "adb install" command.

```
$ buck install example
  or
$ adb install buck-out/gen/apps/OpenSpatialExample/OpenSpatialExample.apk
```

#### How to generate JavaDoc

To generate the API documentation, use the command below, modifying the Android API version as needed.

```
$ javadoc -d release/javadoc -sourcepath src/ -classpath $ANDROID_SDK/platforms/android-18/android.jar net.openspatial
```

## Open Spatial SDK for iOS

For information on Open Spatial SDK for iOS see (https://github.com/openspatial/iOS-SDK)
