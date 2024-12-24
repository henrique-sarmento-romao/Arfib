package com.example.arfib.Blog;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arfib.R;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_doctor);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Blog");
    }
}
