package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.HomePatient;
import com.example.arfib.Measurements.Home;
import com.example.arfib.Measurements.Log;
import com.example.arfib.Notifications;
import com.example.arfib.Professional.HomeDoctor;
import com.example.arfib.Professional.HomeNurse;
import com.example.arfib.R;

public class AddNew extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationaddnew);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String profile = sharedPref.getString("profile", "");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Medication");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.drugblue));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.drugblue));
        }

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
    }
}
