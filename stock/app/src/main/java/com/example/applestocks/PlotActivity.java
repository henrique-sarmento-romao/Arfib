package com.example.applestocks;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.ValueFormatter;

import android.graphics.Color;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PlotActivity extends AppCompatActivity {
    TextView MinClose, MaxClose;
    LineChart Plot;
    String url;

    String selectedChoice;
    int selectedNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plot);

        MinClose = findViewById(R.id.minimumClose);
        MaxClose = findViewById(R.id.maximumClose);

        Plot = findViewById(R.id.lineChart);

        selectedChoice = getIntent().getStringExtra("selecChoice");
        selectedNumber = getIntent().getIntExtra("selecNumber",2);


        // Build the HTTP request URL
        url = "https://api.marketdata.app/v1/stocks/candles/" + selectedChoice + "/AAPL?countback=" + selectedNumber + "&dateformat=timestamp";

        Plot.setNoDataText("Loading data...");

        callExtService();
    }

    public void callExtService() {
        Thread thr = new Thread(new FetchData(url));
        thr.start();
    }

    private void processJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // Parse time and close arrays
            JSONArray timeArray = jsonObject.getJSONArray("t");
            JSONArray closeArray = jsonObject.getJSONArray("c");

            List<Float> timestamps = new ArrayList<>();
            List<Float> closes = new ArrayList<>();

            HashMap<String, String> dateFormatMap = new HashMap<>();
            dateFormatMap.put("H", "yyyy-MM-dd HH:mm:ss Z");
            dateFormatMap.put("D", "yyyy-MM-dd");

            String format = dateFormatMap.get(selectedChoice);
            if (format == null) {
                throw new IllegalArgumentException("Invalid Format: " + selectedChoice);
            }

            float close;

            for (int i = 0; i < timeArray.length(); i++) {
                // Get closing price
                close = (float) closeArray.getDouble(i);
                closes.add(close);

                // Parse ISO 8601 date-time string
                String isoDate = timeArray.getString(i);
                SimpleDateFormat isoFormat = new SimpleDateFormat(format, Locale.getDefault());
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Parse as UTC first
                Date date = isoFormat.parse(isoDate);
                long timestamp = date.getTime();
                float scaledTimestamp = timestamp / 1000f;
                timestamps.add(scaledTimestamp);
            }

            // Pass data to the plotting function
            runOnUiThread(() -> plotData(timestamps, closes));

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            runOnUiThread(() -> {
                Toast.makeText(PlotActivity.this, "Error: Invalid format selection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void plotData(List<Float> dates, List<Float> closes) {
        // Prepare data entries for the chart
        ArrayList<Entry> entries = new ArrayList<>();

        float close, maxClose, minClose;
        maxClose = closes.get(1);
        minClose = closes.get(1);

        for (int i = 0; i < closes.size(); i++) {
            close =  closes.get(i);
            entries.add(new Entry(dates.get(i), closes.get(i)));

            if (close > maxClose) {
                maxClose = close;
            } else if (close < minClose) {
                minClose = close;
            }
        }

        MaxClose.setText(String.valueOf(maxClose));
        MinClose.setText(String.valueOf(minClose));

        ArrayList<Entry> maxBar = new ArrayList<>();
        ArrayList<Entry> minBar = new ArrayList<>();

        for (int i = 0; i < closes.size(); i++) {
            maxBar.add(new Entry(dates.get(i), maxClose));
            minBar.add(new Entry(dates.get(i), minClose));
        }

        LineDataSet dataSet = new LineDataSet(entries, "AAPL Closing Prices");
        dataSet.setColor(Color.parseColor("#A340F2"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(5f);
        dataSet.setCircleColor(Color.parseColor("#A340F2"));
        dataSet.setCircleHoleColor(Color.BLACK);
        dataSet.setDrawValues(false);

        LineDataSet maxDataSet = new LineDataSet(maxBar, "Maximum Price");
        maxDataSet.setColor(Color.GRAY);
        maxDataSet.setLineWidth(2f);
        maxDataSet.enableDashedLine(30,30,0);
        maxDataSet.setDrawValues(false);
        maxDataSet.setDrawCircles(false);


        LineDataSet minDataSet = new LineDataSet(minBar, "Minimum Price");
        minDataSet.setColor(Color.GRAY);
        minDataSet.setLineWidth(2f);
        minDataSet.enableDashedLine(30,30,0);
        minDataSet.setDrawValues(false);
        minDataSet.setDrawCircles(false);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(dataSet);
            dataSets.add(maxDataSet);
            dataSets.add(minDataSet);

        LineData lineData = new LineData(dataSets);
            Plot.setData(lineData);

        // Format X-axis with dates
        XAxis xAxis = Plot.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // If the value is in seconds, convert it to milliseconds
                long timestamp = (long) (value * 1000f);  // Convert the float timestamp to milliseconds

                // Create a Date object using the timestamp
                Date date = new Date(timestamp);

                // Define a simple date format (you can modify the format as needed)
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy, HH:mm", Locale.getDefault()); // Format as "Year-Month-Day"

                // Return the formatted date string
                return dateFormat.format(date);
            }
        });
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(2, true);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(0);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setSpaceMax(4f); // Add extra space to prevent last label from being clipped


        YAxis rightAxis = Plot.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);

        Legend legend = Plot.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        Plot.setExtraOffsets(0, 50, 10, 15); // Add padding to the chart
        Plot.getDescription().setEnabled(true);
        Plot.getDescription().setText("Stock Quotes Over Time");
        Plot.getDescription().setTextColor(Color.WHITE);

        Plot.invalidate(); // Refresh chart
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder respBuilder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                respBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return respBuilder.toString();
    }

    private class FetchData implements Runnable {
        private final String url;

        FetchData(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            HttpURLConnection urlConnection = null;

            try {
                URL endpoint = new URL(url);
                urlConnection = (HttpURLConnection) endpoint.openConnection();
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setUseCaches(false);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == 200) {
                    String response = readStream(urlConnection.getInputStream());

                    // Pass JSON data for processing
                    processJsonData(response);

                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(PlotActivity.this, "Error: HTTP response code " + responseCode, Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(PlotActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }
}
