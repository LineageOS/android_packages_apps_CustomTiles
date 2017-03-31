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

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

public abstract class CustomTileService extends TileService {

    private final static String TAG = "CustomTileService";

    /**
     * Return the new initial state of the tile. See {@see #Tile.setState()}.
     */
    public int getInitialTileState() {
        return Tile.STATE_ACTIVE;
    }

    /**
     * Return the new initial icon of the tile, or null if no change is required.
     */
    public Icon getInitialIcon() {
        return null;
    }

    /**
     * Return the new initial label of the tile, or null if no change is required.
     */
    public String getInitialLabel() {
        return null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = super.onBind(intent);

        // The Tile object is created by super.onBind(). This might change in future,
        // so make sure we get a valid object here.
        Tile tile = getQsTile();
        if (tile == null) {
            Log.w(TAG, "Tile not available, could not set initial state");
            return binder;
        }

        boolean updateNeeded = false;

        int state = getInitialTileState();
        if (state == Tile.STATE_ACTIVE || state == Tile.STATE_INACTIVE ||
                state == Tile.STATE_UNAVAILABLE) {
            if (state != Tile.STATE_ACTIVE) {
                tile.setState(state);
                updateNeeded = true;
            }
        } else {
            Log.e(TAG, "The given state is not valid");
        }

        Icon icon = getInitialIcon();
        if (icon != null) {
            tile.setIcon(icon);
            updateNeeded = true;
        }

        String label = getInitialLabel();
        if (label != null) {
            tile.setLabel(label);
            updateNeeded = true;
        }

        if (updateNeeded) {
            tile.updateTile();
        }

        return binder;
    }
}
