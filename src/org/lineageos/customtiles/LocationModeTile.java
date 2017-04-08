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
import android.location.LocationManager;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class LocationModeTile extends TileService {

    private int mLocationMode;

    private int[] locationModes = new int[] {
            Settings.Secure.LOCATION_MODE_OFF,
            Settings.Secure.LOCATION_MODE_SENSORS_ONLY,
            Settings.Secure.LOCATION_MODE_BATTERY_SAVING,
            Settings.Secure.LOCATION_MODE_HIGH_ACCURACY};

    /**
     * Broadcast receiver for updating the tile.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh(getLocationMode());
        }
    };

    @Override
    public void onStartListening() {
        super.onStartListening();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);

        refresh(getLocationMode());
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        int nextLocationMode = getNextLocationMode();
        refresh(nextLocationMode);
    }

    /**
     * Gets the next location mode, when clicking on the tile
     *
     * @return value of the next location mode
     */
    private int getNextLocationMode() {
        return locationModes[(mLocationMode + 1) % locationModes.length];
    }

    /**
     * Method to refresh the file. Validates if tile needs to be updated.
     *
     * @param locationMode current location mode
     */
    private void refresh(int locationMode) {
        if(locationMode != mLocationMode) {
            mLocationMode = locationMode;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, mLocationMode);
            updateLocationModeTile(locationMode);
        }
    }

    /**
     * Update location mode tile with the current location
     *
     * @param currentLocationMode the current location mode
     */
    private void updateLocationModeTile(int currentLocationMode) {
        switch (currentLocationMode) {
            case Settings.Secure.LOCATION_MODE_OFF:
                updateTile(Icon.createWithResource(this,
                        R.drawable.ic_location_mode_off),
                        Tile.STATE_ACTIVE,
                        getString(R.string.location_mode_off_label));
                break;
            case Settings.Secure.LOCATION_MODE_SENSORS_ONLY:
                updateTile(Icon.createWithResource(this,
                        R.drawable.ic_location_mode_do),
                        Tile.STATE_ACTIVE,
                        getString(R.string.location_mode_do_label));
                break;
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
                updateTile(Icon.createWithResource(this,
                        R.drawable.ic_location_mode_bs),
                        Tile.STATE_ACTIVE,
                        getString(R.string.location_mode_bs_label));
                break;
            case Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:
                updateTile(Icon.createWithResource(this,
                        R.drawable.ic_location_mode_ha),
                        Tile.STATE_ACTIVE,
                        getString(R.string.location_mode_ha_label));
                break;
        }
    }

    /**
     * Update the Tile
     *
     * @param icon icon
     * @param state state of the tile
     * @param label tile label
     */
    private void updateTile(Icon icon, int state, String label) {
        getQsTile().setIcon(icon);
        getQsTile().setState(state);
        getQsTile().setLabel(label);
        getQsTile().updateTile();
    }

    /**
     * Get the current location mode
     *
     * @return location mode
     */
    private int getLocationMode() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
    }

}
