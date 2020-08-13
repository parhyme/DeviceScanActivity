package com.sample.devicescanactivity;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sample.devicescanactivity.adapters.BleDevicesAdapter;


public class DeviceScanActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 500;

    private static BleDevicesAdapter leDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Scanner scanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show();
            finish();
        }


        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_scan, menu);
        if (scanner == null || !scanner.isScanning()) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                leDeviceListAdapter.clear();
                if (scanner == null) {
                    scanner = new Scanner(bluetoothLeScanner, mScanCallback);
                    scanner.startScanning();

                    invalidateOptionsMenu();
                }
                break;
            case R.id.menu_stop:
                if (scanner != null) {
                    scanner.stopScanning();
                    scanner = null;

                    invalidateOptionsMenu();
                }
                break;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled()) {
            final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            } else {
                init();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanner != null) {
            scanner.stopScanning();
            scanner = null;
        }
    }

//    @Override
//    protected void onListItemClick(ListView l, View v, int position, long id) {
//        final BluetoothDevice device = leDeviceListAdapter.getDevice(position);
//        if (device == null)
//            return;
//
//        final Intent intent = new Intent(this, DeviceServicesActivity.class);
//        intent.putExtra(DeviceServicesActivity.EXTRAS_DEVICE_NAME, device.getName());
//        intent.putExtra(DeviceServicesActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
//        startActivity(intent);
//    }

    private void init() {
        if (leDeviceListAdapter == null) {
            leDeviceListAdapter = new BleDevicesAdapter(getBaseContext());
//            set adapter
        }

        if (scanner == null) {
            scanner = new Scanner(bluetoothLeScanner, mScanCallback);
            scanner.startScanning();
        }

        invalidateOptionsMenu();
    }


    // Device scan callback.
//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//
//                @Override
//                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            leDeviceListAdapter.addDevice(device, rssi);
//                            leDeviceListAdapter.notifyDataSetChanged();
//                        }
//                    });
//                }
//            };


    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            leDeviceListAdapter.addDevice(result.getDevice(), result.getRssi());
            leDeviceListAdapter.notifyDataSetChanged();
        }
    };

    private static class Scanner extends Thread {
        private final BluetoothLeScanner bluetoothLeScanner;
        private final ScanCallback mLeScanCallback;

        private volatile boolean isScanning = false;

        Scanner(BluetoothLeScanner adapter, ScanCallback callback) {
            bluetoothLeScanner = adapter;
            mLeScanCallback = callback;
        }

        public boolean isScanning() {
            return isScanning;
        }

        public void startScanning() {
            synchronized (this) {
                isScanning = true;
                start();
            }
        }

        public void stopScanning() {
            synchronized (this) {
                isScanning = false;
                bluetoothLeScanner.stopScan(mLeScanCallback);
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (this) {
                        if (!isScanning)
                            break;

                        bluetoothLeScanner.startScan(mLeScanCallback);
                    }

                    sleep(SCAN_PERIOD);

                    synchronized (this) {
                        bluetoothLeScanner.stopScan(mLeScanCallback);
                    }
                }
            } catch (InterruptedException ignore) {
            } finally {
                bluetoothLeScanner.startScan(mLeScanCallback);
            }
        }
    }
}