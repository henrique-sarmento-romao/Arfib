package com.example.arfib.Measurements;

import Bio.Library.namespace.BioLib;
import android.Manifest;
import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.R;
import android.graphics.Color;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private DatabaseHelper dbHelper;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TARGET_DEVICE_MAC = "00:23:FE:00:0B:50"; // Replace with your device MAC address
    private ArrayList<Integer> hrValues = new ArrayList<>(); // List to store HR values
    private ArrayList<Integer> ecgData = new ArrayList<>(); // List to store ECG data


    private static String[] PERMISSIONS_STORAGE = {
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            permission.BLUETOOTH_SCAN,
            permission.BLUETOOTH_CONNECT,
            permission.BLUETOOTH_PRIVILEGED,
            permission.BLUETOOTH_ADVERTISE

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
            Toast.makeText(this, "Bluetooth is off. Enabling...", Toast.LENGTH_LONG).show();
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
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.2f", value / 500); // Divide o índice pela frequência de amostragem
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false); // Disable right axis

        Legend legend = chart.getLegend();
        legend.setEnabled(true);
    }

    private void checkPermissions() {
        int permission1 = ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_SCAN);
        int permission3 = ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_CONNECT);

        // Request permissions if not granted
        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED || permission3 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
        }
    }


    private void selectDeviceAndConnect() {
        // Clear data before starting a new connection
        hrValues.clear();
        ecgData.clear();

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        boolean devicePaired = false;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(TARGET_DEVICE_MAC)) {
                    selectedDevice = device;
                    devicePaired = true;
                    break;
                }
            }
        }

        if (!devicePaired) {
            // Dispositivo não emparelhado
            textStatus.setText("Device not paired. Attempting to pair...");
            selectedDevice = bluetoothAdapter.getRemoteDevice(TARGET_DEVICE_MAC);
            if (selectedDevice != null) {
                pairDevice(selectedDevice);
            } else {
                textStatus.setText("Device with MAC address " + TARGET_DEVICE_MAC + " not found.");
            }
        } else if (selectedDevice != null) {
            textStatus.setText("Selected device: " + selectedDevice.getName());
            connectToDevice();
        } else {
            textStatus.setText("Device not found in paired devices.");
        }
    }

    private void pairDevice(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Para Android 12+, é necessário solicitar permissão para emparelhamento
            if (ActivityCompat.checkSelfPermission(this, permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission.BLUETOOTH_ADVERTISE}, 2);
                return;
            }
        }

        try {
            boolean bondInitiated = device.createBond();
            if (bondInitiated) {
                textStatus.setText("Paired with " + device.getName());
            } else {
                textStatus.setText("Pairing failed to initiate with " + device.getName());
            }
        } catch (Exception e) {
            textStatus.setText("Error initiating pairing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void connectToDevice() {
        try {
            if (selectedDevice != null) {
                biolib = new BioLib(this, mHandler);
                biolib.Connect(selectedDevice.getAddress(), 30);
                textStatus.setText("Connecting to " + selectedDevice.getName() + "...");

                // Schedule disconnection after 60 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        biolib.Disconnect();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error in disconnecting", Toast.LENGTH_LONG).show();
                    }
                }, 60000); // 60 seconds in milliseconds


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
                    activity.processData();

                    break;

                case BioLib.MESSAGE_DATA_UPDATED:
                    BioLib.Output out = (BioLib.Output) msg.obj;
                    activity.textHR.setText("HR: " + out.pulse + " bpm     Nb. Leads: " + activity.biolib.GetNumberOfChannels());
                    if (out.pulse > 0) {
                        activity.hrValues.add(out.pulse);
                    }
                    break;

                case BioLib.MESSAGE_PEAK_DETECTION:
                    BioLib.QRS qrs = (BioLib.QRS) msg.obj;
                    activity.textECG2.setText("PEAK: " + qrs.position + "  BPMi: " + qrs.bpmi + " bpm  BPM: " + qrs.bpm + " bpm  R-R: " + qrs.rr + " ms");
                    break;

                case BioLib.MESSAGE_ECG_STREAM:
                    try {
                        if (msg.obj != null && msg.obj instanceof byte[][]) {
                            activity.ecg = (byte[][]) msg.obj;
                            int nLeads = activity.ecg.length;
                            int nBytes = activity.ecg[0].length;
                            activity.textECG.setText("ECG stream: OK   nBytes: " + nBytes + "   nLeads: " + nLeads);

                            // Converter os dados para o gráfico
                            ArrayList<Entry> ecgEntries = new ArrayList<>();
                            for (int i = 0; i < Math.min(nBytes, 150); i++) { // Limite de 300 pontos
                                int value = activity.ecg[0][i] & 0xFF;
                                ecgEntries.add(new Entry(i, value));
                            }

                            // Atualiza os valores na stream
                            for (int i = 0; i < nBytes; i++) {
                                int value = activity.ecg[0][i] & 0xFF;
                                activity.ecgData.add(value);
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

    private void processData() {
        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            // Export data to file
            exportDataToFile();

            // Clear the data
            textStatus.setText("Data collection complete. HR samples: " + hrValues.size() + ", ECG samples: " + ecgData.size());
            hrValues.clear();
            ecgData.clear();
        } catch (IOException e) {
            throw new RuntimeException("Error initializing database", e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing data", e);
        }
    }

    private void exportDataToFile() {
        // Criar um nome de arquivo único com base no timestamp
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Arfib");
        if (!directory.exists()) {
            directory.mkdirs(); // Cria o diretório se não existir
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String filename = "patient_data_" + timestamp + ".txt";
        File file = new File(directory, filename);
        try (FileWriter writer = new FileWriter(file)) {
            // Escrever os valores de ECG
            for (Integer value : ecgData) { // Iterar diretamente sobre ecgData
                writer.write(value + " ");
            }

            Toast.makeText(this, "Data exported to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Salvar informações do arquivo no banco de dados
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String username = sharedPref.getString("username", "");

            // Salvar informações no banco de dados
            dbHelper.insertFile(file.getAbsolutePath(), username, this);
        } catch (IOException e) {
            Toast.makeText(this, "Error exporting data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}




