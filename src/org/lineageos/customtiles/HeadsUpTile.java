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

import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;

public class HeadsUpTile extends CustomTileService {

    private boolean mEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
        mEnabled = isHeadsUpEnabled();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();

        Settings.Global.putInt(getContentResolver(),
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED,
                getQsTile().getState() == Tile.STATE_INACTIVE ? 1 : 0);
        refresh();
    }

    private boolean isHeadsUpEnabled() {
        return Settings.Global.HEADS_UP_OFF != Settings.Global.getInt(getContentResolver(),
                Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, Settings.Global.HEADS_UP_OFF);
    }

    private void refresh() {
        boolean enabled = isHeadsUpEnabled();
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        if (enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_heads_up_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_heads_up_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public int getInitialTileState() {
        return mEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public Icon getInitialIcon() {
        return mEnabled ? null : Icon.createWithResource(this, R.drawable.ic_heads_up_off);
    }
}
