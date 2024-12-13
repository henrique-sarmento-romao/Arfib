package com.example.arfib.Medications;

import static android.app.PendingIntent.getActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.DateList;
import com.example.arfib.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DayMedicationList extends RecyclerView.Adapter<DayMedicationList.MyViewHolder> {
    private final List<List<String>> dayMedications;
    private final Context context;

    public DayMedicationList(Context context, List<List<String>> dayMedications) {
        this.context = context;
        this.dayMedications = dayMedications;
    }

    @Override
    public DayMedicationList.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.daymedicationbutton, parent, false);
        return new DayMedicationList.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(com.example.arfib.Medications.DayMedicationList.MyViewHolder holder, int position) {
        List<String> medication = dayMedications.get(position);

        String med_name = medication.get(0);
        String isTaken = medication.get(1);
        String date = medication.get(2);
        String time = medication.get(3);
        String asset = medication.get(4);

        // asset = asset + ".xml";
        int resId = context.getResources().getIdentifier(asset, "drawable","com.example.arfib");
        holder.medButton.setBackgroundResource(resId);

        if(isTaken.equals("false")){
            int grayColor = context.getResources().getColor(R.color.leadgray); // Getting color from resources
            int transparentGray = Color.argb(200, Color.red(grayColor), Color.green(grayColor), Color.blue(grayColor));

            // Set the background tint with the semi-transparent grey
            holder.medButton.setBackgroundTintList(ColorStateList.valueOf(transparentGray));
            holder.medButton.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);

            holder.goToLog(med_name, date, time);
        }

        holder.medName.setText(med_name);
    }

    @Override
    public int getItemCount() {
        return dayMedications.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton medButton;
        TextView medName;
        public MyViewHolder(View itemView) {
            super(itemView);
            medButton = itemView.findViewById(R.id.medButton);
            medName = itemView.findViewById(R.id.med_name);
        }

        // Method to set the date and handle the button click
        public void goToLog(String med_name, String date, String time) {
            medButton.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Log.class);
                intent.putExtra("med_name", med_name);
                intent.putExtra("date", date);
                intent.putExtra("time", time);  // Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}