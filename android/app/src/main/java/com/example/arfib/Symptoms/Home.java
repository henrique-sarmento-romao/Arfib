package com.example.arfib.Symptoms;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.example.arfib.R;

public class Home extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptomshome);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Symptoms");
        ColorDrawable yellow = new ColorDrawable(ContextCompat.getColor(this, R.color.symptompurple));
        actionBar.setBackgroundDrawable(yellow);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.symptompurple));
        }

        HorizontalScrollView scrollView = findViewById(R.id.scroll);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_RIGHT));


        TextView todayDateTextView = findViewById(R.id.today_date);
        // Data atual
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE,dd/MM", Locale.getDefault()); // Inclui o dia da semana
        String currentDate = dateFormat.format(calendar.getTime());

        // Define o texto da TextView para a data de hoje
        todayDateTextView.setText("Today, " + currentDate);

        // --------------------------
        // Zona horizontal com datas
        // --------------------------
                TextView today = findViewById(R.id.today);
        today.setText(currentDate);

        // TextViews correspondentes para os dias
        TextView yesterday = findViewById(R.id.today_1);
        TextView today_2 = findViewById(R.id.today_2);
        TextView today_3 = findViewById(R.id.today_3);
        TextView today_4 = findViewById(R.id.today_4);

        // Método para obter a data de um dia específico
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, dd/MM", Locale.getDefault());
        Calendar tempCalendar = Calendar.getInstance(); // Usar uma cópia para evitar modificações cumulativas

        // Define as datas decrementando os dias
        tempCalendar.add(Calendar.DAY_OF_YEAR, -1);
        yesterday.setText(dayFormat.format(tempCalendar.getTime()));

        tempCalendar = Calendar.getInstance();
        tempCalendar.add(Calendar.DAY_OF_YEAR, -2);
        today_2.setText(dayFormat.format(tempCalendar.getTime()));

        tempCalendar = Calendar.getInstance();
        tempCalendar.add(Calendar.DAY_OF_YEAR, -3);
        today_3.setText(dayFormat.format(tempCalendar.getTime()));

        tempCalendar = Calendar.getInstance();
        tempCalendar.add(Calendar.DAY_OF_YEAR, -4);
        today_4.setText(dayFormat.format(tempCalendar.getTime()));


    }

}
