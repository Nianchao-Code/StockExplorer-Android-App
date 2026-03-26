package com.example.stockexplorer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class StockSearchActivity extends AppCompatActivity {

    private EditText symbolInput;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_search);

        symbolInput = findViewById(R.id.symbolInput);
        searchButton = findViewById(R.id.searchButton);
    }
}
