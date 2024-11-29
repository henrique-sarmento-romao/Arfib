package com.example.arfib.Professional;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arfib.R;

public class PatientList extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagedoctor);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Patient List");
    }
}
