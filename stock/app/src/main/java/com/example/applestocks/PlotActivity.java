package com.example.applestocks;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;

import android.graphics.Color;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.*;
import com.google.gson.*;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

public class PlotActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plot);

        // Set up Edge-to-Edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.plot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.action_bar_2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Plot");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        String url = getIntent().getStringExtra("link");

        fetchAndPlotData(url);

    }

    public void fetchAndPlotData(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace(); // Handle error
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    processJsonData(jsonData);
                }
            }
        });
    }

    public void processJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // Parse time and close arrays
            JSONArray timeArray = jsonObject.getJSONArray("time");
            JSONArray closeArray = jsonObject.getJSONArray("close");

            ArrayList<String> dates = new ArrayList<>();
            ArrayList<Float> closes = new ArrayList<>();

            for (int i = 0; i < timeArray.length(); i++) {
                // Convert timestamp to a readable date format
                long timestamp = timeArray.getLong(i);
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd")
                        .format(new java.util.Date(timestamp * 1000));
                dates.add(date);

                // Get closing price
                closes.add((float) closeArray.getDouble(i));
            }

            // Pass data to the plotting function
            runOnUiThread(() -> plotData(dates, closes));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void plotData(ArrayList<String> dates, ArrayList<Float> closes) {
        LineChart lineChart = findViewById(R.id.lineChart);

        // Prepare data entries for the chart
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < closes.size(); i++) {
            entries.add(new Entry(i, closes.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "AAPL Closing Prices");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Format X-axis with dates
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                // Ensure dates is a List<String> with date strings corresponding to each index
                return dates.get((int) value); // Assuming 'dates' is a List of strings
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);

        lineChart.invalidate(); // Refresh chart
    }
}
