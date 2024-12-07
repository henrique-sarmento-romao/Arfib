package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import com.example.arfib.Professional.HomeDoctor;
import com.example.arfib.Professional.HomeNurse;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginlayout); // Ensure this points to a valid layout file
        getSupportActionBar().hide();

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String profile = sharedPref.getString("profile", "patient");
        boolean isLoggedIn = sharedPref.getBoolean("is_logged_in", false);
        if(isLoggedIn){
            switch (profile) {
                case "patient":
                    startActivity(new Intent(Login.this, HomePatient.class));
                    break;
                case "doctor":
                    startActivity(new Intent(Login.this, HomeDoctor.class));
                    break;
                case "nurse":
                    startActivity(new Intent(Login.this, HomeNurse.class));
                    break;
            }
        }

        TextInputEditText user = findViewById(R.id.username_input);
        TextInputEditText pass = findViewById(R.id.pw_input); // Ensure this ID exists in login.xml

        pass.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_SEND) {
                String username = user.getText().toString();

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username);
                editor.putBoolean("is_logged_in", true);
                editor.apply();

                switch (profile) {
                    case "patient":
                        startActivity(new Intent(Login.this, HomePatient.class));
                        break;
                    case "doctor":
                        startActivity(new Intent(Login.this, HomeDoctor.class));
                        break;
                    case "nurse":
                        startActivity(new Intent(Login.this, HomeNurse.class));
                        break;
                }
                return true; // Event handled
            }
            return false; // Let system handle other actions
        });
    }
}
