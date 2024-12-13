package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.R;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Details extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationdetails);

        Intent previousIntent = getIntent();
        String med_name = previousIntent.getStringExtra("med_name");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(med_name);
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.drugblue));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.drugblue));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cursor medication = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Prescription " +
                        "JOIN Medication ON Prescription.medication = Medication.name " +
                        "WHERE patient = ? AND medication = ? " +
                        "ORDER BY start_date DESC LIMIT 1",
                new String[]{username, med_name}
        );

        medication.moveToFirst();
        int frequency = medication.getInt(medication.getColumnIndex("frequency"));
        String start_date = medication.getString(medication.getColumnIndex("start_date"));
        String end_date = medication.getString(medication.getColumnIndex("end_date"));
        String effect = medication.getString(medication.getColumnIndex("effect"));
        String asset = medication.getString(medication.getColumnIndex("image"));

        TextView Frequency, StartDate, EndDate, Effects;
        ImageView MedImage;

        Frequency = findViewById(R.id.frequency);
        StartDate = findViewById(R.id.start_date);
        EndDate = findViewById(R.id.end_date);
        Effects = findViewById(R.id.effects);
        MedImage = findViewById(R.id.medImage);

        int days = frequency / 24;  // Get the number of days
        int remainingHours = frequency % 24;  // Get the remaining hours after dividing by 24
        String frequencyText;
        if(days==0){
            frequencyText = String.format("Every %02dh", remainingHours);
        } else if(remainingHours==0) {
            frequencyText = String.format("Every %dd", days);
        } else {
            frequencyText = String.format("Every %dd %02dh", days, remainingHours);
        }
        Frequency.setText(frequencyText);


        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());
        SimpleDateFormat outputFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String start_date_formatted = "";
        try{
            Date start_date_parsed = inputFormatter.parse(start_date);
            start_date_formatted = outputFormatter.format(start_date_parsed);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        StartDate.setText(start_date_formatted);

        if(end_date == null){
            EndDate.setText("Not Applicable");
        } else {
            String end_date_formatted = "";
            try {
                Date end_date_parsed = inputFormatter.parse(end_date);
                end_date_formatted = outputFormatter.format(end_date_parsed);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            EndDate.setText(end_date_formatted);
        }

        Effects.setText(effect);

        int resId = getResources().getIdentifier(asset, "drawable","com.example.arfib");
        MedImage.setImageResource(resId);
    }
}