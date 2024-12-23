package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arfib.Users.HomeDoctor;
import com.example.arfib.Users.HomeNurse;
import com.example.arfib.Users.HomePatient;

public class Notifications extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Notifications");
    }
}
