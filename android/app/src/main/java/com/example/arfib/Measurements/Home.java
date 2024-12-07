package com.example.arfib.Measurements;

import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.arfib.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.R;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Database.DatabaseHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Home extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurementhome);

        Intent previousIntent = getIntent();
        String username = previousIntent.getStringExtra("username");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Measurements");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.electroyellow));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.electroyellow));
        }

        ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, HomePatient.class);
            startActivity(intent);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationsButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Notifications.class);
            startActivity(intent);
        });

        ImageButton logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Log.class);
            startActivity(intent);
        });

        //Button measurement = findViewById(R.id.button);
        //measurement.setOnClickListener(v -> {
            //Intent intent = new Intent(Home.this, Detailed.class);
           // startActivity(intent);
        //});

        dbHelper = new DatabaseHelper(this);

        List<List<String>> dataList = null;
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            // Example of reading data from the database
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Measurement WHERE patient='"+ username +"' ORDER BY date_time DESC", null);

            dataList = new ArrayList<>();

            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(cursor.getColumnIndex("date_time"));

                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                    LocalDateTime dateTime = LocalDateTime.parse(date, inputFormatter);

                    // Format the date into the desired format
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM d yyyy, HH:mm");
                    String formattedDate = dateTime.format(outputFormatter);

                    String path = cursor.getString(cursor.getColumnIndex("file"));
                    int AF_presence = cursor.getInt(cursor.getColumnIndex("AF_presence"));
                    String has_AF;
                    if (AF_presence == 0) {
                        has_AF = "AF Detected";
                    } else {
                        has_AF = "No AF Detected";
                    }

                    // Create a list representing one "array" of strings
                    List<String> singleArray = new ArrayList<>();
                    singleArray.add(formattedDate);
                    singleArray.add(has_AF);
                    singleArray.add(path);

                    // Add this "array" to the main list
                    dataList.add(singleArray);

                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseHelper db = new DatabaseHelper(this);
        MeasurementList adapter = new MeasurementList(this, dataList, username);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the ActionBar
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }
}

