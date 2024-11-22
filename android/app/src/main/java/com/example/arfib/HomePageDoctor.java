package com.example.arfib;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageDoctor extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.homepagedoctor);
        Toolbar toolbar = findViewById(R.id.toolbar_hd);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");  // Set empty title

    }
}
