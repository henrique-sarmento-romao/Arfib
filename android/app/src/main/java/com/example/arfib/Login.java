package com.example.arfib;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import com.google.android.material.textfield.TextInputEditText;

import androidx.activity.EdgeToEdge;
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
                    startActivity(new Intent(Login.this, HomePagePatient.class));
                } else if ("d".equals(chosen)) {
                    startActivity(new Intent(Login.this, HomePageDoctor.class));
                } else if ("n".equals(chosen)) {
                    startActivity(new Intent(Login.this, HomePageNurse.class));
                }
                return true; // Event handled
            }
            return false; // Let system handle other actions
        });
    }
}
