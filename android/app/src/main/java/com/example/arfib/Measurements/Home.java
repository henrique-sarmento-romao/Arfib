package com.example.arfib.Measurements;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.R;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagedoctor);
        getSupportActionBar().setTitle("Measurements");  // Set empty title
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        getSupportActionBar().setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }
    }
}
