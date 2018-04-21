package com.example.simplebluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class ControlActivity extends AppCompatActivity {
    private final static String TAG = ControlActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    //private TextView mConnectionState;
    private TextView mDataField;
    private GraphView mGraph;
    private BluetoothGattCharacteristic tempChar;
    private String mDeviceName;
    private String mDeviceAddress;
    //private ExpandableListView mGattServicesList;
    private BTService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private int communicationState;
    private long diffDate;
    private int pointsCounter = 0;
    private DataPoint[] points;
    private ArrayList<Model> productList;
    private listviewAdapter lvadapter;
    private ListView lview;
    private LinearLayout mTableHeader;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        // Sets up UI references.
        //mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        //mGattServicesList.setOnChildClickListener(servicesListClickListner);
        //mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mDataField.setVisibility(View.VISIBLE);
        mGraph = (GraphView) findViewById(R.id.graph);
        mGraph.setVisibility(View.INVISIBLE);
        mTableHeader = (LinearLayout) findViewById(R.id.table_header);
        mTableHeader.setVisibility(View.INVISIBLE);
        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        Intent gattServiceIntent = new Intent(this, BTService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        diffDate = 0;
        communicationState = 0;
        points = new DataPoint[512];
        // Bottom table
        productList = new ArrayList<Model>();
        lview = (ListView) findViewById(R.id.list_view);
        lview.setVisibility(View.INVISIBLE);
        lvadapter = new listviewAdapter(this, productList);
        lview.setAdapter(lvadapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case R.id.menu_help:
                showHelp();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BTService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BTService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.control_connected);
                invalidateOptionsMenu();
                // Start communicate with the TI board
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                communicationState = 0;
                                mBluetoothLeService.writeCustomCharacteristic(0x1);
                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                Log.i("tag", "Ask the board to send us his local time");
                                                mBluetoothLeService.readCustomCharacteristic(); // The board will now send us his local time
                                            }
                                        }, 300);
                            }
                        }, 700);
            } else if (BTService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.control_disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BTService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BTService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BTService.EXTRA_DATA));
            }
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if (mGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                final int charaProp = characteristic.getProperties();
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, false);
                        mNotifyCharacteristic = null;
                    }
                    mBluetoothLeService.readCharacteristic(characteristic);
                }
                if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    mNotifyCharacteristic = characteristic;
                    mBluetoothLeService.setCharacteristicNotification(characteristic, true);
                }
                return true;
            }
            return false;
        }
    };

    private void clearUI() {
        //mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.control_loading);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(Integer.toString(resourceId));
            }
        });
    }

    private long str2long(String data) {
        if (data.length() == 2) { // "00"
            String p0 = data.substring(0,2);
            return Long.parseLong(p0,16);
        } else if (data.length() == 11) { // "0c 01 00 00"
            String p0 = data.substring(9,11);
            String p1 = data.substring(6,8);
            String p2 = data.substring(3,5);
            String p3 = data.substring(0,2);
            return Long.parseLong(p0+p1+p2+p3,16);
        } else {
            return 0;
        }
    }

    private void displayData(String data) {
        if (data != null) {
            Date dateEpoch;
            switch (communicationState) {
                case 0: // New communication
                    communicationState = 1;
                    dateEpoch = new Date(str2long(data.substring(0,11)) * 1000); // Read the board data
                    Calendar calendar = Calendar.getInstance();
                    Date currentDate = calendar.getTime();
                    diffDate = currentDate.getTime() - dateEpoch.getTime();
                    mBluetoothLeService.writeCustomCharacteristic(0x2); // Ask the board to send his samples
                    new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                communicationState = 2;
                                mBluetoothLeService.readCustomCharacteristic();
                                Log.i("tag", "Read first samples packet");
                            }
                        }, 300);
                    break;
                case 2:
                    if (data.length() == 60) { // data = "D4 01 00 00 00 1D 86 CB 3F 00 E8 01 00 00 00 82 D4 C9 3F 00 "
                        long t0 = str2long(data.substring(0, 11)); // "D4 01 00 00" => 468
                        long z0 = str2long(data.substring(12, 14)); // "00" => 0
                        long s0 = str2long(data.substring(15, 26)); // "1D 86 CB 3F" => 1070302749
                        Float f0 = Float.intBitsToFloat((int)s0); // int(1070302749) == float(1.5900303)
                        long z1 = str2long(data.substring(27, 29)); // "00" => 0
                        long t1 = str2long(data.substring(30, 41)); // "E8 01 00 00" => 488
                        long z2 = str2long(data.substring(42, 44)); // "00" => 0
                        long s1 = str2long(data.substring(45, 56)); // "82 D4 C9 3F" => 1070191746
                        Float f1 = Float.intBitsToFloat((int)s1); // int(1070191746) == float(1.5767977)
                        long z3 = str2long(data.substring(57, 59)); // "00" => 0
                        if ((z0 == 0)&&(z1 == 0)&&(z2 == 0)&&(z3 == 0)) {
                            if (t0 != 0) {
                                dateEpoch = new Date(t0 * 1000);
                                points[pointsCounter++] = new DataPoint(dateEpoch.getTime() + diffDate, f0);
                                if (t1 != 0) {
                                    dateEpoch = new Date(t1 * 1000);
                                    points[pointsCounter++] = new DataPoint(dateEpoch.getTime() + diffDate, f1);
                                    mBluetoothLeService.readCustomCharacteristic(); // Read next samples
                                } else { // Close communication
                                    communicationState = 3;
                                    mBluetoothLeService.writeCustomCharacteristic(0x3); // Finish communication
                                    createGraph(); // Create the graph
                                }
                            } else { // Close communication
                                communicationState = 3;
                                mBluetoothLeService.writeCustomCharacteristic(0x3); // Finish communication
                                createGraph(); // Create the graph
                            }
                        }
                    } else {
                        Log.i("tag", "Got invalid string at stage 2");
                    }
                    break;
            }
        } else {
            Log.i("tag", "Got empty string");
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, gattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, gattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        //mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BTService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BTService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BTService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void showHelp() {
        final Intent intent = new Intent(ControlActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    public void createGraph() {
        // generate Dates
        final DataPoint[] finalPoints;
        if (pointsCounter == 0) {
            finalPoints = new DataPoint[1];
            finalPoints[0] = new DataPoint(1.0, 1514757600);
        } else {
            finalPoints = new DataPoint[pointsCounter];
            for (int i=0;i<pointsCounter;i++) {
                finalPoints[i] = new DataPoint(i, points[i].getY());
                productList.add(new Model(i, points[i].getX(), points[i].getY()));
            }
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(finalPoints);
        // set manual X,Y bounds
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setMinY(1.0);
        mGraph.getViewport().setMaxY(14.0);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(pointsCounter);
        // enable scaling and scrolling
        mGraph.getViewport().setScalable(true);
        // Create the graph
        mGraph.addSeries(series);
        // set date label formatter
        mGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) { // show custom label for x values
                    return super.formatLabel(value, isValueX);
                } else { // show normal y values
                    return super.formatLabel(value, isValueX);
                }
            }
        });
        // Show it to the user
        mDataField.setText("");
        mDataField.setVisibility(View.INVISIBLE);
        mGraph.setVisibility(View.VISIBLE);
        mTableHeader.setVisibility(View.VISIBLE);
        lview.setVisibility(View.VISIBLE);
        lvadapter.notifyDataSetChanged();
    }
}
