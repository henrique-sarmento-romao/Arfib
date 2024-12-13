package com.example.arfib.Symptoms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Log extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptom_log);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Log Symptom");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.symptompurple));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.symptompurple));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("patient", "");

        Intent previousIntent = getIntent();
        String intentSymptom = previousIntent.getStringExtra("symptom");

        Spinner Symptom, Intensity;
        Symptom = findViewById(R.id.symptom);
        Intensity = findViewById(R.id.intensity);
        ImageButton LogButton = findViewById(R.id.logButton);

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Load symptom options from the database
        List<String> all_symptoms = new ArrayList<>();
        Cursor allSymptoms = dbHelper.getReadableDatabase().rawQuery(
                "SELECT name FROM Symptom",
                null
        );
        if (allSymptoms.moveToFirst()) {
            do {
                String symp = allSymptoms.getString(allSymptoms.getColumnIndexOrThrow("name")); // Use `getColumnIndexOrThrow` for safety
                all_symptoms.add(symp);
            } while (allSymptoms.moveToNext());
        }
        allSymptoms.close();

        // Set up adapter for Symptom spinner
        ArrayAdapter<String> symptomAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, all_symptoms);
        symptomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Symptom.setPopupBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.symptom_box));
        Symptom.setAdapter(symptomAdapter);

        // Handle pre-selected symptom from Intent
        if (intentSymptom != null) {
            int position = symptomAdapter.getPosition(intentSymptom);
            if (position >= 0) {
                Symptom.setSelection(position); // Set selection to the matching item
            } else {
                Symptom.setSelection(0); // Default to the first item
            }
        } else {
            Symptom.setSelection(0); // Default to the first item
        }

        // Map for intensity options
        Map<String, Integer> intensityMap = new LinkedHashMap<>();
        intensityMap.put("Low", 1);
        intensityMap.put("Moderate", 2);
        intensityMap.put("High", 3);
        intensityMap.put("Very High", 4);

        String[] intensityOptions = intensityMap.keySet().toArray(new String[0]);

        // Set up adapter for Intensity spinner
        ArrayAdapter<String> intensityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, intensityOptions);
        intensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Intensity.setPopupBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.symptom_box));
        Intensity.setAdapter(intensityAdapter);
        Intensity.setSelection(0);

        // Handle button click
        LogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedSymptom = Symptom.getSelectedItem().toString();
                String selectedIntensity = Intensity.getSelectedItem().toString();

                // Retrieve intensity as an integer
                Integer intIntensity = intensityMap.get(selectedIntensity);
                if (intIntensity == null) {
                    // Handle error case where the selected intensity is not mapped
                    intIntensity = -1;
                }

                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String time = new SimpleDateFormat("HH:mm:ss.SSSSSS", Locale.getDefault()).format(new Date());

                dbHelper.logSymptom(v, patient, selectedSymptom, intIntensity, date, time);

                // Create the Intent to send data to Home activity
                Intent intent = new Intent(v.getContext(), Home.class);
                startActivity(intent);
            }
        });
    }
}
