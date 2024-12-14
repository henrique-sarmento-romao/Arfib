package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.DateList;
import com.example.arfib.HomePatient;
import com.example.arfib.Measurements.Log;
import com.example.arfib.Notifications;
import com.example.arfib.Professional.HomeDoctor;
import com.example.arfib.Professional.HomeNurse;
import com.example.arfib.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Home extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationhome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Medications");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.drugblue));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.drugblue));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("patient", "");
        String profile = sharedPref.getString("profile", "");

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
            Intent intent = new Intent(v.getContext(), Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddNew.class);
            startActivity(intent);
        });

        TextView YourMedications = findViewById(R.id.your_medications);
        YourMedications.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PatientMedications.class);
            startActivity(intent);
        });

        Intent previousIntent = getIntent();
        String viewDate = previousIntent.getStringExtra("date");
        if (viewDate == null) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date today = new Date();
            viewDate = dateFormatter.format(today);
        }

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
            YourMedications.setText(name+ "'s Medications");
        }

        List<List<String>> dateList = new ArrayList<>();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Medication_Log WHERE patient='" + patient + "' GROUP BY date ORDER BY date DESC, time DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                List<String> date_time = new ArrayList<>();

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


        List<List<String>> day_medications = new ArrayList<>();
        Cursor dayMed = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Medication_Log " +
                        "JOIN Medication ON Medication_Log.medication = Medication.name " +
                        "WHERE patient = ? AND date = ? " +
                        "ORDER BY taken ASC, time ASC",
                new String[]{patient, viewDate}
        );
        if (dayMed.moveToFirst()) {
            do {
                String med = dayMed.getString(dayMed.getColumnIndex("medication"));
                String date = dayMed.getString(dayMed.getColumnIndex("date"));
                String time = dayMed.getString(dayMed.getColumnIndex("time"));
                String asset = dayMed.getString(dayMed.getColumnIndex("image"));
                int taken = dayMed.getInt(dayMed.getColumnIndex("taken"));

                String isTaken="";
                if(taken==1){
                    isTaken = "true";
                } else if(taken==0){
                    isTaken = "false";
                }
                List<String> singleArray = new ArrayList<>();
                singleArray.add(med);
                singleArray.add(isTaken);
                singleArray.add(date);
                singleArray.add(time);
                singleArray.add(asset);

                // Add this "array" to the main list
                day_medications.add(singleArray);

            } while (dayMed.moveToNext());
        }
        dayMed.close();

        RecyclerView dayMedicationView = findViewById(R.id.day_medications);
        LinearLayoutManager dayMedicationLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dayMedicationView.setLayoutManager(dayMedicationLayoutManager);
        dayMedicationView.setVerticalScrollBarEnabled(false);
        dayMedicationView.setHorizontalScrollBarEnabled(false);

        ListDayMedication dayMedicationAdapter = new ListDayMedication(this, day_medications);
        dayMedicationView.setAdapter(dayMedicationAdapter);

        List<List<String>> patient_medications = new ArrayList<>();
        Cursor patMed = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Prescription " +
                        "JOIN Medication ON Prescription.medication = Medication.name " +
                        "WHERE patient = ? " +
                        "ORDER BY start_date ASC",
                new String[]{patient}
        );
        if (patMed.moveToFirst()) {
            do {
                String med = patMed.getString(patMed.getColumnIndex("medication"));
                int frequency = patMed.getInt(patMed.getColumnIndex("frequency"));
                String asset = patMed.getString(patMed.getColumnIndex("image"));

                List<String> singleArray = new ArrayList<>();
                singleArray.add(med);
                singleArray.add(String.valueOf(frequency));
                singleArray.add(asset);

                // Add this "array" to the main list
                patient_medications.add(singleArray);
            } while (patMed.moveToNext());
        }
        patMed.close();

        RecyclerView patientMedicationView = findViewById(R.id.medication_list);
        LinearLayoutManager patientMedicationLayoutManager = new LinearLayoutManager(this);
        patientMedicationView.setLayoutManager(patientMedicationLayoutManager);
        patientMedicationView.setVerticalScrollBarEnabled(false);
        patientMedicationView.setHorizontalScrollBarEnabled(false);

        PatientMedicationList patientMedicationAdapter = new PatientMedicationList(this, patient_medications);
        patientMedicationView.setAdapter(patientMedicationAdapter);

    }
}