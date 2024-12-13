package com.example.arfib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.example.arfib.Professional.HomeDoctor;
import com.example.arfib.Professional.HomeNurse;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Login extends AppCompatActivity {

    private DatabaseHelper dbHelper;

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
        TextInputEditText pass = findViewById(R.id.pw_input);
        TextView error = findViewById(R.id.error_dialog);

        pass.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_SEND) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                }

                String username = user.getText().toString();
                String password = pass.getText().toString();

                dbHelper = new DatabaseHelper(this);
                try{
                    dbHelper.createDatabase();
                    dbHelper.openDatabase();

                    Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                            "SELECT password FROM User WHERE username = ?",
                            new String[]{username}
                    );

                    if (cursor.moveToFirst()) { // Check if the username exists
                        String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));

                        if (storedPassword.equals(password)) {
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
                        } else {
                            error.setText("Username and Password Don't Match");
                        }
                    } else {
                        error.setText("Username Not Found");
                    }
                    cursor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true; // Event handled
            }
            return false; // Let system handle other actions
        });
    }
}
