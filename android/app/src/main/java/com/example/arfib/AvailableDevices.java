package com.example.arfib;

import Bio.Library.namespace.BioLib;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Set;

public class AvailableDevices extends Activity {

    public static final String SELECT_DEVICE_ADDRESS = "device_address";
    public static final int CHANGE_MACADDRESS = 100;

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
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(SELECT_DEVICE_ADDRESS, selectedValue);

                // Set result and finish this Activity
                setResult(CHANGE_MACADDRESS, intent);
                finish();
            }
        });

        try {
            mainListView = findViewById(R.id.lstDevices);
            ArrayList<String> lstDevices = new ArrayList<>();

            // Create ArrayAdapter using the list of devices
            listAdapter = new ArrayAdapter<>(this, android.R.layout.list_content, lstDevices);

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                if (mBluetoothAdapter.isEnabled()) {
                    // Listing paired devices
                    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice device : devices) {
                        listAdapter.add(device.getAddress() + "   " + device.getName());
                    }
                }
            }

            mainListView.setAdapter(listAdapter);

            mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                    selectedValue = listAdapter.getItem(position);

                    if (selectedValue != null) {
                        String[] aux = selectedValue.split("   ");
                        selectedValue = aux[0];
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
