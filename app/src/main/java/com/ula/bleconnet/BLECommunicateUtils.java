package com.ula.bleconnet;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by zrf on 2016/4/27.
 */
public class BLECommunicateUtils {


    public static BluetoothLeService mBLEService = null;

    /**
     * After new a bleService ,must call the method
     *
     * @param mBLEService mBLEService
     */
    public static void setBLEService(final BluetoothLeService mBLEService) {
        BLECommunicateUtils.mBLEService = mBLEService;
    }

    public static BluetoothLeService getBLEService() {
        return mBLEService;
    }

    /**
     * receive data / read data
     *
     * @param gattCharacteristic gattCharacteristic
     */
    public static void receiveData(BluetoothGattCharacteristic gattCharacteristic) {
        if (mBLEService != null && gattCharacteristic != null) {
            mBLEService.readCharacteristic(gattCharacteristic);
        }
    }

    /**
     * send data/ write data
     *
     * @param gattCharacteristic gattCharacteristic
     * @param bytes              bytes
     */
    public static void sendData(BluetoothGattCharacteristic gattCharacteristic, byte[] bytes) {
        gattCharacteristic.setValue(bytes);
        gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        mBLEService.writeCharacteristic(gattCharacteristic);
    }


    /**
     * send data
     *
     * @param gattCharacteristic gattCharacteristic
     * @param str                str
     */
    public static void sendData(BluetoothGattCharacteristic gattCharacteristic, String str) {
        if (mBLEService != null && gattCharacteristic != null) {
            gattCharacteristic.setValue(str);
            mBLEService.writeCharacteristic(gattCharacteristic);
        }
    }

    /**
     * when read or write data ,will trigger the listener
     *
     * @param onDataAvailableListener onDataAvailableListener
     */
    public static void setOnCharacteristicDataListener(BluetoothLeService.OnDataAvailableListener onDataAvailableListener) {
        if (mBLEService != null) {
            mBLEService.setOnDataAvailableListener(onDataAvailableListener);
        }
    }

    /**
     * when connect a remote device /the state is connected ,will trigger the listener
     *
     * @param onConnectListener onConnectListener
     */
    public static void setOnConnectListener(BluetoothLeService.OnConnectListener onConnectListener) {
        if (mBLEService != null) {
            mBLEService.setOnConnectListener(onConnectListener);
        }
    }

    /**
     * when disconnect a remote device /the state is disconnected ,will trigger the listener
     *
     * @param onDisconnectListener OnDisconnectListener
     */
    public static void setOnDisConnectListener(BluetoothLeService.OnDisconnectListener onDisconnectListener) {
        if (mBLEService != null) {
            mBLEService.setOnDisconnectListener(onDisconnectListener);
        }
    }
}
