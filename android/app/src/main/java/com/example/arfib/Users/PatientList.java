package com.example.arfib.Users;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.arfib.R;

import java.util.List;

public class PatientList extends RecyclerView.Adapter<PatientList.MyViewHolder>{
private final List<List<String>> patientList;
private final Context context;

public PatientList(Context context, List<List<String>> patientList) {
    this.context = context;
    this.patientList = patientList;
}

@Override
public PatientList.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.recycler_list_patient, parent, false);
    return new PatientList.MyViewHolder(view);
}

@Override
public void onBindViewHolder(PatientList.MyViewHolder holder, int position) {
    List<String> patient = patientList.get(position);

    String username = patient.get(0);
    String first_name = patient.get(1);
    String last_name = patient.get(2);
    String asset = patient.get(3);

    holder.Name.setText(first_name + " " +last_name);
    holder.Username.setText(username);

    holder.goToPatient(username);  // Set the date for the button click
}

@Override
public int getItemCount() {
    return patientList.size();
}

public static class MyViewHolder extends RecyclerView.ViewHolder {
    ImageButton Image, Arrow;
    TextView Name, Username;
    public MyViewHolder(View itemView) {
        super(itemView);
        Image = itemView.findViewById(R.id.image);
        Arrow = itemView.findViewById(R.id.arrow);
        Name = itemView.findViewById(R.id.name);
        Username = itemView.findViewById(R.id.username);
    }

    // Method to set the date and handle the button click
    public void goToPatient(String username) {
        Image.setOnClickListener(v -> {
            // Create an Intent to open the Detailed activity
            Intent intent = new Intent(v.getContext(), HomePatient.class);
            SharedPreferences sharedPref = v.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("patient", username);
            editor.apply();
            v.getContext().startActivity(intent);  // Start the activity
        });
        Arrow.setOnClickListener(v -> {
            // Create an Intent to open the Detailed activity
            Intent intent = new Intent(v.getContext(), HomePatient.class);
            SharedPreferences sharedPref = v.getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("patient", username);
            editor.apply();
            v.getContext().startActivity(intent);  // Start the activity
        });
    }
}
}
