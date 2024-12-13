package com.example.arfib.Measurements;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Notifications;
import com.example.arfib.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MeasurementList extends RecyclerView.Adapter<MeasurementList.MyViewHolder> {

    private final String username;
    private List<List<String>> dataList;
    private Context context;

    public MeasurementList(Context context, List<List<String>> dataList, String username) {
        this.context = context;
        this.dataList = dataList;
        this.username = username;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.measurement_in_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        List<String> data = dataList.get(position);
        String date, time;
        date = data.get(0);
        time = data.get(1);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormatterDate = new SimpleDateFormat("MMM dd yyyy");
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.SSSSSS");
        SimpleDateFormat outputFormatterTime = new SimpleDateFormat("HH:mm");

        String date_time = "";
        try{
            Date parsedDate = dateFormatter.parse(date);
            Date parsedTime = timeFormatter.parse(time);

            String formattedDate = outputFormatterDate.format(parsedDate);
            String formattedTime = outputFormatterTime.format(parsedTime);

            // Final combined output
            date_time = formattedDate + ", " + formattedTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.date_time.setText(date_time);

        String AF_presence, has_AF_message;
        AF_presence = data.get(2);
        if (AF_presence.equals("yes")) {
            has_AF_message = "AF Detected";
        } else if (AF_presence.equals("no")){
            has_AF_message = "No AF Detected";
        } else {
            has_AF_message = "No AF Information";
        }
        holder.AF_presence.setText(has_AF_message);

        // Pass data to the holder's button click listener
        holder.goToDetailed(username, date, time);  // Set the date for the button click
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView date_time, AF_presence;
        Button actionButton;

        public MyViewHolder(View itemView) {
            super(itemView);
            date_time = itemView.findViewById(R.id.date_time);
            AF_presence = itemView.findViewById(R.id.AF_presence);
            actionButton = itemView.findViewById(R.id.button);
        }

        // Method to set the date and handle the button click
        public void goToDetailed(String username, String date, String time) {
            actionButton.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Detailed.class);
                intent.putExtra("patient", username);
                intent.putExtra("date", date);
                intent.putExtra("time", time); // Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}