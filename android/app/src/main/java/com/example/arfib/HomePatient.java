package com.example.arfib;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arfib.Measurements.BluetoothActivity;


public class HomePatient extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagepatient);

        getSupportActionBar().setTitle("Home");  // Set empty title


        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(HomePatient.this, BluetoothActivity.class);
            startActivity(intent);
        });
    }



}
