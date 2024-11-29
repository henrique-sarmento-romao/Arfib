package com.example.arfib;

import android.content.Intent;
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
        getSupportActionBar().setTitle("");
        Intent intent = getIntent();
        String chosen = intent.getStringExtra("chosen");

        TextInputEditText pw = findViewById(R.id.pw_input); // Ensure this ID exists in login.xml
        pw.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_SEND) {
                if ("p".equals(chosen)) {
                    startActivity(new Intent(Login.this, HomePatient.class));
                } else if ("d".equals(chosen)) {
                    startActivity(new Intent(Login.this, HomeDoctor.class));
                } else if ("n".equals(chosen)) {
                    startActivity(new Intent(Login.this, HomeNurse.class));
                }
                return true; // Event handled
            }
            return false; // Let system handle other actions
        });
    }
}
