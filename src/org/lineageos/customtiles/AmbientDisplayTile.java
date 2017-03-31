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

public class AmbientDisplayTile extends CustomTileService {

    private boolean mEnabled;

    @Override
    public void onCreate() {
        super.onCreate();
        mEnabled = isDozeEnabled();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.DOZE_ENABLED, mEnabled ? 0 : 1);
        refresh();
    }

    private boolean isDozeEnabled() {
        return Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.DOZE_ENABLED, 1) != 0;
    }

    private void refresh() {
        boolean enabled = isDozeEnabled();
        if (mEnabled == enabled) {
            return;
        }
        mEnabled = enabled;
        if (enabled) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_ambient_display_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_ambient_display_off));
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
        return mEnabled ? null : Icon.createWithResource(this, R.drawable.ic_ambient_display_off);
    }
}
