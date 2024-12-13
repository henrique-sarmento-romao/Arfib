package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MedicationList extends AppCompatActivity {
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
        String username = sharedPref.getString("username", "");

        ImageButton addMedication = findViewById(R.id.addMedication);
        addMedication.setOnClickListener(v -> {
            Intent intent = new Intent(MedicationList.this, AddNew.class);
            startActivity(intent);
        });

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<List<String>> patient_medications = new ArrayList<>();
        Cursor patMed = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Prescription " +
                        "JOIN Medication ON Prescription.medication = Medication.name " +
                        "WHERE patient = ? " +
                        "ORDER BY start_date ASC",
                new String[]{username}
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