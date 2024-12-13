package com.example.arfib.Symptoms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
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
import java.util.List;
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
        String patient = sharedPref.getString("username", "");

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
                        "LIMIT 1",
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
        int black = getResources().getColor(R.color.black);
        dataSet.setColor(purple);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleColor(purple);
        dataSet.setCircleHoleColor(black);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(false);

        LineData lineData = new LineData(dataSet);
            SymptomTimeline.setData(lineData);

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
        xAxis.setLabelCount(10, true);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceMax(4f); // Add extra space to prevent last label from being clipped


        YAxis rightAxis = SymptomTimeline.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);

        Legend legend = SymptomTimeline.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        SymptomTimeline.setExtraOffsets(0, 50, 10, 15); // Add padding to the chart
        SymptomTimeline.getDescription().setEnabled(false);
        SymptomTimeline.getDescription().setText("Stock Quotes Over Time");
        SymptomTimeline.getDescription().setTextColor(Color.WHITE);

        SymptomTimeline.invalidate(); // Refresh chart
    }
}