package com.example.applestocks;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;

import android.annotation.SuppressLint;
import android.graphics.Color;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class PlotActivity extends AppCompatActivity {
    TextView Txt, Arrays;
    LineChart Plot;
    String url;

    // Get the URL from the Intent
    String selectedChoice = getIntent().getStringExtra("selecChoice");
    String selectedNumber = getIntent().getStringExtra("selecNumber");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plot);

        Txt = findViewById(R.id.textView);
        Plot = findViewById(R.id.lineChart);
        Arrays = findViewById(R.id.jsonValues);

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

        // Build the HTTP request URL
        String url = "https://api.marketdata.app/v1/stocks/candles/" + selectedChoice + "/AAPL?countback=" + selectedNumber + "&dateformat=timestamp";

        Txt.setText("Fetching data from: " + url);

        callExtService();
    }

    public void callExtService() {
        Thread thr = new Thread(new FetchData(url));
        thr.start();
    }

    private void writeJson(final String json) {
        runOnUiThread(() -> Txt.setText(json));
    }

    private void processJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);

            // Parse time and close arrays
            JSONArray timeArray = jsonObject.getJSONArray("t");
            JSONArray closeArray = jsonObject.getJSONArray("c");

            ArrayList<String> dates = new ArrayList<>();
            ArrayList<Float> closes = new ArrayList<>();

            HashMap<String, String> dateFormatMap = new HashMap<>();
            dateFormatMap.put("H", "yyyy-MM-dd HH:mm:ss Z");
            dateFormatMap.put("D", "yyyy-MM-dd");

            String format = dateFormatMap.get(selectedChoice);
            if (format == null) {
                throw new IllegalArgumentException("Invalid Format: " + selectedChoice);
            }

            for (int i = 0; i < timeArray.length(); i++) {
                // Parse ISO 8601 date-time string
                String isoDate = timeArray.getString(i);
                SimpleDateFormat isoFormat = new SimpleDateFormat(format, Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Parse as UTC first
                Date date = isoFormat.parse(isoDate);

                // Format to desired output (e.g., "yyyy-MM-dd")
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                String formattedDate = outputFormat.format(date);

                dates.add(formattedDate);

                // Get closing price
                closes.add((float) closeArray.getDouble(i));
            }

            // Pass data to the plotting function
            runOnUiThread(() -> plotData(dates, closes));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException f) {
            f.printStackTrace();
        } catch (IllegalArgumentException e) {
            writeJson("Error: Invalid format selection: " + e.getMessage());
        }
    }

    private void plotData(ArrayList<String> dates, ArrayList<Float> closes) {
        // Prepare data entries for the chart
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < closes.size(); i++) {
            entries.add(new Entry(i, closes.get(i)));
        }

        LineDataSet dataSet = new LineDataSet(entries, "AAPL Closing Prices");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        Plot.setData(lineData);

        // Format X-axis with dates
        XAxis xAxis = Plot.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = Math.round(value);
                return (index >= 0 && index < dates.size()) ? dates.get(index) : "";
            }
        });
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(45);

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
                    writeJson(response);

                    // Pass JSON data for processing
                    processJsonData(response);

                } else {
                    writeJson("Error: HTTP response code " + responseCode);
                }
            } catch (Exception e) {
                writeJson("Error: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }
}
