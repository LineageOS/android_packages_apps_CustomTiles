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
import android.graphics.drawable.Icon;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class BatterySaverTile extends TileService {

    private PowerManager mPm;

    private boolean mActive = false;
    private boolean mPluggedIn;

    @Override
    public void onStartListening() {
        super.onStartListening();

        mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        registerReceiver();
        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();

        mActive = !mActive;
        mPm.setPowerSaveMode(mActive);
        refresh();
        if (!mActive) unregisterReceiver();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        unregisterReceiver();
    }

    private void unregisterReceiver() {
        unregisterReceiver(mBatteryReceiver);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGING);
        registerReceiver(mBatteryReceiver, filter);
    }

    private void refresh() {
        boolean enabled = mPm.isPowerSaveMode();
        if (mPluggedIn || !enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_battery_saver_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        } else if (enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_battery_saver_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        }
        getQsTile().updateTile();
    }

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                mPluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
            }
            refresh();
        }
    };

}
