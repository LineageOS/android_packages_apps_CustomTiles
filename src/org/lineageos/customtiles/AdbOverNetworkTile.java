/*
 * Copyright (C) 2017 The LineageOS Project
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
package org.lineageos.customtiles;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import java.net.InetAddress;

import cyanogenmod.providers.CMSettings;

public class AdbOverNetworkTile extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        getContentResolver().registerContentObserver(
                CMSettings.Secure.getUriFor(CMSettings.Secure.ADB_PORT),
                false, mObserver);
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(Settings.Global.ADB_ENABLED),
                false, mObserver);

        refresh();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onClick() {
        super.onClick();

        CMSettings.Secure.putIntForUser(getContentResolver(),
                CMSettings.Secure.ADB_PORT,
                getQsTile().getState() == Tile.STATE_ACTIVE ? -1 : 5555,
                UserHandle.USER_CURRENT);

        refresh();
    }

    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            refresh();
        }
    };

    private void refresh() {
        if (isAdbEnabled() && isAdbNetworkEnabled()) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null) {
                // if wifiInfo is not null, set the label to "hostAddress"
                InetAddress address = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
                getQsTile().setLabel(address.getHostAddress());
            } else {
                // if wifiInfo is null, set the label without host address
                getQsTile().setLabel(getString(R.string.network_adb_label));
            }

            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_network_adb_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setLabel(this.getString(R.string.network_adb_label));
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_network_adb_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    private boolean isAdbEnabled() {
        return Settings.Global.getInt(getContentResolver(),
                Settings.Global.ADB_ENABLED, 0) > 0;
    }

    private boolean isAdbNetworkEnabled() {
        return CMSettings.Secure.getInt(getContentResolver(),
                CMSettings.Secure.ADB_PORT, 0) > 0;
    }

}
