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

import java.util.List;

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
        holder.date_time.setText(data.get(0));
        holder.AF_presence.setText(data.get(1));

        // Pass data to the holder's button click listener
        holder.goToDetailed(username, data.get(0));  // Set the date for the button click
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
        public void goToDetailed(String username, String date) {
            actionButton.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Detailed.class);
                intent.putExtra("username", username);
                intent.putExtra("date", date);  // Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}