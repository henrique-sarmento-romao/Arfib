package com.example.arfib.Symptoms;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterAllSymptoms extends RecyclerView.Adapter<AdapterAllSymptoms.MyViewHolder> {
    private final List<String> symptomList;
    private final Context context;

    public AdapterAllSymptoms(Context context, List<String> symptomList) {
        this.context = context;
        this.symptomList = symptomList;
    }

    @Override
    public AdapterAllSymptoms.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_list_symptom, parent, false);
        return new AdapterAllSymptoms.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterAllSymptoms.MyViewHolder holder, int position) {
        String symptom = symptomList.get(position);

        Map<String, String> symptomMap = new HashMap<>();
        symptomMap.put("Fatigue", "fatigue");
        symptomMap.put("Breathlessness", "breathlessness");
        symptomMap.put("Dizziness", "dizziness");
        symptomMap.put("Chest Pain", "chest_pain");
        String asset = symptomMap.get(symptom);

        int resId = context.getResources().getIdentifier(asset, "drawable","com.example.arfib");
        holder.symptomImage.setBackgroundResource(resId);

        holder.symptomName.setText(symptom);

        holder.goToLog(symptom);
    }

    @Override
    public int getItemCount() {
        return symptomList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageButton symptomImage;
        TextView symptomName;
        public MyViewHolder(View itemView) {
            super(itemView);
            symptomImage = itemView.findViewById(R.id.symptom_Image);
            symptomName = itemView.findViewById(R.id.symptom_Name);
        }

        // Method to set the date and handle the button click
        public void goToLog(String symptom) {
            symptomImage.setOnClickListener(v -> {
                // Create an Intent to open the Detailed activity
                Intent intent = new Intent(v.getContext(), Details.class);
                intent.putExtra("symptom", symptom);
                v.getContext().startActivity(intent);  // Start the activity
            });
        }
    }
}