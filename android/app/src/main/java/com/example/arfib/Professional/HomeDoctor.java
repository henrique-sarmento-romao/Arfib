package com.example.arfib.Professional;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.R;

public class HomeDoctor extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagedoctor);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Doctor Home");

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");


    }
}