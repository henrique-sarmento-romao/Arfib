package com.example.applestocks;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Apple Stocks");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));


        Button button_submit=findViewById(R.id.btn_submit);
        Spinner spinnerChoice = findViewById(R.id.spinner_choice);
        Spinner spinnerNumber = findViewById(R.id.spinner_number);
        // Set up Toolbar




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
