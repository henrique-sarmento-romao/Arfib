package com.example.arfib.Users;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.MainActivity;
import com.example.arfib.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeNurse extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_nurse);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Nurse Home");

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String username = sharedPref.getString("username", "");

        Button logout = findViewById(R.id.logoutButton);
        logout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("username"); // Remove the username
            editor.remove("is_logged_in"); // Remove the is_logged_in flag
            editor.apply();
            Intent backMainActivity = new Intent(HomeNurse.this, MainActivity.class);
            startActivity(backMainActivity);
        });

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Cursor nameCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT first_name FROM User " +
                        "WHERE username = ? " +
                        "LIMIT 1",
                new String[]{username}
        );
        nameCursor.moveToFirst();
        String name = nameCursor.getString(0);

        TextView welcome = findViewById(R.id.welcome);
        welcome.setText("👋 Welcome, Nurse "+name+"!");


        List<List<String>> patient_list = new ArrayList<>();
        Cursor patientListCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Patient " +
                        "JOIN User USING(username) " +
                        "WHERE nurse = ? ",
                new String[]{username}
        );
        if (patientListCursor.moveToFirst()) {
            do {
                String pat_username = patientListCursor.getString(patientListCursor.getColumnIndex("username"));
                String first_name = patientListCursor.getString(patientListCursor.getColumnIndex("first_name"));
                String last_name = patientListCursor.getString(patientListCursor.getColumnIndex("last_name"));
                String asset = patientListCursor.getString(patientListCursor.getColumnIndex("picture"));

                List<String> singlePatient = new ArrayList<>();
                singlePatient.add(pat_username);
                singlePatient.add(first_name);
                singlePatient.add(last_name);
                singlePatient.add(asset);

                // Add this "array" to the main list
                patient_list.add(singlePatient);
            } while (patientListCursor.moveToNext());
        }
        patientListCursor.close();

        RecyclerView PatientListView = findViewById(R.id.patient_list);
        LinearLayoutManager patientListLayoutManager = new LinearLayoutManager(this);
        PatientListView.setLayoutManager(patientListLayoutManager);
        PatientListView.setVerticalScrollBarEnabled(false);
        PatientListView.setHorizontalScrollBarEnabled(false);

        PatientList patientListAdapter = new PatientList(this, patient_list);
        PatientListView.setAdapter(patientListAdapter);

    }
}
