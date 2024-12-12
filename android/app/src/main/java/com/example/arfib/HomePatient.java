package com.example.arfib;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Database.DatabaseHelper;
import com.example.arfib.Measurements.BluetoothActivity;
import com.example.arfib.Measurements.Home;
import com.example.arfib.Medications.DayMedicationList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HomePatient extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagepatient);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setIcon(R.drawable.ic_menu_icon);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");

        TextView welcome = findViewById(R.id.welcome);
        welcome.setText("👋 Welcome, "+username+"!");

        Button logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("username"); // Remove the username
            editor.remove("is_logged_in"); // Remove the is_logged_in flag
            editor.apply();
            Intent backMainActivity = new Intent(HomePatient.this, MainActivity.class);
            startActivity(backMainActivity);
        });

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        String viewDate = dateFormatter.format(today);


        List<List<String>> day_medications = new ArrayList<>();
        Cursor dayMed = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Medication_Log " +
                        "JOIN Medication ON Medication_Log.medication = Medication.name " +
                        "WHERE patient = ? AND date = ? " +
                        "ORDER BY date DESC, time DESC",
                new String[]{username, viewDate}
        );
        if (dayMed.moveToFirst()) {
            do {
                String med = dayMed.getString(dayMed.getColumnIndex("medication"));
                String date = dayMed.getString(dayMed.getColumnIndex("date"));
                String time = dayMed.getString(dayMed.getColumnIndex("time"));
                String asset = dayMed.getString(dayMed.getColumnIndex("image"));
                int taken = dayMed.getInt(dayMed.getColumnIndex("taken"));

                String isTaken="";
                if(taken==1){
                    isTaken = "true";
                } else if(taken==0){
                    isTaken = "false";
                }
                List<String> singleArray = new ArrayList<>();
                singleArray.add(med);
                singleArray.add(isTaken);
                singleArray.add(date);
                singleArray.add(time);
                singleArray.add(asset);

                // Add this "array" to the main list
                day_medications.add(singleArray);

            } while (dayMed.moveToNext());
        }
        dayMed.close();

        RecyclerView dayMedicationView = findViewById(R.id.day_medications);
        LinearLayoutManager dayMedicationLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        dayMedicationView.setLayoutManager(dayMedicationLayoutManager);
        dayMedicationView.setVerticalScrollBarEnabled(false);
        dayMedicationView.setHorizontalScrollBarEnabled(false);

        DayMedicationList dayMedicationAdapter = new DayMedicationList(this, day_medications);
        dayMedicationView.setAdapter(dayMedicationAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the ActionBar
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu) {
            // Show PopupMenu when "Notifications" is clicked
            showHorizontalPopupMenu(item);
            return true;
        } else if (id == R.id.notifications) {
            Intent intent = new Intent(HomePatient.this, Notifications.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.measurements) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Measurements.Home.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.symptoms) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Symptoms.Home.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.medications) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Medications.Home.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.blog) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Blog.Home.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHorizontalPopupMenu(MenuItem item) {
        // Create a PopupMenu for the notifications item
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.menu));

        // Inflate the menu for PopupMenu, using the same or different menu XML
        getMenuInflater().inflate(R.menu.page_menu, popupMenu.getMenu());

        // Customize the appearance of the PopupMenu (e.g., making it horizontal)
        popupMenu.setGravity(Gravity.START);

        // Show the menu
        popupMenu.show();
    }
}
