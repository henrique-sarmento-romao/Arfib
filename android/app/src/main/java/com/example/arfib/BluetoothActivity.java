package com.example.arfib;

import Bio.Library.namespace.BioLib;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice selectedDevice;
    private BioLib biolib;
    private TextView textStatus, textHR, textSignal;
    private Button buttonConnect;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TARGET_DEVICE_MAC = "00:23:FE:00:0B:59"; // Replace with your device MAC address

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurement);

        textStatus = findViewById(R.id.textViewStatus);
        textHR = findViewById(R.id.textViewHR);
        textSignal = findViewById(R.id.textViewSignal);
        buttonConnect = findViewById(R.id.buttonConnect);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            textStatus.setText("Bluetooth not supported on this device.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // List paired devices and allow selection
        buttonConnect.setOnClickListener(v -> selectDeviceAndConnect());
    }

    private void selectDeviceAndConnect() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(TARGET_DEVICE_MAC)) { // Replace with your device MAC address
                    selectedDevice = device;
                    break;
                }
            }

            if (selectedDevice != null) {
                connectToDevice();
            } else {
                Toast.makeText(this, "Device not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            textStatus.setText("No paired devices found.");
        }
    }

    private void connectToDevice() {
        try {
            // Assuming BioLib expects a String (the MAC address) for connection
            biolib = new BioLib(this, mHandler);

            // Pass the MAC address (String) instead of the BluetoothDevice object
            biolib.Connect(selectedDevice.getAddress(),5);

            textStatus.setText("Connecting to " + selectedDevice.getName() + "...");
        } catch (Exception e) {
            textStatus.setText("Failed to connect to device.");
            e.printStackTrace();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BioLib.STATE_CONNECTED:
                    textStatus.setText("Connected to " + selectedDevice.getName());
                    break;



                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    textStatus.setText("Unable to connect to device.");
                    break;

                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    textStatus.setText("Device disconnected.");
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };
}
