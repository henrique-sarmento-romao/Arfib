package com.example.arfib;

import android.content.Intent;
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

        Button patient_button = findViewById(R.id.patient_button);
        patient_button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.putExtra("chosen", "p");
            startActivity(intent);
        });

        Button doctor_but = findViewById(R.id.doctor_button);
        doctor_but.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.putExtra("chosen", "d");
            startActivity(intent);
        });

        Button nurse_but = findViewById(R.id.nurse_button);
        nurse_but.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.putExtra("chosen", "n");
            startActivity(intent);
        });

        TextView register = findViewById(R.id.register_button);
        register.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PatientRegister.class);
            startActivity(intent);
        });
    }
}
