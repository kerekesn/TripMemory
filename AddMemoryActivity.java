package com.example.kerekesnora.tripmemory;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;

public class AddMemoryActivity extends AppCompatActivity {

    //Members
    private TextView mLocationTextView, mDateTextView;
    private String mLocationString, mDateString;
    private EditText mStoryEditText;
    private ImageButton mLocationButton, mDateButton;
    private int currentYear, currentMonth, currentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memory);

        currentYear = Calendar.getInstance().get(Calendar.YEAR);
        currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        mLocationTextView = (TextView) findViewById(R.id.location_id);
        mDateTextView = (TextView) findViewById(R.id.date_id);

        mDateButton = (ImageButton) findViewById(R.id.img_date);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddMemoryActivity.this,datePickerDialog,currentYear,currentMonth,currentDay).show();
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.img_location);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder locationDialog = new AlertDialog.Builder(AddMemoryActivity.this);
                locationDialog.setTitle("Add location");
                locationDialog.setMessage("Enable to find your location or type manually.");
                locationDialog.setNegativeButton("Cancel",null);
                locationDialog.setNeutralButton("Type manually",null);
                locationDialog.setPositiveButton("Enable", null);
                AlertDialog dialog = locationDialog.create();
                dialog.show();
                
            }
        });
    }

    DatePickerDialog.OnDateSetListener datePickerDialog = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            int mon = (int)(view.getMonth()+1);
            mDateTextView.setText(view.getYear() + "/" + mon + "/" + view.getDayOfMonth());
        }
    };
}
