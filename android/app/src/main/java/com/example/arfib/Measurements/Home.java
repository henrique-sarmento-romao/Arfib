package com.example.arfib.Measurements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.example.arfib.HomePatient;
import com.example.arfib.Notifications;
import com.example.arfib.R;

import android.database.Cursor;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.DatabaseHelper;
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

public class Home extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.measurementhome);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String patient = sharedPref.getString("username", "");

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

        dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDatabase();
            dbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

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


        List<List<String>> dataList = new ArrayList<>();
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Measurement WHERE patient='"+ patient +"' ORDER BY date DESC, time DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex("date"));
                String time = cursor.getString(cursor.getColumnIndex("time"));

                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex("file"));
                @SuppressLint("Range") int AF_presence = cursor.getInt(cursor.getColumnIndex("AF_presence"));
                String has_AF;
                if (AF_presence == 1) {
                    has_AF = "yes";
                } else {
                    has_AF = "no";
                }
                // Create a list representing one "array" of strings
                List<String> singleArray = new ArrayList<>();
                singleArray.add(date);
                singleArray.add(time);
                singleArray.add(has_AF);
                singleArray.add(path);

                // Add this "array" to the main list
                dataList.add(singleArray);

            } while (cursor.moveToNext());
        }
        cursor.close();


        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        MeasurementList adapter = new MeasurementList(this, dataList, patient);
        recyclerView.setAdapter(adapter);
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
            Intent intent = new Intent(Home.this, Notifications.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.measurements) {
            Intent intent = new Intent(Home.this, com.example.arfib.Measurements.Home.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.symptoms) {
            Intent intent = new Intent(Home.this, com.example.arfib.Symptoms.Home.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.medications) {
            Intent intent = new Intent(Home.this, com.example.arfib.Medications.Home.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.blog) {
            Intent intent = new Intent(Home.this, com.example.arfib.Blog.Home.class);
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

