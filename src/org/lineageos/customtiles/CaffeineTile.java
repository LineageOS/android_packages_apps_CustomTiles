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

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Icon;
import android.os.IBinder;
import android.os.UserHandle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class CaffeineTile extends TileService {
    private WakelockService wakelockService;
    private ServiceConnection serviceConnection;

    @Override
    public void onStartListening() {
        super.onStartListening();

        startServiceAsUser(new Intent(getApplicationContext(), WakelockService.class),
                UserHandle.CURRENT);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                wakelockService = ((WakelockService.Binder) iBinder).getService();
                refresh();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                wakelockService = null;
                refresh();
            }
        };
        bindServiceAsUser(new Intent(getApplicationContext(), WakelockService.class),
                serviceConnection, 0, UserHandle.CURRENT);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();

        if (wakelockService != null && !wakelockService.isActive()) {
            stopServiceAsUser(new Intent(getApplicationContext(), WakelockService.class),
                    UserHandle.CURRENT);
        }

        unbindService(serviceConnection);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();

        stopServiceAsUser(new Intent(getApplicationContext(), WakelockService.class),
                UserHandle.CURRENT);
    }

    @Override
    public void onClick() {
        super.onClick();

        if (wakelockService != null) {
            wakelockService.toggle();
        }

        refresh();
    }

    private void refresh() {
        getQsTile().setLabel(getString(R.string.caffeine_label));
        if (wakelockService != null && wakelockService.isActive()) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_caffeine_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_caffeine_off));
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }
}
