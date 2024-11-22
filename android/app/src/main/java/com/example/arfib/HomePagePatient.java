package com.example.arfib;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class HomePagePatient extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.homepagepatient);
        Toolbar toolbar = findViewById(R.id.toolbar_hp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");  // Set empty title


        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(HomePagePatient.this, BluetoothActivity.class);
            startActivity(intent);
        });
    }



}
