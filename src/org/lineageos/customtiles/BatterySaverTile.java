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
import android.util.Log;

public class BatterySaverTile extends CustomTileService {

    private static final String TAG = BatterySaverTile.class.getSimpleName();

    private boolean mEnabled;

    /* Assume unplugged */
    private boolean mPluggedIn = false;

    private PowerManager mPm;

    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                boolean pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;
                refresh(pluggedIn);
            }
        }
    };

    @Override
    public void onCreate() {
        mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mEnabled = mPm.isPowerSaveMode();
    }

    private boolean isPluggedIn() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        registerReceiver();
        refresh(isPluggedIn());
    }

    @Override
    public void onStopListening() {
        unregisterReceiver();
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();

        boolean ret = mPm.setPowerSaveMode(!mEnabled);
        if (!ret) {
            Log.e(TAG, "Could not set power mode");
            return;
        }
        refresh(mPluggedIn);
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

    private void refresh(boolean pluggedIn) {
        boolean enabled = mPm.isPowerSaveMode();
        if (enabled == mEnabled && pluggedIn == mPluggedIn) {
            return;
        }
        mEnabled = enabled;
        mPluggedIn = pluggedIn;
        if (mPluggedIn) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_battery_saver_off));
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
        } else if (!enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_battery_saver_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_battery_saver_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public int getInitialTileState() {
        return mEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public Icon getInitialIcon() {
        return mEnabled ? Icon.createWithResource(this, R.drawable.ic_battery_saver_on) : null;
    }
}
