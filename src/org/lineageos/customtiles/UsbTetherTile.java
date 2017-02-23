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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.Icon;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import cyanogenmod.providers.CMSettings;

public class UsbTetherTile extends TileService {

    private ConnectivityManager mConnectivityManager;

    private boolean mUsbTethered = false;
    private boolean mUsbConnected = false;

    @Override
    public void onStartListening() {
        super.onStartListening();

        mConnectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        final IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_STATE);
        registerReceiver(mReceiver, filter);

        refresh();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();

        mConnectivityManager.setUsbTethering(!mUsbTethered);

        refresh();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mUsbConnected = intent.getBooleanExtra(UsbManager.USB_CONNECTED, false);
            if (mUsbConnected && mConnectivityManager.isTetheringSupported()) {
                refresh();
            } else {
                mUsbTethered = false;
            }
            refresh();
        }
    };

    private void refresh() {
        String[] tetheredIfaces = mConnectivityManager.getTetheredIfaces();
        String[] usbRegexs = mConnectivityManager.getTetherableUsbRegexs();

        mUsbTethered = false;
        for (String s : tetheredIfaces) {
            for (String regex : usbRegexs) {
                if (s.matches(regex)) {
                    mUsbTethered = true;
                }
            }
        }

        if (mUsbTethered) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_usb_tether_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_usb_tether_off));
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
