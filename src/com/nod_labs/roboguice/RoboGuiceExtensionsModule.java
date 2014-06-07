/*
 * Copyright 2014 Nod Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nod_labs.roboguice;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.hardware.SensorManager;
import android.media.MediaRouter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import com.google.inject.AbstractModule;
import roboguice.inject.SystemServiceProvider;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RoboGuiceExtensionsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WifiP2pManager.class).toProvider(
                new SystemServiceProvider<WifiP2pManager>(Context.WIFI_P2P_SERVICE));
        bind(MediaRouter.class).toProvider(
                new SystemServiceProvider<MediaRouter>(Context.MEDIA_ROUTER_SERVICE));
        bind(BluetoothManager.class).toProvider(
                new SystemServiceProvider<BluetoothManager>(Context.BLUETOOTH_SERVICE));
        bind(BluetoothAdapter.class).toInstance(BluetoothAdapter.getDefaultAdapter());
        bind(DownloadManager.class).toProvider(
                new SystemServiceProvider<DownloadManager>(Context.DOWNLOAD_SERVICE));
    }
}
