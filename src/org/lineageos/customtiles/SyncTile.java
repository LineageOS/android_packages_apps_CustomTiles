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
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class SyncTile extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();

        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();

        ContentResolver.setMasterSyncAutomatically(getQsTile().getState() == Tile.STATE_INACTIVE);
        refresh();
    }

    private void refresh() {
        boolean enabled = ContentResolver.getMasterSyncAutomatically();
        if (enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_sync_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_sync_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

}
