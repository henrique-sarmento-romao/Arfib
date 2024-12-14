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
import com.example.arfib.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.Professional.HomeDoctor;
import com.example.arfib.Professional.HomeNurse;
import com.example.arfib.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PatientMedications extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationlist);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Your Medications");
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

        ImageButton addMedication = findViewById(R.id.addMedication);
        addMedication.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AddNew.class);
            startActivity(intent);
        });

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
            actionBar.setTitle(name+ "'s Medications");
            TextView YourMedications = findViewById(R.id.your_medications);
            YourMedications.setText(name+ "'s Medications");
        }

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

                java.util.List<String> singleArray = new ArrayList<>();
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