package com.example.arfib.Measurements;

import Bio.Library.namespace.BioLib;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.arfib.R;
import android.graphics.Color;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import com.github.mikephil.charting.data.Entry;


import java.lang.ref.WeakReference;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice selectedDevice;
    private BioLib biolib;
    private TextView textStatus, textHR, textSignal;
    private Button buttonConnect;
    private TextView textECG;
    private TextView textECG2;
    private byte[][] ecg = new byte[0][0]; // Initialized as an empty array
    private int nBytes = 0;
    private LineChart ecgChart;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TARGET_DEVICE_MAC = "00:23:FE:00:0B:50"; // Replace with your device MAC address

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurement);

        // Initialize UI components
        textStatus = findViewById(R.id.textViewStatus);
        textHR = findViewById(R.id.textViewHR);
        textSignal = findViewById(R.id.textViewSignal);
        textECG = findViewById(R.id.textECG);
        textECG2 = findViewById(R.id.textECG2);
        buttonConnect = findViewById(R.id.buttonConnect);
        ecgChart = findViewById(R.id.chartECG);

        // Check for necessary permissions
        checkPermissions();

        try {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Bluetooth is not supported on this device.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred while accessing Bluetooth: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (bluetoothAdapter == null) {
            textStatus.setText("Bluetooth not supported on this device.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            textStatus.setText("Bluetooth is off. Enabling...");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Set the connect button listener
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectDeviceAndConnect();
            }
        });

        // Configure the ECG chart
        configureChart(ecgChart);
    }

    private void configureChart(LineChart chart) {
        chart.getDescription().setEnabled(false); // Disable description
        chart.setExtraOffsets(10, 10, 10, 10); // Extra margins
        chart.setDrawGridBackground(false); // No grid background
        chart.setTouchEnabled(true); // Allow touch
        chart.setDragEnabled(true); // Allow drag
        chart.setScaleEnabled(true); // Allow zoom
        chart.setPinchZoom(true); // Enable pinch-to-zoom

        // Configure axes
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false); // Disable right axis

        Legend legend = chart.getLegend();
        legend.setEnabled(false); // Hide legend
    }

    private void checkPermissions() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        int permission3 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);

        // Request permissions if not granted
        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED || permission3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
    }

    private void selectDeviceAndConnect() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(TARGET_DEVICE_MAC)) {
                    selectedDevice = device;
                    break;
                }
            }

            if (selectedDevice != null) {
                textStatus.setText("Selected device: " + selectedDevice.getName());
                connectToDevice();
            } else {
                textStatus.setText("Device not found in paired devices.");
            }
        } else {
            textStatus.setText("No paired devices found.");
        }
    }

    private void connectToDevice() {
        try {
            if (selectedDevice != null) {
                biolib = new BioLib(this, mHandler);
                biolib.Connect(selectedDevice.getAddress(), 30); // Timeout value assumed to be 30 seconds
                textStatus.setText("Connecting to " + selectedDevice.getName() + "...");
            } else {
                textStatus.setText("Selected device is null.");
            }
        } catch (Exception e) {
            textStatus.setText("Failed to connect to device.");
            e.printStackTrace();
        }
    }
    private void updateChart(LineChart chart, ArrayList<Entry> data) {
        LineDataSet dataSet = new LineDataSet(data, "ECG Data");
        dataSet.setColor(Color.CYAN);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false); // Sem círculos nos pontos
        dataSet.setDrawValues(false); // Sem valores nos pontos
        dataSet.setMode(LineDataSet.Mode.LINEAR); // Linear para dados ECG

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Atualizar o gráfico
    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<BluetoothActivity> activityReference;

        // Constructor receives a reference to the Activity
        MyHandler(BluetoothActivity activity) {
            super(Looper.getMainLooper()); // Explicitly associate with the main Looper
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BluetoothActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) {
                return; // The Activity has been destroyed, do nothing
            }

            switch (msg.what) {
                case BioLib.STATE_CONNECTED:
                    activity.textStatus.setText("Connected to " + activity.selectedDevice.getName());
                    break;

                case BioLib.UNABLE_TO_CONNECT_DEVICE:
                    activity.textStatus.setText("Unable to connect to device.");
                    break;

                case BioLib.MESSAGE_DISCONNECT_TO_DEVICE:
                    activity.textStatus.setText("Device disconnected.");
                    break;

                case BioLib.MESSAGE_DATA_UPDATED:
                    BioLib.Output out = (BioLib.Output) msg.obj;
                    activity.textHR.setText("HR: " + out.pulse + " bpm     Nb. Leads: " + activity.biolib.GetNumberOfChannels());
                    break;

                case BioLib.MESSAGE_PEAK_DETECTION:
                    BioLib.QRS qrs = (BioLib.QRS) msg.obj;
                    activity.textHR.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms");
                    break;

                case BioLib.MESSAGE_ECG_STREAM:
                    try {
                        if (msg.obj != null && msg.obj instanceof byte[][]) {
                            activity.ecg = (byte[][]) msg.obj;
                            int nLeads = activity.ecg.length;
                            int nBytes = activity.ecg[0].length;

                            activity.textECG.setText("ECG stream: OK   nBytes: " + nBytes + "   nLeads: " + nLeads);
                            // Convertendo os dados para o gráfico
                            ArrayList<Entry> ecgEntries = new ArrayList<>();
                            for (int i = 0; i < Math.min(nBytes, 300); i++) { // Limite de 300 pontos
                                int value = activity.ecg[0][i] & 0xFF; // Pegando o primeiro lead (ajustável)
                                ecgEntries.add(new Entry(i, value));
                            }

                            // Atualizar o gráfico
                            activity.updateChart(activity.ecgChart, ecgEntries);
                        } else {
                            activity.textECG.setText("Error: ECG data not received or unexpected format.");
                        }
                    } catch (Exception ex) {
                        activity.textECG.setText("Error: " + ex.getMessage());
                    }
                    break;


                default:
                    super.handleMessage(msg);
            }
        }
    }
}


