package com.niusounds.vsaplayer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun SensorManager.orientationFlow(): Flow<SensorEvent> = callbackFlow {

    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            trySend(event)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    val sensor = getDefaultSensor(Sensor.TYPE_ORIENTATION)
    registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

    awaitClose {
        unregisterListener(listener, sensor)
    }
}