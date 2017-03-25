package com.ula.bleconnet;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

/**
 * Created by zrf on 2016/4/26.
 */
public class BLECommunicateConstant {
    //after connect success ，notify remotedevice
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static BluetoothGattCharacteristic gattCharacteristic_charA = null;

}
