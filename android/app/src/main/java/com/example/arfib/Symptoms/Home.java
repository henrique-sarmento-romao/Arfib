package com.example.arfib.Symptoms;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.R;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagedoctor);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Symptoms");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.symptompurple));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.symptompurple));
        }
    }
}
