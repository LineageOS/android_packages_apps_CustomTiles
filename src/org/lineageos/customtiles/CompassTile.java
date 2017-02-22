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

import android.content.Context;
import android.graphics.drawable.Icon;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.ImageView;

public class CompassTile extends TileService implements SensorEventListener {
    private final static float ALPHA = 0.97f;

    private boolean mActive = false;
    private boolean mListening = false;
    private boolean mListeningSensors;

    private SensorManager mSensorManager;
    private Sensor mAccelerationSensor;
    private Sensor mGeomagneticFieldSensor;

    private Float mDegrees;

    private float[] mAcceleration;
    private float[] mGeomagnetic;

    private ImageView mImage;

    @Override
    public void onStartListening() {
        super.onStartListening();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGeomagneticFieldSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mImage = new ImageView(this);
        mListening = true;
        setListeningSensors(mActive && mListening);
        refresh();
    }

    @Override
    public void onClick() {
        super.onClick();
        mActive = !mActive;
        refresh();
        setListeningSensors(mActive);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        setListeningSensors(false);
        mSensorManager = null;
        mImage = null;
    }

    private void setListeningSensors(boolean listening) {
        if (listening == mListeningSensors) return;
        mListeningSensors = listening;
        if (mListeningSensors) {
            mSensorManager.registerListener(
                    this, mAccelerationSensor, SensorManager.SENSOR_DELAY_GAME);
            mSensorManager.registerListener(
                    this, mGeomagneticFieldSensor, SensorManager.SENSOR_DELAY_GAME);
        } else {
            mSensorManager.unregisterListener(this);
        }
    }

    private void refresh() {
        if (mActive && mListeningSensors) {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_compass_on));
            getQsTile().setState(Tile.STATE_ACTIVE);
            if (mDegrees != 0) {
                getQsTile().setLabel(formatValueWithCardinalDirection(mDegrees));
                float target = 360 - mDegrees;
                float relative = target - mImage.getRotation();
                if (relative > 180) {
                    relative -= 360;
                }
            } else {
                getQsTile().setLabel(getString(R.string.quick_settings_compass_init));
                mImage.setRotation(0);
            }
        } else {
            getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_compass_off));
            getQsTile().setLabel(getString(R.string.compass_label));
            getQsTile().setState(Tile.STATE_INACTIVE);
            mImage.setRotation(0);
        }
        getQsTile().updateTile();
    }

    private String formatValueWithCardinalDirection(float degree) {
        int cardinalDirectionIndex = (int) (Math.floor(((degree - 22.5) % 360) / 45) + 1) % 8;
        String[] cardinalDirections = getResources().getStringArray(
                R.array.cardinal_directions);

        return getString(R.string.quick_settings_compass_value, degree,
                cardinalDirections[cardinalDirectionIndex]);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (mAcceleration == null) {
                mAcceleration = event.values.clone();
            }

            values = mAcceleration;
        } else {
            // Magnetic field sensor
            if (mGeomagnetic == null) {
                mGeomagnetic = event.values.clone();
            }

            values = mGeomagnetic;
        }

        for (int i = 0; i < 3; i++) {
            values[i] = ALPHA * values[i] + (1 - ALPHA) * event.values[i];
        }

        if (!mActive || !mListeningSensors || mAcceleration == null || mGeomagnetic == null) {
            // Nothing to do at this moment
            return;
        }

        float R[] = new float[9];
        float I[] = new float[9];
        if (!SensorManager.getRotationMatrix(R, I, mAcceleration, mGeomagnetic)) {
            // Rotation matrix couldn't be calculated
            return;
        }

        // Get the current orientation
        float[] orientation = new float[3];
        SensorManager.getOrientation(R, orientation);

        // Convert azimuth to degrees
        Float newDegree = Float.valueOf((float) Math.toDegrees(orientation[0]));
        newDegree = (newDegree + 360) % 360;

        mDegrees = newDegree;
        refresh();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
