package com.example.arfib.Measurements;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.Notifications;
import com.example.arfib.R;

public class Measuring extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurementmeasuring);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Measuring");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }

        ImageButton cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(Measuring.this, Log.class);
            startActivity(intent);
        });
    }
}
