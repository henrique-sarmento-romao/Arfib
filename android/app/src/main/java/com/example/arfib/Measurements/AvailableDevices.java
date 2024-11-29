package com.example.arfib.Measurements;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.arfib.R;

import java.util.ArrayList;
import java.util.Set;

public class AvailableDevices extends Activity {

    public static final String SELECT_DEVICE_ADDRESS = "device_address";
    public static final int CHANGE_MACADDRESS = 100;
    private static final int REQUEST_BLUETOOTH_CONNECT = 1;

    private ListView mainListView;
    private ArrayAdapter<String> listAdapter;
    private String selectedValue = "";
    private BluetoothAdapter mBluetoothAdapter = null;
    private Button buttonOK;

    /**
     * Returns the selected MAC address of the Bluetooth device.
     */
    public String getMacAddress() {
        return selectedValue;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        buttonOK = findViewById(R.id.cmdOK);
        buttonOK.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra(SELECT_DEVICE_ADDRESS, selectedValue);

            setResult(CHANGE_MACADDRESS, intent);
            finish();
        });

        mainListView = findViewById(R.id.lstDevices);
        ArrayList<String> lstDevices = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lstDevices);
        mainListView.setAdapter(listAdapter);

        mainListView.setOnItemClickListener((parent, item, position, id) -> {
            selectedValue = listAdapter.getItem(position);
            if (selectedValue != null) {
                String[] aux = selectedValue.split("   ");
                selectedValue = aux[0];
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show();
            return;
        }

       listPairedDevices();
    }

    private void listPairedDevices() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is disabled. Please enable it.", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        if (devices != null && !devices.isEmpty()) {
            for (BluetoothDevice device : devices) {
                listAdapter.add(device.getAddress() + "   " + device.getName());
            }
        } else {
            Toast.makeText(this, "No paired Bluetooth devices found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_CONNECT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listPairedDevices();
            } else {
                Toast.makeText(this, "Permission denied. Cannot list Bluetooth devices.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
