package com.example.stockexplorer;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StockSearchActivity extends AppCompatActivity {

    private EditText symbolInput;
    private Spinner recordCountSpinner;
    private Switch volumeFilterSwitch;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView errorMessage;

    private static final String[] RECORD_COUNTS = {"10", "25", "50", "100"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_search);

        symbolInput = findViewById(R.id.symbolInput);
        recordCountSpinner = findViewById(R.id.recordCountSpinner);
        volumeFilterSwitch = findViewById(R.id.volumeFilterSwitch);
        searchButton = findViewById(R.id.searchButton);
        progressBar = findViewById(R.id.progressBar);
        errorMessage = findViewById(R.id.errorMessage);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, RECORD_COUNTS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordCountSpinner.setAdapter(spinnerAdapter);
    }
}
