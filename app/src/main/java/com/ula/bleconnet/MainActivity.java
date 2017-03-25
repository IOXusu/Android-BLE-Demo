package com.ula.bleconnet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import static com.ula.bleconnet.BLECommunicateConstant.gattCharacteristic_charA;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 100;
    private Handler mHandler;
    // 蓝牙扫描时间
    private static final long SCAN_PERIOD = 10000;
    private static final String BLUETOOTH_NAME = "HC-08";//Find Me
    private boolean mScanning = false;

    private BluetoothLeService mBLEService;
    public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initPermission();
    }

    private void init() {
        mHandler = new Handler();
        mBLEService = new BluetoothLeService(this);
        BLECommunicateUtils.setBLEService(mBLEService);
        mBLEService.setOnServiceDiscoverListener(mOnServiceDiscover);
        mBLEService.setOnDataAvailableListener(mOnDataAvailable);
        mBLEService.setOnDisconnectListener(mOnDisconnectListener);
        mBLEService.setOnConnectListener(mOnConnectListener);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            //拒绝
            return;
        }

        if (requestCode == REQUEST_ENABLE_BT) {
            //允许
            scanLeDevice(true);
        }
    }


    private void initPermission() {
        // 手机硬件支持蓝牙
        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes Bluetooth adapter.
        // 获取手机本地的蓝牙适配器
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // 打开蓝牙权限
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            //蓝牙可用
            scanLeDevice(true);
        }
    }

    /**
     * @param enable (扫描使能，true:扫描开始,false:扫描停止)
     * @return void
     * @throws
     * @Title: scanLeDevice
     * @Description: TODO(扫描蓝牙设备)
     */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if (mBLEService != null) {
                        mBLEService.close();
                    }
                    Log.e(TAG, "扫描时间结束,停止扫描");
                }
            }, SCAN_PERIOD);
            /* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.e(TAG, "开始扫描");
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            Log.e(TAG, "停止扫描");
        }
    }

    /**
     * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
     **/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             byte[] scanRecord) {

            if (device.getName() != null && device.getName().equals(BLUETOOTH_NAME)) {
                Log.e(TAG, "扫描得到我的设备");
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                mBLEService.connect(device);
            }
        }
    };


    /**
     * get the supported characteristics , maybe need to change
     *
     * @param gattServices gattServices
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (gattCharacteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT)) {

                    // 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBLEService.setCharacteristicNotification(
                            gattCharacteristic, true);
                }
                gattCharacteristic_charA = gattCharacteristic;
            }
        }

        BLECommunicateUtils.sendData(gattCharacteristic_charA, StringUtils.
                hexStringToBytes("***********发送的指令************"));
    }


    /**
     * find the service listener
     */
    private BluetoothLeService.OnServiceDiscoverListener mOnServiceDiscover = new BluetoothLeService.OnServiceDiscoverListener() {

        @Override
        public void onServiceDiscover(BluetoothGatt gatt) {
            displayGattServices(mBLEService.getSupportedGattServices());
        }
    };

    /**
     * read and write listener
     */
    private BluetoothLeService.OnDataAvailableListener mOnDataAvailable = new BluetoothLeService.OnDataAvailableListener() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "收到了数据" + characteristic.toString());
            //断开连接
            if (mBLEService != null) {
                mBLEService.close();
            }
            Log.e(TAG, "断开连接");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, "发送出去了数据");
        }
    };

    /**
     * connect a device listener
     */
    private BluetoothLeService.OnConnectListener mOnConnectListener = new BluetoothLeService.OnConnectListener() {
        @Override
        public void onConnect(BluetoothGatt gatt) {
            Log.e(TAG, "已成功连接到设备");
        }
    };

    /**
     * disconnect a device listener
     */
    private BluetoothLeService.OnDisconnectListener mOnDisconnectListener = new BluetoothLeService.OnDisconnectListener() {
        @Override
        public void onDisconnect(BluetoothGatt gatt) {
            Log.e(TAG, "没有连接到设备");
        }
    };


}
