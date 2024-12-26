package com.example.arfib.Measurements;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.DatabaseHelper;
import com.example.arfib.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdapterMeasurements extends RecyclerView.Adapter<AdapterMeasurements.MyViewHolder> {
    private DatabaseHelper dbHelper;
    private final String patient;
    private List<List<String>> dataList;
    private Context context;
    private String path;


    // Constructor to initialize adapter with context, data list, and patient
    public AdapterMeasurements(Context context, List<List<String>> dataList, String patient) {
        this.context = context;
        this.dataList = dataList;
        this.patient = patient;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_measurement, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Get data for the current position
        List<String> data = dataList.get(position);
        String date = data.get(0);
        String time = data.get(1);

        // Query the database for the file path of the measurement
        dbHelper = new DatabaseHelper(context);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "SELECT * FROM Measurement WHERE patient='" + patient + "' AND date='" + date + "' AND time='" + time + "' ORDER BY time DESC",
                null
        );

        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex("file"));
        }
        cursor.close();

        // Format date and time
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormatterDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("HH:mm");

        String date_time = "";
        try {
            Date parsedDate = dateFormatter.parse(date);
            Date parsedTime = timeFormatter.parse(time);

            String formattedDate = outputFormatterDate.format(parsedDate);
            String formattedTime = outputFormatterTime.format(parsedTime);

            date_time = formattedDate + ", " + formattedTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.date_time.setText(date_time);

        // Update AF (Atrial Fibrillation) status
        String AF_presence = data.get(2);
        String has_AF_message;
        if (AF_presence.equals("yes")) {
            has_AF_message = "AF Detected";
        } else if (AF_presence.equals("no")) {
            has_AF_message = "No AF Detected";
        } else {
            has_AF_message = "No AF Information";
        }
        holder.AF_presence.setText(has_AF_message);

        // Read the ECG data and update the chart
        ArrayList<Entry> chartData = readTxtFile(path);
        configureChart(holder.chart);
        updateChart(holder.chart, chartData);

        // Set a click listener for detailed view
        holder.goToDetailed(patient, date, time);
    }

    // Maps output to mV
    private float mapToMV(float x, float minIn, float maxIn, float minOut, float maxOut) {
        return minOut + ((x - minIn) * (maxOut - minIn)) / (maxIn - minIn);
    }

    // Maps input to time
    private float mapToTime(float x, float sampleRate) {
        return x / sampleRate;
    }

    // Reads the data file and converts it into a list of entries for the chart
    private ArrayList<Entry> readTxtFile(String filePath) {
        ArrayList<Entry> data = new ArrayList<>();
        File file = new File(context.getFilesDir() + File.separator + filePath);


        // Reads the ECG .txt file
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int index = 0;
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(" ");
                    for (String value : values) {
                        try {
                            float yValue = Float.parseFloat(value.trim());
                            data.add(new Entry(index++, yValue));
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Error processing: " + value, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error reading file: " + filePath, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "File not found: " + filePath, Toast.LENGTH_LONG).show();
        }
        return data;
    }

    // Updates the chart with mapped data
    private void updateChart(LineChart chart, ArrayList<Entry> data) {
        float minIn = 0f;
        float maxIn = 230f;
        float minOut = -1.15f;
        float maxOut = 1.15f;

        ArrayList<Entry> mappedData = new ArrayList<>();
        for (Entry entry : data) {
            float mappedX = mapToTime(entry.getX(), 500); // Sample rate is 500
            float mappedY = mapToMV(entry.getY(), minIn, maxIn, minOut, maxOut);
            mappedData.add(new Entry(mappedX, mappedY));
        }

        LineDataSet dataSet = new LineDataSet(mappedData, "ECG Data");
        dataSet.setColor(ContextCompat.getColor(context, R.color.hartpink));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart
    }

    // Configures the chart appearance
    private void configureChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setVisibleXRangeMaximum(1000);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);

        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date_time, AF_presence;
        private LineChart chart;

        public MyViewHolder(View itemView) {
            super(itemView);
            date_time = itemView.findViewById(R.id.date_time);
            AF_presence = itemView.findViewById(R.id.AF_presence);
            chart = itemView.findViewById(R.id.chartECG_detailed);
        }

        // Handles click events to navigate to detailed view
        public void goToDetailed(String patient, String date, String time) {
            chart.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("patient", patient);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                v.getContext().startActivity(intent);
            });
        }
    }
}