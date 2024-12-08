package com.example.arfib;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Measurements.Detailed;
import com.example.arfib.Medications.Home;
import com.example.arfib.Notifications;
import com.example.arfib.R;

import java.util.List;

public class DateList extends RecyclerView.Adapter<DateList.MyViewHolder> {

    private final String username;
    private final String selectedDate;
    private List<String> dateList;
    private Context context;

    public DateList(Context context, List<String> dateList, String username, String selectedDate) {
        this.context = context;
        this.dateList = dateList;
        this.selectedDate = selectedDate;
        this.username = username;
    }

    @Override
    public com.example.arfib.DateList.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.datebutton, parent, false);
        return new com.example.arfib.DateList.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(com.example.arfib.DateList.MyViewHolder holder, int position) {
        String date = dateList.get(position);
        holder.dateButton.setText(date);
        // Change the button background color based on selection
        if (date.equals(selectedDate)) {
            holder.dateButton.setBackgroundResource(R.drawable.dark_date_button); // Set the background from a drawable resource // Use your selected color
        }

        holder.goToDate(username, dateList.get(position));  // Set the date for the button click
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button dateButton;
        public MyViewHolder(View itemView) {
            super(itemView);
            dateButton = itemView.findViewById(R.id.date);
        }

        // Method to set the date and handle the button click
        public void goToDate(String username, String date) {
            dateButton.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Home.class);
                intent.putExtra("username", username);
                intent.putExtra("date", date);  // Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}
