package com.example.simplebluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;
import java.util.LinkedHashSet;

public class MainActivity extends AppCompatActivity {
    // GUI Components
    private TextView mBluetoothStatus;
    private Button mDiscoverBtn;
    private BluetoothAdapter mAdapter;
    public Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Declare vars
        Button mScanBtn;
        Button mOffBtn;
        Button mListPairedDevicesBtn;
        ListView mDevicesListView;
        // Init vars
        setContentView(com.example.simplebluetooth.R.layout.activity_main);
        mBluetoothStatus = findViewById(com.example.simplebluetooth.R.id.bluetoothStatus);
        mScanBtn = findViewById(com.example.simplebluetooth.R.id.scan);
        mOffBtn = findViewById(com.example.simplebluetooth.R.id.off);
        mDiscoverBtn = findViewById(com.example.simplebluetooth.R.id.discover);
        mListPairedDevicesBtn =  findViewById(com.example.simplebluetooth.R.id.PairedBtn);
        mAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio
        mBTArrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        mDevicesListView = findViewById(com.example.simplebluetooth.R.id.devicesListView);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);
        getSupportActionBar().setIcon(R.drawable.ic_action_bluetooth);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Ask for location permission if not already allowed
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        if (mBTArrayAdapter == null) { // Device does not support Bluetooth
            mBluetoothStatus.setText(getString(com.example.simplebluetooth.R.string.status_not_found));
            Toast.makeText(getApplicationContext(),getString(com.example.simplebluetooth.R.string.toast_not_found),Toast.LENGTH_SHORT).show();
        } else {
            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothOn();
                }
            });
            mOffBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {bluetoothOff();}
            });
            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){listPairedDevices();}
            });
            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover();
                }
            });
        }
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) { // Check which request we're responding to
            case REQUEST_ENABLE_BT: // When the request to enable Bluetooth returns
                if (resultCode == RESULT_OK) { // Make sure the request was successful
                    mBluetoothStatus.setText(getString(com.example.simplebluetooth.R.string.status_enabled));
                } else {
                    mBluetoothStatus.setText(getString(com.example.simplebluetooth.R.string.status_disabled));
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelDiscovery();
        mBTArrayAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mAdapter.isEnabled()) {
            bluetoothOn();
        }
        if (!mAdapter.isDiscovering()) {
            discover();
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            if (!mAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), getString(R.string.toast_bt_not_on), Toast.LENGTH_SHORT).show();
                return;
            }
            mBluetoothStatus.setText(getString(R.string.status_connecting));
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            final String deviceAddress = info.substring(info.length() - 17);
            final String deviceName = info.substring(0,info.length() - 17).trim();
            // Send it to the next activity
            final Intent intent = new Intent(MainActivity.this, ControlActivity.class);
            intent.putExtra(ControlActivity.EXTRAS_DEVICE_NAME, deviceName);
            intent.putExtra(ControlActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
            cancelDiscovery();
            startActivity(intent);
        }
    };

    private void bluetoothOn() {
        if (!mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText(getString(com.example.simplebluetooth.R.string.status_on));
            Toast.makeText(getApplicationContext(),getString(com.example.simplebluetooth.R.string.toast_bt_on),Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),getString(com.example.simplebluetooth.R.string.toast_bt_was_on), Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff() {
        cancelDiscovery();
        mAdapter.disable(); // turn off
        mBluetoothStatus.setText(getString(com.example.simplebluetooth.R.string.status_off));
        Toast.makeText(getApplicationContext(),getString(com.example.simplebluetooth.R.string.toast_bt_off), Toast.LENGTH_SHORT).show();
    }

    private void discover() {
        // Check if the device is already discovering
        if (mAdapter.isDiscovering()) {
            cancelDiscovery();
        } else {
            if(mAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mAdapter.startDiscovery();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND)); // Register for broadcasts when a device is discovered.
                mDiscoverBtn.setText(getString(com.example.simplebluetooth.R.string.btn_discover_on));
                Toast.makeText(getApplicationContext(), getString(com.example.simplebluetooth.R.string.toast_scan_started), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), getString(com.example.simplebluetooth.R.string.toast_bt_not_on), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // Discovery has found a device
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // Get the BluetoothDevice object from the Intent
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                removeArrayAdapterDuplication();
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void cancelDiscovery() {
        try {
            unregisterReceiver(blReceiver);
        } catch (Exception e){
            // already unregistered
        }
        mAdapter.cancelDiscovery();
        mDiscoverBtn.setText(getString(com.example.simplebluetooth.R.string.btn_discover_off));
        Toast.makeText(getApplicationContext(),getString(com.example.simplebluetooth.R.string.toast_scan_stopped),Toast.LENGTH_SHORT).show();
    }

    private void listPairedDevices() {
        mBTArrayAdapter.clear(); // clear items
        mPairedDevices = mAdapter.getBondedDevices();
        if (mAdapter.isEnabled()) {
            if (mPairedDevices.size() > 0) { // There are paired devices.
                for (BluetoothDevice device : mPairedDevices) {
                    mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                removeArrayAdapterDuplication();
                Toast.makeText(getApplicationContext(), getString(com.example.simplebluetooth.R.string.toast_show_paired), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(com.example.simplebluetooth.R.string.toast_bt_not_on), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeArrayAdapterDuplication() {
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        for (int i=0 ; i<mBTArrayAdapter.getCount() ; i++) {
            linkedHashSet.add(mBTArrayAdapter.getItem(i));
        }
        mBTArrayAdapter.clear();
        mBTArrayAdapter.addAll(linkedHashSet);
    }
}
