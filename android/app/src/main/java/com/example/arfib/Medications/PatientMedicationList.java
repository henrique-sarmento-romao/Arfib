package com.example.arfib.Medications;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.R;

import java.util.List;

public class PatientMedicationList extends RecyclerView.Adapter<PatientMedicationList.MyViewHolder>{
    private final List<List<String>> dayMedications;
    private final Context context;

    public PatientMedicationList(Context context, List<List<String>> dayMedications) {
        this.context = context;
        this.dayMedications = dayMedications;
    }

    @Override
    public PatientMedicationList.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_medication, parent, false);
        return new PatientMedicationList.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(com.example.arfib.Medications.PatientMedicationList.MyViewHolder holder, int position) {
        List<String> medication = dayMedications.get(position);

        String med_name = medication.get(0);
        String frequency = medication.get(1);
        String asset = medication.get(2);

        holder.medName.setText(med_name);

        int frequencyHour = Integer.parseInt(frequency);
        int days = frequencyHour / 24;  // Get the number of days
        int remainingHours = frequencyHour % 24;  // Get the remaining hours after dividing by 24

        String frequencyText;
        if(days==0){
            frequencyText = String.format("Every %02dh", remainingHours);
        } else if(remainingHours==0) {
            frequencyText = String.format("Every %dd", days);
        } else {
            frequencyText = String.format("Every %dd %02dh", days, remainingHours);
        }
        holder.medFrequency.setText(frequencyText);

        // asset = asset + ".xml";
        int resId = context.getResources().getIdentifier(asset, "drawable","com.example.arfib");
        holder.medImage.setBackgroundResource(resId);

        holder.goToMedication(med_name);  // Set the date for the button click
    }

    @Override
    public int getItemCount() {
        return dayMedications.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton medImage, arrow;
        TextView medName, medFrequency;
        public MyViewHolder(View itemView) {
            super(itemView);
            medImage = itemView.findViewById(R.id.med_Image);
            arrow = itemView.findViewById(R.id.arrow);
            medName = itemView.findViewById(R.id.medName);
            medFrequency = itemView.findViewById(R.id.medFrequency);
        }

        // Method to set the date and handle the button click
        public void goToMedication(String med_name) {
            medImage.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("med_name", med_name);
                v.getContext().startActivity(intent);  // Start the activity
            });
            arrow.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("med_name", med_name);
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}
