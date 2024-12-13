package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.R;

import java.io.IOException;

public class Log extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationlog);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Log Medication");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.drugblue));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.drugblue));
        }

        Intent previousIntent = getIntent();
        String med_name = previousIntent.getStringExtra("med_name");
        String date = previousIntent.getStringExtra("date");
        String time = previousIntent.getStringExtra("time");

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("patient", "");

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cursor medication = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Medication_Log " +
                        "JOIN Medication ON Medication_Log.medication = Medication.name " +
                        "WHERE patient = ? AND medication = ? AND date = ? AND time = ?" +
                        "ORDER BY date DESC, time DESC LIMIT 1",
                new String[]{patient, med_name, date, time}
        );

        medication.moveToFirst();
        String asset = medication.getString(medication.getColumnIndex("image"));

        ImageView MedImage = findViewById(R.id.medImage);
        int resId = getResources().getIdentifier(asset, "drawable","com.example.arfib");
        MedImage.setImageResource(resId);

        Button logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            dbHelper.logMedication(this, patient,  med_name, date, time);
            Intent intent = new Intent(Log.this, Home.class);
            intent.putExtra("date", date);
            startActivity(intent);
        });




    }
}