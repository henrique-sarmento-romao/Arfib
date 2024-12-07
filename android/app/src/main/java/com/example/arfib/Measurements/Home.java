package com.example.arfib.Measurements;

import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
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
import androidx.appcompat.app.AppCompatActivity;
import com.example.arfib.Database.DatabaseHelper;

import java.io.IOException;

public class Home extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurementhome);

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

        Button measurement = findViewById(R.id.button);
        measurement.setOnClickListener(v ->{
            Intent intent = new Intent(Home.this, Detailed.class);
            startActivity(intent);
        });

        dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();

            // Example of reading data from the database
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM User", null);

            if (cursor.moveToFirst()) {
                do {
                    // Access the data in the cursor
                    String data = cursor.getString(cursor.getColumnIndex("username"));
                    // Do something with the data
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the ActionBar
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }
}