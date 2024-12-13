package com.example.arfib.Measurements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Detailed extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LineChart ecgChart;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurementdetailed);

        ecgChart = findViewById(R.id.chartECG_detailed);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Measurement");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");

        Intent previousIntent = getIntent();
        String date = previousIntent.getStringExtra("date");
        String time = previousIntent.getStringExtra("time");

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(Detailed.this, HomePatient.class);
            startActivity(intent);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationsButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Detailed.this, Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(Detailed.this, Log.class);
            startActivity(intent);
        });


        dbHelper = new DatabaseHelper(this);
        List<String> measurementData = null;
        String has_AF = null;
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Measurement WHERE patient='" + username + "' AND date='" + date + "' AND time='" + time + "' ORDER BY time DESC", null);

            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("file"));
                int AF_presence = cursor.getInt(cursor.getColumnIndex("AF_presence"));
                has_AF = (AF_presence == 1) ? "AF Detected" : "No AF Detected";
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        TextView Date, AFpresent, Observations;

        Date = findViewById(R.id.date);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormatterDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("HH:mm");

        String date_time = "";
        try {
            Date parsedDate = dateFormatter.parse(date);
            Date parsedTime = timeFormatter.parse(time);

            String formattedDate = outputFormatterDate.format(parsedDate);
            String formattedTime = outputFormatterTime.format(parsedTime);

            // Final combined output
            date_time = formattedDate + ", " + formattedTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date.setText(date_time);

        AFpresent = findViewById(R.id.AF_presence);
        AFpresent.setText(has_AF);

        Observations = findViewById(R.id.observation);
        Observations.setText("No observations");

        configureChart(ecgChart);
        ArrayList<Entry> data = readTxtFile(path);
        updateChart(ecgChart, data);

    }


    // Graphical representation of the ECG

    private void configureChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setVisibleXRangeMaximum(1000); // number of points
        chart.setDragDecelerationEnabled(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        chart.setVisibleXRangeMaximum(1000);  // Limita a visualização a 1000 pontos no eixo X
        chart.setVisibleXRangeMinimum(1);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(-1.15f); // Mínimo fixo
        leftAxis.setAxisMaximum(1.15f);  // Máximo fixo
        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }
    private void updateChart(LineChart chart, ArrayList<Entry> data) {
        // Data mapping
        float minIn = 0f;
        float maxIn = 230f;   // Maximum of bit
        float minOut = -1.15f; // Min in mV (Y)
        float maxOut = 1.15f;  // Max in mV (Y)

        ArrayList<Entry> mappedData = new ArrayList<>();

        for (Entry entry : data) {
            float mappedX = mapToTime(entry.getX(), 500);  // sample rate=500

            // Map Y (ECG) to mV
            float mappedY = mapToMV(entry.getY(), minIn, maxIn, minOut, maxOut);

            mappedData.add(new Entry(mappedX, mappedY));
        }

        LineDataSet dataSet = new LineDataSet(mappedData, "ECG Data");
        int color = ContextCompat.getColor(this, R.color.hartpink);
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }
    private float mapToMV(float x, float minIn, float maxIn, float minOut, float maxOut) {
        return minOut + ((x - minIn) * (maxOut - minOut)) / (maxIn - minIn);
    }

    private float mapToTime(float x, float sampleRate) {

        return x / sampleRate;
    }

    private ArrayList<Entry> readTxtFile(String filePath) {

        ArrayList<Entry> data = new ArrayList<>();
        File file = new File(getFilesDir()+ File.separator + filePath);

        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int index = 0;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(" ");
                    for (String value : values) {
                        try {
                            float yValue = Float.parseFloat(value.trim());
                            data.add(new Entry(index++, yValue));
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Error processing: " + value, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error reading file: " + filePath, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "File not found: " + filePath, Toast.LENGTH_LONG).show();
        }

        return data;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, tente ler o arquivo novamente
                ArrayList<Entry> data = readTxtFile(path);
            } else {
                Toast.makeText(this, "Permissão negada para ler o armazenamento externo", Toast.LENGTH_LONG).show();
            }
        }
    }




}
