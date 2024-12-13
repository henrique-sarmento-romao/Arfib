package com.example.arfib;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.Measurements.Log;

public class Notifications extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagedoctor);

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(Notifications.this, HomePatient.class);
            startActivity(intent);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationsButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Notifications.this, Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(Notifications.this, Log.class);
            startActivity(intent);
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");
    }
}
