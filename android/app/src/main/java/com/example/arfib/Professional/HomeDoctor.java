package com.example.arfib.Professional;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arfib.R;

public class HomeDoctor extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagedoctor);
        getSupportActionBar().setTitle("Doctor");  // Set empty title
    }
}
