package com.ula.bleconnet;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.List;

/**
 * Created by zrf on 2016/4/25.
 */
public class BluetoothLeService {

    public static final String BLUETOOTH_DEVICE = "Bluetooth_device";
    private static BluetoothGatt mBluetoothGatt = null;
    private Context mContext;

    private OnConnectListener mOnConnectListener;
    private OnDisconnectListener mOnDisconnectListener;
    private OnServiceDiscoverListener mOnServiceDiscoverListener;
    private OnDataAvailableListener mOnDataAvailableListener;

    public interface OnConnectListener {
        void onConnect(BluetoothGatt gatt);
    }

    public interface OnDisconnectListener {
        void onDisconnect(BluetoothGatt gatt);
    }

    public interface OnServiceDiscoverListener {
        void onServiceDiscover(BluetoothGatt gatt);
    }

    public interface OnDataAvailableListener {
        void onCharacteristicRead(BluetoothGatt gatt,
                                  BluetoothGattCharacteristic characteristic);

        void onCharacteristicWrite(BluetoothGatt gatt,
                                   BluetoothGattCharacteristic characteristic);
    }

    /**
     * when connect a remote device /the state is connected ,will trigger the listener
     *
     * @param mOnConnectListener mOnConnectListener
     */
    public void setOnConnectListener(OnConnectListener mOnConnectListener) {
        this.mOnConnectListener = mOnConnectListener;
    }

    /**
     * when disconnect a device / the state is disconnected ,will trigger the listener
     *
     * @param mOnDisconnectListener mOnDisconnectListener
     */
    public void setOnDisconnectListener(OnDisconnectListener mOnDisconnectListener) {
        this.mOnDisconnectListener = mOnDisconnectListener;
    }

    /**
     * when find service will trigger the listener
     *
     * @param mOnServiceDiscoverListener mOnServiceDiscoverListener
     */
    public void setOnServiceDiscoverListener(OnServiceDiscoverListener mOnServiceDiscoverListener) {
        this.mOnServiceDiscoverListener = mOnServiceDiscoverListener;
    }

    /**
     * when read or write data ,will trigger the listener
     *
     * @param mOnDataAvailableListener mOnDataAvailableListener
     */
    public void setOnDataAvailableListener(OnDataAvailableListener mOnDataAvailableListener) {
        this.mOnDataAvailableListener = mOnDataAvailableListener;
    }

    public BluetoothLeService(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * connect a remoteDevice callback
     */
    private BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mBluetoothGatt.discoverServices();
                if (mOnConnectListener != null) {
                    mOnConnectListener.onConnect(mBluetoothGatt);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //  2016/4/25 if disconnect ,trigger mOnDisconnectListener
                if (mOnDisconnectListener != null) {
                    mOnDisconnectListener.onDisconnect(mBluetoothGatt);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS && mOnServiceDiscoverListener != null) {
                mOnServiceDiscoverListener.onServiceDiscover(gatt);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status==BluetoothGatt.GATT_SUCCESS && mOnDataAvailableListener != null) {
                mOnDataAvailableListener.onCharacteristicRead(gatt, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (mOnDataAvailableListener != null) {
                mOnDataAvailableListener.onCharacteristicWrite(gatt,
                        characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (mOnDataAvailableListener != null) {
                mOnDataAvailableListener.onCharacteristicWrite(gatt,
                        characteristic);
                mOnDataAvailableListener.onCharacteristicRead(gatt,characteristic);
            }
        }

        /*
      * 读描述值
      * */
        @Override
        public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        /*
      * 写描述值
      * */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };


    /**
     * close the BluetoothGatt
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
//        mBluetoothGatt = null;
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic gattCharacterisitic, boolean enable) {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(gattCharacterisitic, enable);
        BluetoothGattDescriptor descriptor = gattCharacterisitic.getDescriptor(BLECommunicateConstant.CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }


    /**
     * connect a device
     *
     * @param device bleDevice
     * @return if connect successful return true ,else return false
     */
    public boolean connect(final BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        close();
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallBack);
        return true;
    }

    /**
     * disconnect
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * read
     *
     * @param characteristic characteristic
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }
}
