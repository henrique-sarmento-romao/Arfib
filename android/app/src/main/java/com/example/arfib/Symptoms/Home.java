package com.example.arfib.Symptoms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.DateList;
import com.example.arfib.Medications.DayMedicationList;
import com.example.arfib.R;

public class Home extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptomshome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Symptoms");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.symptompurple));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.symptompurple));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("username", "");

        Intent previousIntent = getIntent();
        String viewDate = previousIntent.getStringExtra("date");

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (viewDate == null) {
            Cursor maxDate = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT * FROM Symptom_Log WHERE patient= ? GROUP BY date ORDER BY date DESC, time DESC LIMIT 1",
                    new String[]{patient}
            );
            maxDate.moveToFirst();
            viewDate = maxDate.getString(maxDate.getColumnIndex("date"));
        }

        List<List<String>> dateList = new ArrayList<>();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Symptom_Log WHERE patient= ? GROUP BY date ORDER BY date DESC, time DESC",
                new String[]{patient}
        );
        if (cursor.moveToFirst()) {
            do {
                java.util.List<String> date_time = new ArrayList<>();

                String date = cursor.getString(cursor.getColumnIndex("date"));
                String time = cursor.getString(cursor.getColumnIndex("time"));

                date_time.add(date);
                date_time.add(time);

                dateList.add(date_time);
            } while (cursor.moveToNext());
        }
        cursor.close();

        RecyclerView recyclerView = findViewById(R.id.dates);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);

        DateList adapter = new DateList(this, dateList, patient, viewDate);
        recyclerView.setAdapter(adapter);

        int selectedPosition = -1; // Default to -1 if no match is found
        for (int i = 0; i < dateList.size(); i++) {
            List<String> date_time = dateList.get(i);
            if (date_time.get(0).equals(viewDate)) { // Compare with the date part
                selectedPosition = i;
                break;
            }
        }
        if (selectedPosition != -1) {
            int finalSelectedPosition = selectedPosition;
            recyclerView.post(() -> {
                int offset = (recyclerView.getWidth() / 2) - (recyclerView.getChildAt(0).getWidth() / 2);
                layoutManager.scrollToPositionWithOffset(finalSelectedPosition, offset);
            });
        }

        List<List<String>> day_symptoms = new ArrayList<>();
        Cursor daySymptoms = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Symptom_Log " +
                        "WHERE patient = ? AND date = ? " +
                        "ORDER BY time ASC",
                new String[]{patient, viewDate}
        );
        if (daySymptoms.moveToFirst()) {
            do {
                String symptom = daySymptoms.getString(daySymptoms.getColumnIndex("symptom"));
                String time = daySymptoms.getString(daySymptoms.getColumnIndex("time"));
                int intensity = daySymptoms.getInt(daySymptoms.getColumnIndex("intensity"));

                SimpleDateFormat inputFormatter = new SimpleDateFormat("HH:mm:ss.SSSSSS", Locale.getDefault());
                SimpleDateFormat outputFormatter = new SimpleDateFormat("HH:mm", Locale.getDefault());

                String formattedTime = "";
                try{
                    Date parsedTime = inputFormatter.parse(time);
                    formattedTime = outputFormatter.format(parsedTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String intensityString="";
                switch (intensity) {
                    case 1:
                        intensityString = "Low";
                        break;
                    case 2:
                        intensityString = "Moderate";
                        break;
                    case 3:
                        intensityString = "High";
                        break;
                    case 4:
                        intensityString = "Very High";
                        break;
                }

                List<String> singleArray = new ArrayList<>();
                singleArray.add(symptom);
                singleArray.add(formattedTime);
                singleArray.add(intensityString);

                // Add this "array" to the main list
                day_symptoms.add(singleArray);

            } while (daySymptoms.moveToNext());
        }
        daySymptoms.close();

        RecyclerView dayMedicationView = findViewById(R.id.day_symptoms);
        LinearLayoutManager dayMedicationLayoutManager = new LinearLayoutManager(this);
        dayMedicationView.setLayoutManager(dayMedicationLayoutManager);
        dayMedicationView.setVerticalScrollBarEnabled(false);
        dayMedicationView.setHorizontalScrollBarEnabled(false);

        DaySymptomList dayMedicationAdapter = new DaySymptomList(this, day_symptoms);
        dayMedicationView.setAdapter(dayMedicationAdapter);
    }

}
