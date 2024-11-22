package com.example.arfib;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HomePageNurse extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.homepagenurse);
        Toolbar toolbar = findViewById(R.id.toolbar_hn);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");  // Set empty title
    }
}
