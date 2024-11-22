package com.example.arfib;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import Bio.Library.namespace.BioLib;
import java.util.HashSet;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mDeviceListAdapter;
    private ListView mDeviceListView;
    private TextView textViewStatus;
    private Set<String> mDeviceAddresses; // To filter duplicates
    private BioLib bioLib;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurement);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDeviceListView = findViewById(R.id.deviceListView);
        textViewStatus = findViewById(R.id.textViewStatus);

        // Initialize BioLib with the handler for receiving messages
       // bioLib = new BioLib(this, mHandlerUI);

        mDeviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mDeviceListView.setAdapter(mDeviceListAdapter);

        // Initialize device address set to avoid duplicate entries
        mDeviceAddresses = new HashSet<>();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {
            scanForDevices();
        }

        // Set up the listener for selecting a device from the list
        mDeviceListView.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = mDeviceListAdapter.getItem(position);
            String address = deviceInfo.split("\n")[1];  // The address is on the second line
            connectToDevice(address);
        });
    }

    // Scan for available Bluetooth devices
    private void scanForDevices() {
        mDeviceListAdapter.clear();  // Clear previous results
        textViewStatus.setText("Scanning for devices...");

        // Start discovering Bluetooth devices
        mBluetoothAdapter.startDiscovery();

        // Register for Bluetooth device found broadcasts
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    // BroadcastReceiver for detecting Bluetooth devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();  // MAC address

                // Filter out devices we've already added
                if (deviceName != null && !mDeviceAddresses.contains(deviceAddress)) {
                    mDeviceAddresses.add(deviceAddress); // Add address to the set
                    mDeviceListAdapter.add(deviceName + "\n" + deviceAddress);  // Add device to the list
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Register the broadcast receiver to receive Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the receiver when the activity is stopped
        unregisterReceiver(mReceiver);
    }

    // Method to connect to a selected Bluetooth device
    private void connectToDevice(String deviceAddress) {
        try {
            // You can also get the device object directly
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);

            // Attempt to connect to the device using BioLib
            bioLib.Connect(deviceAddress, 30);  // Connect and request QRS detection for 30 samples

            textViewStatus.setText("Connected to: " + deviceAddress);
        } catch (Exception e) {
            e.printStackTrace();
            textViewStatus.setText("Failed to connect to device");
        }
    }

    // Handler to process incoming Bluetooth messages from BioLib
    private Handler mHandlerUI = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BioLib.MESSAGE_BLUETOOTH_ENABLED:
                    textViewStatus.setText("Bluetooth Enabled");
                    break;

                case BioLib.MESSAGE_BLUETOOTH_NOT_ENABLED:
                    textViewStatus.setText("Bluetooth Not Enabled");
                    break;

                case BioLib.MESSAGE_BLUETOOTH_NOT_SUPPORTED:
                    textViewStatus.setText("Bluetooth Not Supported");
                    break;

                case BioLib.MESSAGE_DEVICE_NAME:
                    String deviceName = (String) msg.obj;
                    textViewStatus.setText("Connected to device: " + deviceName);
                    break;

                case BioLib.MESSAGE_ECG_STREAM:
                    byte[] ecgData = (byte[]) msg.obj;
                    textViewStatus.setText("ECG Data: " + ecgData.toString());
                    break;

                case BioLib.MESSAGE_PEAK_DETECTION:
                    textViewStatus.setText("QRS Peak Detected");
                    break;

                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    textViewStatus.setText("Unable to Connect to Device");
                    break;

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bioLib.Disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
