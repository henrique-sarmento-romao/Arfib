package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        /*if(isLoggedIn){
            startActivity(new Intent(MainActivity.this, Login.class));
        }*/

        Button patient_button = findViewById(R.id.patient_button);
        patient_button.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("profile", "patient");
            editor.apply();
            startActivity(new Intent(MainActivity.this, Login.class));
        });

        Button doctor_but = findViewById(R.id.doctor_button);
        doctor_but.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("profile", "doctor");
            editor.apply();
            startActivity(new Intent(MainActivity.this, Login.class));
        });

        Button nurse_but = findViewById(R.id.nurse_button);
        nurse_but.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("profile", "nurse");
            editor.apply();
            startActivity(new Intent(MainActivity.this, Login.class));
        });

        Button register = findViewById(R.id.register_button);
        register.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });
    }
}
