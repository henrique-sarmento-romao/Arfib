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


    public AdapterMeasurements(Context context, List<List<String>> dataList, String patient) {
        this.context = context;
        this.dataList = dataList;
        this.patient = patient;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_measurement, parent, false);
        return new MyViewHolder(view);
    }



    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        List<String> data = dataList.get(position);
        String date, time;
        date = data.get(0);
        time = data.get(1);



        dbHelper = new DatabaseHelper(context);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("SELECT * FROM Measurement WHERE patient='" + patient + "' AND date='" + date + "' AND time='" + time + "' ORDER BY time DESC", null);

        if (cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndex("file"));
        }
        cursor.close();

        // Formatar a data e hora
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

        // Atualizar a presença de AF (Fibrilação Atrial)
        String AF_presence, has_AF_message;
        AF_presence = data.get(2);
        if (AF_presence.equals("yes")) {
            has_AF_message = "AF Detected";
        } else if (AF_presence.equals("no")) {
            has_AF_message = "No AF Detected";
        } else {
            has_AF_message = "No AF Information";
        }
        holder.AF_presence.setText(has_AF_message);

        ArrayList<Entry> chartData = readTxtFile(path); // Lê o arquivo de dados
        configureChart(holder.chart);
        updateChart(holder.chart, chartData); // Atualiza o gráfico com os dados

        // Configurar o clique do gráfico para navegar até a tela detalhada
        holder.goToDetailed(patient, date, time);
    }

    private float mapToMV(float x, float minIn, float maxIn, float minOut, float maxOut) {
        return minOut + ((x - minIn) * (maxOut - minOut)) / (maxIn - minIn);
    }

    private float mapToTime(float x, float sampleRate) {

        return x / sampleRate;
    }
    private ArrayList<Entry> readTxtFile(String filePath) {
        ArrayList<Entry> data = new ArrayList<>();
        File file = new File(context.getFilesDir() + File.separator + filePath);

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

    // Método para atualizar o gráfico
    private void updateChart(LineChart chart, ArrayList<Entry> data) {
        // Aqui você pode mapear os dados se necessário e configurar o gráfico
        float minIn = 0f;
        float maxIn = 230f;   // Maximum of bit
        float minOut = -1.15f; // Min in mV (Y)
        float maxOut = 1.15f;  // Max in mV (Y)

        ArrayList<Entry> mappedData = new ArrayList<>();

        for (Entry entry : data) {
            float mappedX = mapToTime(entry.getX(), 500);  // sample rate=500

            // Map Y (ECG) to mV
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
        chart.invalidate(); // Atualiza o gráfico
    }

    // Método para configurar o gráfico
    private void configureChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setVisibleXRangeMaximum(1000); // Limita a visualização a 1000 pontos
        chart.setDragDecelerationEnabled(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(false);
        //leftAxis.setAxisMinimum(-1.15f); // Mínimo fixo
        //leftAxis.setAxisMaximum(1.15f);  // Máximo fixo
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
        Button actionButton;
        private LineChart chart;
        public MyViewHolder(View itemView) {
            super(itemView);
            date_time = itemView.findViewById(R.id.date_time);
            AF_presence = itemView.findViewById(R.id.AF_presence);
            chart = itemView.findViewById(R.id.chartECG_detailed);
        }




        // Method to set the date and handle the button click
        public void goToDetailed(String patient, String date, String time) {
            chart.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("patient", patient);
                intent.putExtra("date", date);
                intent.putExtra("time", time); // Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}