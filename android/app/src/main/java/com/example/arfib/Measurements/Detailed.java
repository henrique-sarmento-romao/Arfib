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

public class Detailed extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private LineChart ecgChart;
    private String path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurement_details);

        ecgChart = findViewById(R.id.chartECG_detailed);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Measurement");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }

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

        Cursor nameCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT first_name FROM User " +
                        "WHERE username = ? " +
                        "LIMIT 1",
                new String[]{patient}
        );
        nameCursor.moveToFirst();
        String name = nameCursor.getString(0);

        if (profile.equals("nurse") || profile.equals("doctor")){
            getSupportActionBar().setTitle(name+ "'s Measurement");
        }

        Intent previousIntent = getIntent();
        String date = previousIntent.getStringExtra("date");
        String time = previousIntent.getStringExtra("time");

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent;
            if (profile.equals("doctor")){
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

            Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Measurement WHERE patient='" + patient + "' AND date='" + date + "' AND time='" + time + "' ORDER BY time DESC", null);

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
        String af_path = deriveAFDetectionFilePath(path);
        ArrayList<Entry> data_af = readAFDataFromPath(af_path);
        updateChart(ecgChart, data,data_af);

    }
    private String deriveAFDetectionFilePath(String originalPath) {
        return originalPath.replace(".txt", "_AFdetection.txt");
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
        xAxis.setDrawLabels(false); // Remove os rótulos numéricos
        xAxis.setDrawAxisLine(false);

        chart.setVisibleXRangeMaximum(1000);  // Limita a visualização a 1000 pontos no eixo X
        chart.setVisibleXRangeMinimum(1);
        chart.setPinchZoom(false);
        chart.setScaleXEnabled(true);
        chart.setScaleYEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMinimum(-0.8f); // Mínimo fixo
        leftAxis.setAxisMaximum(0.8f);  // Máximo fixo
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }
    private void updateChart(LineChart chart, ArrayList<Entry> data, ArrayList<Entry> AF_data) {
        // Data mapping
        float minIn = 0f;
        float maxIn = 230f;   // Maximum of bit
        float minOut = -1.15f; // Min in mV (Y)
        float maxOut = 1.15f;  // Max in mV (Y)

        ArrayList<Entry> mappedData = new ArrayList<>();
        ArrayList<Entry> mappedAF = new ArrayList<>();

        for (Entry entry : data) {

            // Map Y (ECG) to mV
            float mappedY = mapToMV(entry.getY(), minIn, maxIn, minOut, maxOut);

            mappedData.add(new Entry(entry.getX(), mappedY));
        }



        LineDataSet dataSetECG = new LineDataSet(mappedData, "ECG Data");
        int color = ContextCompat.getColor(this, R.color.hartpink);
        dataSetECG.setColor(color);
        dataSetECG.setLineWidth(2f);
        dataSetECG.setDrawCircles(false);
        dataSetECG.setDrawValues(false);
        dataSetECG.setMode(LineDataSet.Mode.LINEAR);

        LineDataSet dataSetaf = new LineDataSet(AF_data, "ECG Data");
        int color_2 = ContextCompat.getColor(this, R.color.atrial);
        dataSetaf.setColor(color_2);
        dataSetaf.setLineWidth(2f);
        dataSetaf.setDrawCircles(false);
        dataSetaf.setDrawValues(false);
        dataSetaf.setMode(LineDataSet.Mode.LINEAR);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSetECG);
        dataSets.add(dataSetaf);

        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
    }
    private float mapToMV(float x, float minIn, float maxIn, float minOut, float maxOut) {
        return minOut + ((x - minIn) * (maxOut - minOut)) / (maxIn - minIn);
    }

    private float mapToTime(float x, float sampleRate) {

        return x / sampleRate;
    }

    private ArrayList<Entry> readAFDataFromPath(String filePath) {
        ArrayList<Entry> afData = new ArrayList<>();
        File file = new File(getFilesDir()+ File.separator + filePath);

        if (!file.exists()) {
             return afData;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Ignora linhas vazias
                if (line.isEmpty()) {
                    continue;
                }

                String[] values = line.split("\\s+"); // Divide por espaços (um ou mais)

                if (values.length == 2) { // Valida que a linha tem exatamente duas colunas
                    try {
                        float x = Float.parseFloat(values[0]); // Coluna 1
                        float y = Float.parseFloat(values[1]); // Coluna 2
                        afData.add(new Entry(x, y)); // Adiciona ponto à lista
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Erro ao converter valores na linha " + lineNumber + ": " + line, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Linha com formato inesperado (" + lineNumber + "): " + line, Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao ler o arquivo: " + filePath, Toast.LENGTH_LONG).show();
        }

        return afData;
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








}