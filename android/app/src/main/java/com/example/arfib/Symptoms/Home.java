package com.example.arfib.Symptoms;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.arfib.R;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptomshome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Symptoms");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.symptompurple));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.symptompurple));
        }

        TextView todayDateTextView = findViewById(R.id.todaydate);

        // Data atual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Define o texto da TextView
        todayDateTextView.setText("Today: "+ currentDate);
    }

}
