package com.example.arfib.Symptoms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Details extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptom_details);

        Intent previousIntent = getIntent();
        String symptom_name = previousIntent.getStringExtra("symptom");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(symptom_name);
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.symptompurple));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.symptompurple));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("patient", "");

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(Details.this, HomePatient.class);
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
            intent.putExtra("symptom", symptom_name);
            startActivity(intent);
        });

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cursor symptom = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Symptom " +
                        "WHERE name = ? " +
                        "LIMIT 1",
                new String[]{symptom_name}
        );

        symptom.moveToFirst();
        String description = symptom.getString(symptom.getColumnIndex("description"));

        TextView Description = findViewById(R.id.description);
        ImageView SymptomImage = findViewById(R.id.symptom_Image);
        LineChart SymptomTimeline = findViewById(R.id.symptom_Timeline);

        Description.setText(description);

        Map<String, String> symptomMap = new HashMap<>();
        symptomMap.put("Fatigue", "fatigue");
        symptomMap.put("Breathlessness", "breathlessness");
        symptomMap.put("Dizziness", "dizziness");
        symptomMap.put("Chest Pain", "chest_pain");
        String asset = symptomMap.get(symptom_name);

        int resId = getResources().getIdentifier(asset, "drawable","com.example.arfib");
        SymptomImage.setBackgroundResource(resId);

        Cursor symptom_log = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Symptom_Log " +
                        "WHERE patient = ? AND symptom = ? " +
                        "ORDER BY date ASC, time ASC",
                new String[]{patient, symptom_name}
        );

        ArrayList<Entry> symptom_entries = new ArrayList<>();
        if (symptom_log.moveToFirst()) {
            do {
                String date = symptom_log.getString(symptom_log.getColumnIndex("date"));
                String time = symptom_log.getString(symptom_log.getColumnIndex("time"));
                int intensity = symptom_log.getInt(symptom_log.getColumnIndex("intensity"));


                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());
                String dateTime = date + " " + time;

                long timestamp = new Date().getTime();
                try {
                    Date date_time = inputFormatter.parse(dateTime);
                    timestamp = date_time.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                symptom_entries.add(new Entry((float) timestamp, (float) intensity));

            } while (symptom_log.moveToNext());
        }
        symptom_log.close();


        LineDataSet dataSet = new LineDataSet(symptom_entries, symptom_name+" Logs");
        int purple = getResources().getColor(R.color.symptompurple);
        dataSet.setColor(purple);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(1f);
        dataSet.setCircleColor(purple);
        dataSet.setCircleHoleColor(purple);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(false);

        SymptomTimeline.setData(new LineData(dataSet));

        SymptomTimeline.setExtraLeftOffset(16f); // Adjust padding for the left
        SymptomTimeline.setExtraRightOffset(16f); // Adjust padding for the right

        // Format X-axis with dates
        XAxis xAxis = SymptomTimeline.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Create a Date object using the timestamp
                Date date = new Date((long) value);

                // Define a simple date format (you can modify the format as needed)
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Format as "Year-Month-Day"

                // Return the formatted date string
                return dateFormat.format(date);
            }
        });
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(4, true);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setTextColor(R.color.atrial);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceMax(4f); // Add extra space to prevent last label from being clipped
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = SymptomTimeline.getAxisLeft();
        leftAxis.setDrawGridLines(true); // Ensure horizontal grid lines are drawn
        leftAxis.enableGridDashedLine(30f, 20f, 0f); // Dash pattern: 10px line, 5px space
        leftAxis.setAxisMinimum(1f); // Ensure axis starts at 1
        leftAxis.setAxisMaximum(4f); // Ensure axis ends at 4
        leftAxis.setLabelCount(4, true); // Ensure labels at 1, 2, 3, 4
        leftAxis.setTextColor(R.color.atrial);

        Map<Integer, String> intensityMap = new HashMap<>();
            intensityMap.put(1, "Low");
            intensityMap.put(2, "Moderate");
            intensityMap.put(3, "High");
            intensityMap.put(4, "Very High");
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return intensityMap.get((int) value); // Default to empty string if no match
            }
        });


        YAxis rightAxis = SymptomTimeline.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y axis

        SymptomTimeline.setExtraOffsets(0, 50, 10, 15); // Add padding to the chart
        SymptomTimeline.getLegend().setEnabled(false); // Hide the legend
        SymptomTimeline.getDescription().setEnabled(false);
        SymptomTimeline.getDescription().setText("Stock Quotes Over Time");
        SymptomTimeline.getDescription().setTextColor(Color.WHITE);

        SymptomTimeline.invalidate(); // Refresh chart
    }
}