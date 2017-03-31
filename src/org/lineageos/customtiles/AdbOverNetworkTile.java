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

import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.NetworkUtils;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.service.quicksettings.Tile;

import java.net.InetAddress;

import cyanogenmod.providers.CMSettings;

public class AdbOverNetworkTile extends CustomTileService {

    private boolean mEnabled = false;
    private String mLabel = null;

    private WifiManager mWifiManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mLabel = getString(R.string.network_adb_label);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        refresh();
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

    private void refresh() {
        boolean enabled = isAdbNetworkEnabled();
        String label;

        if (enabled) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                InetAddress address = NetworkUtils.intToInetAddress(wifiInfo.getIpAddress());
                label = address.getHostAddress();
            } else {
                label = getString(R.string.network_adb_label);
            }
        } else {
            label = getString(R.string.network_adb_label);
        }

        if (enabled == mEnabled && label.equals(mLabel)) {
            return;
        }

        if (enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_network_adb_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().setLabel(label);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_network_adb_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
            getQsTile().setLabel(label);
        }
        getQsTile().updateTile();

        mEnabled = enabled;
        mLabel = label;
    }

    private boolean isAdbNetworkEnabled() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0 &&
                CMSettings.Secure.getInt(getContentResolver(), CMSettings.Secure.ADB_PORT, 0) > 0;
    }

    @Override
    public int getInitialTileState() {
        return isAdbNetworkEnabled() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public Icon getInitialIcon() {
        return isAdbNetworkEnabled() ?
                Icon.createWithResource(this, R.drawable.ic_network_adb_on) : null;
    }
}
