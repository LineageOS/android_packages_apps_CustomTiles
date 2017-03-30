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
import android.os.IBinder;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

public abstract class CustomTileService extends TileService {

    private final static String TAG = "CustomTileService";

    /**
     * Return the initial state of the tile and sets it with {@see #Tile.setState()}.
     */
    public abstract int getInitialTileState();

    /*
     * STATE_ACTIVE is the default state. This causes a quick 'flash' if the state should be
     * something else and we call setState() from onStartListening(). This allows to change
     * the state as early as possible.
     */
    @Override
    public IBinder onBind(Intent intent) {
        IBinder binder = super.onBind(intent);
        int state = getInitialTileState();

        if (state == Tile.STATE_ACTIVE) {
            /* Default state, nothing to do */
            return binder;
        }

        if (state != Tile.STATE_INACTIVE && state != Tile.STATE_UNAVAILABLE) {
            Log.e(TAG, "The given state is not valid");
            return binder;
        }

        Tile tile = getQsTile();
        if (tile != null) {
            tile.setState(state);
            tile.updateTile();
        } else {
            Log.w(TAG, "Tile not available, could not set initial state");
        }

        return binder;
    }
}
