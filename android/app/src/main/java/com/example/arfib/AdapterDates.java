package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterDates extends RecyclerView.Adapter<AdapterDates.MyViewHolder> {

    private final String username;
    private final String selectedDate;
    private final List<List<String>> dateList;
    private final Context context;

    public AdapterDates(Context context, List<List<String>> dateList, String username, String selectedDate) {
        this.context = context;
        this.dateList = dateList;
        this.selectedDate = selectedDate;
        this.username = username;
    }

    @Override
    public AdapterDates.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_date, parent, false);
        return new AdapterDates.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterDates.MyViewHolder holder, int position) {

        List<String> date_time = dateList.get(position);

        String date = date_time.get(0);

        SimpleDateFormat inputFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormatter = new SimpleDateFormat("E,\nMMM d", Locale.getDefault());

        String formattedDate = "";
        try{
            Date parsedDate = inputFormatter.parse(date);
            formattedDate = outputFormatter.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.dateButton.setText(formattedDate);

        // Change the button background color based on selection
        if (date.equals(selectedDate)) {
            holder.dateButton.setBackgroundResource(R.drawable.dark_date_button); // Set the background from a drawable resource // Use your selected color
        } else {
            holder.dateButton.setBackgroundResource(R.drawable.light_date_button); // Default background
        }

        holder.goToDate(username, date);  // Set the date for the button click
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
                Intent intent = new Intent(v.getContext(), v.getContext().getClass());
                intent.putExtra("username", username);
                intent.putExtra("date", date);  // Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}
