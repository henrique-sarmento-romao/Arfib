package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.HomePatient;
import com.example.arfib.DateList;
import com.example.arfib.Notifications;
import com.example.arfib.R;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.medicationhome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Medications");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.drugblue));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.drugblue));
        }

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");

        Intent previousIntent = getIntent();
        String viewDate = previousIntent.getStringExtra("date");
        if (viewDate == null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E,\nMMM d"); // Match the format in your dateList
            viewDate = LocalDateTime.now().format(formatter);
        }

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(com.example.arfib.Medications.Home.this, HomePatient.class);
            startActivity(intent);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationsButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(com.example.arfib.Medications.Home.this, Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(com.example.arfib.Medications.Home.this, Log.class);
            startActivity(intent);
        });

        dbHelper = new DatabaseHelper(this);

        java.util.List<String> dateList = null;
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            // Example of reading data from the database
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Medication_Log WHERE patient='"+ username +"' ORDER BY date_time DESC", null);

            dateList = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndex("date_time"));

                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);

                    // Format the date into the desired format
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("E,\nMMM d");
                    String formattedDate = dateTime.format(outputFormatter);

                    dateList.add(formattedDate);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVerticalScrollBarEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(false);

        DateList adapter = new DateList(this, dateList, username, viewDate);
        recyclerView.setAdapter(adapter);

        int selectedPosition = dateList.indexOf(viewDate);
        if (selectedPosition != -1) {
            recyclerView.post(() -> {
                int offset = (recyclerView.getWidth() / 2) - (recyclerView.getChildAt(0).getWidth() / 2);
                layoutManager.scrollToPositionWithOffset(selectedPosition, offset);
            });
        }
    }
}