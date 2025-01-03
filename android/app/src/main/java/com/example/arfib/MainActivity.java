package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arfib.Users.HomeDoctor;
import com.example.arfib.Users.HomeNurse;
import com.example.arfib.Users.HomePatient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPref.getBoolean("is_logged_in", false);
        String profile = sharedPref.getString("profile", "patient");

        if(isLoggedIn){
            switch (profile) {
                case "patient":
                    startActivity(new Intent(this, HomePatient.class));
                    break;
                case "doctor":
                    startActivity(new Intent(this, HomeDoctor.class));
                    break;
                case "nurse":
                    startActivity(new Intent(this, HomeNurse.class));
                    break;
            }
        }


        // Set up navigation buttons
        Button patient_button = findViewById(R.id.patient_button);
        patient_button.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("profile", "patient");
            editor.apply();
            startActivity(new Intent(v.getContext(), Login.class));
        });

        Button doctor_but = findViewById(R.id.doctor_button);
        doctor_but.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("profile", "doctor");
            editor.apply();
            startActivity(new Intent(v.getContext(), Login.class));
        });

        Button nurse_but = findViewById(R.id.nurse_button);
        nurse_but.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("profile", "nurse");
            editor.apply();
            startActivity(new Intent(v.getContext(), Login.class));
        });

        Button register = findViewById(R.id.register_button);
        register.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), Register.class);
            startActivity(intent);
        });
    }
}
