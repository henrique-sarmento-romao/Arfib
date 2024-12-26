package com.example.arfib.Measurements;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.Users.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.Users.HomeDoctor;
import com.example.arfib.Users.HomeNurse;
import com.example.arfib.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Details extends AppCompatActivity {

    private DatabaseHelper dbHelper; // Helper class for database interactions
    private LineChart ecgChart;
    private String path; // Path to the data file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurement_details);

        ecgChart = findViewById(R.id.chartECG_detailed); // Initialize the ECG chart

        // Configure ActionBar with custom title and color
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Measurement");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }

        // Load shared preferences for patient and profile information
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("patient", "");
        String profile = sharedPref.getString("profile", "");

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Retrieve patient's first name from the database
        Cursor nameCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT first_name FROM User " +
                        "WHERE username = ? " +
                        "LIMIT 1",
                new String[]{patient}
        );
        nameCursor.moveToFirst();
        String name = nameCursor.getString(0);

        // Set the ActionBar title based on user profile
        if (profile.equals("nurse") || profile.equals("doctor")) {
            getSupportActionBar().setTitle(name + "'s Measurement");
        }

        // Retrieve date and time from the intent
        Intent previousIntent = getIntent();
        String date = previousIntent.getStringExtra("date");
        String time = previousIntent.getStringExtra("time");

        // Set up navigation buttons
        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent;
            if (profile.equals("doctor")) {
                intent = new Intent(v.getContext(), HomeDoctor.class);
            } else if (profile.equals("nurse")) {
                intent = new Intent(v.getContext(), HomeNurse.class);
            } else {
                intent = new Intent(v.getContext(), HomePatient.class);
            }
            startActivity(intent);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationsButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Details.this, Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(Details.this, Log.class);
            startActivity(intent);
        });

        // Retrieve measurement data and AF detection status from the database
        dbHelper = new DatabaseHelper(this);
        List<String> measurementData = null;
        String has_AF = null;
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM Measurement WHERE patient='" + patient +
                            "' AND date='" + date +
                            "' AND time='" + time + "' ORDER BY time DESC",
                    null);

            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex("file"));
                int AF_presence = cursor.getInt(cursor.getColumnIndex("AF_presence"));
                has_AF = (AF_presence == 1) ? "AF Detected" : "No AF Detected";
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display date, AF presence, and observations
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

            date_time = formattedDate + ", " + formattedTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date.setText(date_time);

        AFpresent = findViewById(R.id.AF_presence);
        AFpresent.setText(has_AF);

        Observations = findViewById(R.id.observation);
        Observations.setText("No observations");

        // Configure and populate the ECG chart
        configureChart(ecgChart);
        ArrayList<Entry> data = readTxtFile(path);
        String af_path = deriveAFDetectionFilePath(path);
        ArrayList<Entry> data_af = readAFDataFromPath(af_path);
        updateChart(ecgChart, data, data_af);
    }

    // ---
    // Chart adjustments
    // ---

    // Configure chart appearance and behavior
    private void configureChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setVisibleXRangeMaximum(1000);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(-0.8f);
        leftAxis.setAxisMaximum(0.8f);
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);

        chart.getAxisRight().setEnabled(false);
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }

    // Update the chart with mapped data
    private void updateChart(LineChart chart, ArrayList<Entry> data, ArrayList<Entry> AF_data) {
        float minIn = 0f;
        float maxIn = 230f;
        float minOut = -1.15f;
        float maxOut = 1.15f;

        ArrayList<Entry> mappedData = new ArrayList<>();
        for (Entry entry : data) {
            float mappedY = mapToMV(entry.getY(), minIn, maxIn, minOut, maxOut);
            mappedData.add(new Entry(entry.getX(), mappedY));
        }

        LineDataSet dataSetECG = new LineDataSet(mappedData, "ECG Data");
        dataSetECG.setColor(ContextCompat.getColor(this, R.color.hartpink));

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSetECG);

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
    }

    private float mapToMV(float x, float minIn, float maxIn, float minOut, float maxOut) {
        return minOut + ((x - minIn) * (maxOut - minOut)) / (maxIn - minIn);
    }


    // ---
    // Text files manipulation
    // ---

    // Helper method to derive the path for AF detection data
    private String deriveAFDetectionFilePath(String originalPath) {
        return originalPath.replace(".txt", "_AFdetection.txt");
    }


    private ArrayList<Entry> readAFDataFromPath(String filePath) {
        ArrayList<Entry> afData = new ArrayList<>();
        File file = new File(getFilesDir() + File.separator + filePath);

        if (!file.exists()) {
            return afData;
        }
        // Read and parse AF data
        return afData;
    }

    private ArrayList<Entry> readTxtFile(String filePath) {
        ArrayList<Entry> data = new ArrayList<>();
        File file = new File(getFilesDir() + File.separator + filePath);
        if (file.exists()) {
            // Read and parse ECG data
        }
        return data;
    }
}





