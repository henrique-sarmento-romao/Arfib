package com.example.arfib.Symptoms;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.Medications.Log;
import com.example.arfib.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DaySymptomList  extends RecyclerView.Adapter<DaySymptomList.MyViewHolder> {
    private final List<List<String>> daySymptoms;
    private final Context context;

    public DaySymptomList(Context context, List<List<String>> daySymptoms) {
        this.context = context;
        this.daySymptoms = daySymptoms;
    }

    @Override
    public DaySymptomList.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.symptom_in_list, parent, false);
        return new DaySymptomList.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DaySymptomList.MyViewHolder holder, int position) {
        List<String> symptom = daySymptoms.get(position);

        String symptom_name = symptom.get(0);
        String time = symptom.get(1);
        String intensity = symptom.get(2);

        Map<String, String> symptomMap = new HashMap<>();
        symptomMap.put("Fatigue", "fatigue");
        symptomMap.put("Breathlessness", "breathlessness");
        symptomMap.put("Dizziness", "dizziness");
        symptomMap.put("Chest Pain", "chest_pain");

        String asset = symptomMap.get(symptom_name);

        // asset = asset + ".xml";
        int resId = context.getResources().getIdentifier(asset, "drawable","com.example.arfib");
        holder.symptomButton.setBackgroundResource(resId);

        holder.symptomName.setText(symptom_name);
        holder.symptomIntensity.setText(intensity);
        holder.symptomTime.setText(time);

        holder.goToSymptom(symptom_name);
    }

    @Override
    public int getItemCount() {
        return daySymptoms.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton symptomButton;
        TextView symptomName, symptomIntensity, symptomTime;
        public MyViewHolder(View itemView) {
            super(itemView);
            symptomButton = itemView.findViewById(R.id.symptom_Image);
            symptomName = itemView.findViewById(R.id.symptom_Name);
            symptomIntensity = itemView.findViewById(R.id.symptom_Intensity);
            symptomTime = itemView.findViewById(R.id.time);
        }

        // Method to set the date and handle the button click
        public void goToSymptom(String symptom_name) {
            symptomButton.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("symptom", symptom_name);// Pass the date as an extra
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}