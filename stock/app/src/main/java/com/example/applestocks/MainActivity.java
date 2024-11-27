package com.example.applestocks;

import android.content.Intent;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinnerChoice = findViewById(R.id.spinner_choice);
        Spinner spinnerNumber = findViewById(R.id.spinner_number);
        Button button_submit=findViewById(R.id.btn_submit);



        ArrayAdapter<String> choiceAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_item, new String[]{"H", "D"});
        choiceAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerChoice.setAdapter(choiceAdapter);

        ArrayAdapter<Integer> numberAdapter = new ArrayAdapter<>(this,
           R.layout.spinner_item, new Integer[]{2, 3, 4, 5, 6, 7, 8, 9, 10});
        numberAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerNumber.setAdapter(numberAdapter);

        spinnerChoice.setSelection(0); // Default to "H" (index 0)
        spinnerNumber.setSelection(0); // Default to "2" (index 0)

        // Handle button click
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedChoice = spinnerChoice.getSelectedItem().toString();
                int selectedNumber = (Integer) spinnerNumber.getSelectedItem();

                // Create the Intent to send data to PlotActivity
                Intent intent = new Intent(MainActivity.this, PlotActivity.class);

                // Pass the URL as an extra
                intent.putExtra("selecChoice", selectedChoice);
                intent.putExtra("selecNumber", selectedNumber);

                // Start the PlotActivity
                startActivity(intent);
            }
        });
    }
}
