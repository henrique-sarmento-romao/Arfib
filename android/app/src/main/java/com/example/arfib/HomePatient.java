package com.example.arfib;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.example.arfib.Measurements.BluetoothActivity;
import com.example.arfib.Measurements.Home;


public class HomePatient extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepagepatient);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setIcon(R.drawable.ic_menu_icon);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> {
            Intent bluetoothIntent = new Intent(HomePatient.this, BluetoothActivity.class);
            startActivity(bluetoothIntent);
        });

        TextView welcome = findViewById(R.id.welcome);
        welcome.setText("👋 Welcome, "+username+"!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the ActionBar
        getMenuInflater().inflate(R.menu.page_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu) {
            // Show PopupMenu when "Notifications" is clicked
            showHorizontalPopupMenu(item);
            return true;
        } else if (id == R.id.notifications) {
            Intent intent = new Intent(HomePatient.this, Notifications.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.measurements) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Measurements.Home.class);
            intent.putExtra("username", "lauraalves30");
            startActivity(intent);
            return true;
        } else if (id == R.id.symptoms) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Symptoms.Home.class);
            intent.putExtra("username", "lauraalves30");
            startActivity(intent);
            return true;
        } else if (id == R.id.medications) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Medications.Home.class);
            intent.putExtra("username", "lauraalves30");
            startActivity(intent);
            return true;
        } else if (id == R.id.blog) {
            Intent intent = new Intent(HomePatient.this, com.example.arfib.Blog.Home.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showHorizontalPopupMenu(MenuItem item) {
        // Create a PopupMenu for the notifications item
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.menu));

        // Inflate the menu for PopupMenu, using the same or different menu XML
        getMenuInflater().inflate(R.menu.page_menu, popupMenu.getMenu());

        // Customize the appearance of the PopupMenu (e.g., making it horizontal)
        popupMenu.setGravity(Gravity.START);

        // Show the menu
        popupMenu.show();
    }
}
