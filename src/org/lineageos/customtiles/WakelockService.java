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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;

public class WakelockService extends Service {
    private PowerManager.WakeLock wakeLock;
    private ScreenOffReceiver screenOffReceiver = new ScreenOffReceiver();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        wakeLock = createWakeLock();
        screenOffReceiver.init();
    }

    private PowerManager.WakeLock createWakeLock() {
        PowerManager.WakeLock newWakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
                .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "CaffeineTile");

        return newWakeLock;
    }

    @Override
    public void onDestroy() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        screenOffReceiver.destroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public boolean isActive() {
        return wakeLock.isHeld();
    }

    public void toggle() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        } else {
            wakeLock.acquire();
        }
    }

    public class Binder extends android.os.Binder {
        public WakelockService getService() {
            return WakelockService.this;
        }
    }

    private class ScreenOffReceiver extends BroadcastReceiver {
        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            getApplicationContext().registerReceiver(this, filter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        }

        public void destroy() {
            getApplicationContext().unregisterReceiver(this);
        }
    }
}
