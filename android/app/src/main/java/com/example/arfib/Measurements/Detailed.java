package com.example.arfib.Measurements;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.R;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Detailed extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurementdetailed);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Measurement");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }

        Intent previousIntent = getIntent();
        String username = previousIntent.getStringExtra("username");
        String date = previousIntent.getStringExtra("date");

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(Detailed.this, HomePatient.class);
            startActivity(intent);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationsButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Detailed.this, Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(Detailed.this, Log.class);
            startActivity(intent);
        });


        dbHelper = new DatabaseHelper(this);
        List<String> measurementData = null;
        String has_AF = null;
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            // Example of reading data from the database
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Measurement WHERE patient='" + username + "' ORDER BY date_time DESC", null);

            if (cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex("file"));
                int AF_presence = cursor.getInt(cursor.getColumnIndex("AF_presence"));
                if (AF_presence == 0) {
                    has_AF = "AF Detected";
                } else {
                    has_AF = "No AF Detected";
                }
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        TextView Date, AFpresent, Observations;

        Date = findViewById(R.id.date);
        Date.setText(date);

        AFpresent = findViewById(R.id.AF_presence);
        AFpresent.setText(has_AF);

        Observations = findViewById(R.id.observation);
        Observations.setText("No observations");

    }
}
