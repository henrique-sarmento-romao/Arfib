package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Medications.DayMedicationList;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class HomePatient extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    String patient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagepatient);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setIcon(R.drawable.ic_menu_icon);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("patient", "");

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

        Cursor nameCursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT first_name FROM User " +
                        "WHERE username = ? " +
                        "LIMIT 1",
                new String[]{patient}
        );
        nameCursor.moveToFirst();
        String name = nameCursor.getString(0);

        TextView welcome = findViewById(R.id.welcome);
        welcome.setText("👋 Welcome, "+name+"!");

        Cursor af_timeline = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Measurement " +
                        "WHERE patient = ? " +
                        "ORDER BY date ASC, time ASC",
                new String[]{patient}
        );

        ArrayList<Entry> symptom_entries = new ArrayList<>();
        if (af_timeline.moveToFirst()) {
            do {
                String date = af_timeline.getString(af_timeline.getColumnIndex("date"));
                String time = af_timeline.getString(af_timeline.getColumnIndex("time"));
                int AF_presence = af_timeline.getInt(af_timeline.getColumnIndex("AF_presence"));


                SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());
                String dateTime = date + " " + time;

                long timestamp = new Date().getTime();
                try {
                    Date date_time = inputFormatter.parse(dateTime);
                    timestamp = date_time.getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                symptom_entries.add(new Entry((float) timestamp, (float) AF_presence));

            } while (af_timeline.moveToNext());
        }
        af_timeline.close();


        LineDataSet dataSet = new LineDataSet(symptom_entries, patient+" AF Presence");
        int pink = getResources().getColor(R.color.hartpink);
        dataSet.setColor(pink);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(1f);
        dataSet.setCircleColor(pink);
        dataSet.setCircleHoleColor(pink);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(false);

        LineChart AfTimeline = findViewById(R.id.AF_Timeline);
        AfTimeline.setData(new LineData(dataSet));

        AfTimeline.setExtraLeftOffset(16f); // Adjust padding for the left
        AfTimeline.setExtraRightOffset(16f); // Adjust padding for the right

        // Format X-axis with dates
        XAxis xAxis = AfTimeline.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Create a Date object using the timestamp
                Date date = new Date((long) value);

                // Define a simple date format (you can modify the format as needed)
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Format as "Year-Month-Day"

                // Return the formatted date string
                return dateFormat.format(date);
            }
        });
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(4, true);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setTextColor(R.color.atrial);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceMax(4f); // Add extra space to prevent last label from being clipped
        xAxis.setDrawGridLines(false);

        YAxis leftAxis = AfTimeline.getAxisLeft();
        leftAxis.setDrawGridLines(true); // Ensure horizontal grid lines are drawn
        leftAxis.enableGridDashedLine(30f, 20f, 0f); // Dash pattern: 10px line, 5px space
        leftAxis.setAxisMinimum(0f); // Ensure axis starts at 1
        leftAxis.setAxisMaximum(1f); // Ensure axis ends at 4
        leftAxis.setLabelCount(2, true);
        leftAxis.setTextColor(R.color.atrial);

        Map<Integer, String> intensityMap = new HashMap<>();
        intensityMap.put(0, "No");
        intensityMap.put(1, "Yes");
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return intensityMap.get((int) value); // Default to empty string if no match
            }
        });


        YAxis rightAxis = AfTimeline.getAxisRight();
        rightAxis.setEnabled(false); // Disable right Y axis

        AfTimeline.setExtraOffsets(10, 20, 20, 20); // Add padding to the chart
        AfTimeline.getLegend().setEnabled(false); // Hide the legend
        AfTimeline.getDescription().setEnabled(false);
        AfTimeline.getDescription().setTextColor(Color.WHITE);

        AfTimeline.invalidate(); // Refresh chart


        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        Date today = new Date();
        String viewDate = dateFormatter.format(today);



        List<List<String>> day_medications = new ArrayList<>();
        Cursor dayMed = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Medication_Log " +
                        "JOIN Medication ON Medication_Log.medication = Medication.name " +
                        "WHERE patient = ? AND date = ? " +
                        "ORDER BY date DESC, time DESC",
                new String[]{patient, viewDate}
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
