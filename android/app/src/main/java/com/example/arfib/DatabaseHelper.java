package com.example.arfib;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.db"; // Name of the pre-populated DB
    private static final int DATABASE_VERSION = 1; // Version number (no upgrade logic needed here)
    private final Context context;
    private SQLiteDatabase database;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // You won't actually create anything here, because the database is prepopulated
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed, for now it's not used because we don't alter the DB
    }

    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();

        if (!dbExist) {
            // By calling this method, we create the empty database in the default system path
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkDatabase() {
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    private void copyDatabase() throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream inputStream = assetManager.open(DATABASE_NAME);
        String outFileName = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        OutputStream outputStream = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
    }

    public SQLiteDatabase openDatabase() throws SQLException {
        String dbPath = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
        database = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        return database;
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
        }
        super.close();
    }

    public void insertFile(String filename, String patient, Integer AF_presence, Context context) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("file", filename);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSSSSS", Locale.getDefault());
            String formattedDate = dateFormat.format(new Date()); // Data e hora atual formatada
            String formattedTime = timeFormat.format(new Date()); // Data e hora atual formatada

            values.put("date", formattedDate);
            values.put("time", formattedTime);
            values.put("AF_presence", AF_presence); // Sempre insere como 0
            values.put("patient", patient);

            long result = db.insert("Measurement", null, values);

        } catch (Exception e) {
            Toast.makeText(context, "Error inserting file metadata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void logMedication(Context context, String username, String med_name, String date, String time) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("taken", 1);

            String whereClause = "patient = ? AND medication = ? AND date = ? AND time = ?";
            String[] whereArgs = {username, med_name, date, time};

            long result = db.update("Medication_Log", values, whereClause, whereArgs);
        } catch (Exception e) {
            Toast.makeText(context, "Error inserting file metadata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void logSymptom(View context, String patient, String symptom, int intensity, String date, String time) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put("patient", patient);
            values.put("symptom", symptom);
            values.put("intensity", intensity);
            values.put("date", date);
            values.put("time", time);

            long result = db.insert("Symptom_Log", null,values);

        } catch (Exception e) {
            Toast.makeText(context.getContext(), "Error inserting file metadata: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


}