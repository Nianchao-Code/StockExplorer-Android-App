package com.example.stockexplorer;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.stockexplorer.BuildConfig;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockSearchActivity extends AppCompatActivity {

    private static final String FINNHUB_CANDLE_URL = "https://finnhub.io/api/v1/stock/candle";
    private static final String API_KEY = BuildConfig.FINNHUB_API_KEY;

    private static final long SECONDS_PER_DAY = 24L * 3600L;
    /** Request enough daily history to satisfy max spinner value (100). */
    private static final long CANDLE_LOOKBACK_SECONDS = 400L * SECONDS_PER_DAY;

    private static final long VOLUME_FILTER_THRESHOLD = 1_000_000L;

    private EditText symbolInput;
    private Spinner recordCountSpinner;
    private Switch volumeFilterSwitch;
    private Button searchButton;
    private ProgressBar progressBar;
    private TextView errorMessage;
    private RecyclerView resultsRecyclerView;
    private TextView emptyStateText;
    private StockAdapter stockAdapter;

    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();

    private static final String[] RECORD_COUNTS = {"10", "25", "50", "100"};

    /** Latest results for a future RecyclerView adapter (teammate). */
    private List<StockRecord> lastStockResults = new ArrayList<>();

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
        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, RECORD_COUNTS);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordCountSpinner.setAdapter(spinnerAdapter);

        stockAdapter = new StockAdapter(new ArrayList<>());
        if (resultsRecyclerView != null) {
            resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            resultsRecyclerView.setHasFixedSize(true);
            resultsRecyclerView.setAdapter(stockAdapter);
        }

        if (searchButton != null) {
            searchButton.setOnClickListener(v -> onSearchClicked());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkExecutor.shutdown();
    }

    private void onSearchClicked() {
        if (symbolInput == null || recordCountSpinner == null) {
            return;
        }

        String rawSymbol = symbolInput.getText() != null
                ? symbolInput.getText().toString().trim()
                : "";
        if (TextUtils.isEmpty(rawSymbol)) {
            showError(getString(R.string.error_empty_symbol));
            return;
        }

        if (TextUtils.isEmpty(BuildConfig.FINNHUB_API_KEY)) {
            showError(getString(R.string.error_missing_api_key));
            return;
        }

        final String symbol = rawSymbol.toUpperCase(Locale.US);
        int maxRecords;
        try {
            maxRecords = Integer.parseInt((String) recordCountSpinner.getSelectedItem());
        } catch (Exception e) {
            maxRecords = 10;
        }
        final int maxRecordsFinal = maxRecords;

        final boolean volumeFilterOn = volumeFilterSwitch != null && volumeFilterSwitch.isChecked();

        hideError();
        clearResultsForNewSearch();
        setLoading(true);

        networkExecutor.execute(() -> {
            try {
                List<StockRecord> parsed = fetchAndParseCandles(symbol, maxRecordsFinal);
                List<StockRecord> filtered = applyVolumeFilter(parsed, volumeFilterOn);
                runOnUiThread(() -> {
                    if (filtered.isEmpty()) {
                        showError(getString(R.string.error_no_results_after_filter));
                        updateStockList(filtered);
                    } else {
                        hideError();
                        updateStockList(filtered);
                    }
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    String msg = e.getMessage();
                    Integer httpCode = parseHttpCodeFromMessage(msg);
                    if (httpCode != null) {
                        showError(getString(R.string.error_api_http, httpCode));
                    } else {
                        showError(getString(R.string.error_network));
                    }
                    updateStockList(new ArrayList<>());
                });
            } catch (JSONException e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_parse));
                    updateStockList(new ArrayList<>());
                });
            } catch (IllegalStateException e) {
                runOnUiThread(() -> {
                    String msg = e.getMessage();
                    if (TextUtils.isEmpty(msg)) {
                        msg = getString(R.string.error_empty_response);
                    }
                    showError(msg);
                    updateStockList(new ArrayList<>());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showError(getString(R.string.error_unexpected));
                    updateStockList(new ArrayList<>());
                });
            } finally {
                runOnUiThread(() -> setLoading(false));
            }
        });
    }

    /**
     * Clears the current results UI before starting a new request.
     * Keeps the screen clean while loading (no stale results, no empty message).
     */
    private void clearResultsForNewSearch() {
        if (stockAdapter != null) {
            stockAdapter.updateData(new ArrayList<>());
        }
        lastStockResults = new ArrayList<>();
        if (resultsRecyclerView != null) {
            resultsRecyclerView.setVisibility(View.GONE);
        }
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the RecyclerView adapter with new data and toggles empty-state visibility.
     */
    private void updateStockList(List<StockRecord> data) {
        if (data == null) {
            lastStockResults = new ArrayList<>();
        } else {
            lastStockResults = new ArrayList<>(data);
        }

        if (stockAdapter != null) {
            stockAdapter.updateData(lastStockResults);
        }

        boolean hasError = errorMessage != null && errorMessage.getVisibility() == View.VISIBLE
                && !TextUtils.isEmpty(errorMessage.getText());

        if (lastStockResults.isEmpty()) {
            if (resultsRecyclerView != null) {
                resultsRecyclerView.setVisibility(View.GONE);
            }
            if (emptyStateText != null) {
                emptyStateText.setVisibility(hasError ? View.GONE : View.VISIBLE);
            }
        } else {
            if (resultsRecyclerView != null) {
                resultsRecyclerView.setVisibility(View.VISIBLE);
            }
            if (emptyStateText != null) {
                emptyStateText.setVisibility(View.GONE);
            }
        }
    }

    /** For a future RecyclerView adapter: latest rows after search. */
    public List<StockRecord> getLastStockResults() {
        return new ArrayList<>(lastStockResults);
    }

    private List<StockRecord> fetchAndParseCandles(String symbol, int maxRecords)
            throws IOException, JSONException {

        long toSec = System.currentTimeMillis() / 1000L;
        long fromSec = toSec - CANDLE_LOOKBACK_SECONDS;

        String urlString = Uri.parse(FINNHUB_CANDLE_URL).buildUpon()
                .appendQueryParameter("symbol", symbol)
                .appendQueryParameter("resolution", "D")
                .appendQueryParameter("from", String.valueOf(fromSec))
                .appendQueryParameter("to", String.valueOf(toSec))
                .appendQueryParameter("token", API_KEY)
                .build()
                .toString();

        String body = httpGet(urlString);
        if (TextUtils.isEmpty(body)) {
            throw new IllegalStateException(
                    getApplicationContext().getString(R.string.error_empty_response));
        }

        JSONObject json = new JSONObject(body);
        String status = json.optString("s", "");
        if (!"ok".equals(status)) {
            // Finnhub commonly returns {"s":"no_data"} for invalid/unsupported symbols.
            throw new IllegalStateException(getString(R.string.error_no_data));
        }

        JSONArray tArr = json.optJSONArray("t");
        JSONArray oArr = json.optJSONArray("o");
        JSONArray cArr = json.optJSONArray("c");
        JSONArray hArr = json.optJSONArray("h");
        JSONArray lArr = json.optJSONArray("l");
        JSONArray vArr = json.optJSONArray("v");
        if (tArr == null || oArr == null || cArr == null || hArr == null || lArr == null || vArr == null) {
            throw new IllegalStateException(getString(R.string.error_no_data));
        }

        int n = tArr.length();
        if (n == 0) {
            throw new IllegalStateException(
                    getApplicationContext().getString(R.string.error_empty_response));
        }
        if (oArr.length() != n || cArr.length() != n || hArr.length() != n
                || lArr.length() != n || vArr.length() != n) {
            throw new JSONException("Mismatched array lengths");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        List<StockRecord> all = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            long tsSec = tArr.getLong(i);
            double open = oArr.getDouble(i);
            double close = cArr.getDouble(i);
            double high = hArr.getDouble(i);
            double low = lArr.getDouble(i);
            long volume = vArr.getLong(i);
            String dateStr = dateFormat.format(new Date(tsSec * 1000L));
            all.add(new StockRecord(symbol, dateStr, open, close, high, low, volume));
        }

        // Finnhub returns timestamps ascending; keep the most recent maxRecords.
        int fromIndex = Math.max(0, all.size() - maxRecords);
        List<StockRecord> slice = new ArrayList<>(all.subList(fromIndex, all.size()));
        return slice;
    }

    private List<StockRecord> applyVolumeFilter(List<StockRecord> records, boolean enabled) {
        if (!enabled || records == null || records.isEmpty()) {
            return records != null ? records : new ArrayList<>();
        }
        List<StockRecord> out = new ArrayList<>();
        for (StockRecord r : records) {
            if (r.getVolume() > VOLUME_FILTER_THRESHOLD) {
                out.add(r);
            }
        }
        return out;
    }

    /** Parses our synthetic messages like "HTTP 403" from {@link #httpGet}. */
    private static Integer parseHttpCodeFromMessage(String message) {
        if (TextUtils.isEmpty(message) || !message.startsWith("HTTP ")) {
            return null;
        }
        try {
            return Integer.parseInt(message.substring("HTTP ".length()).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String httpGet(String urlString) throws IOException {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = URI.create(urlString).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15_000);
            connection.setReadTimeout(15_000);

            int code = connection.getResponseCode();
            inputStream = code >= 200 && code < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            if (inputStream == null) {
                if (code >= 200 && code < 300) {
                    return "";
                }
                throw new IOException("HTTP " + code);
            }
            String body = readStreamFully(inputStream);
            if (code < 200 || code >= 300) {
                throw new IOException("HTTP " + code);
            }
            return body;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static String readStreamFully(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (searchButton != null) {
            searchButton.setEnabled(!loading);
        }
    }

    private void showError(String message) {
        if (errorMessage != null) {
            errorMessage.setText(message);
            errorMessage.setVisibility(View.VISIBLE);
        }
        if (resultsRecyclerView != null) {
            resultsRecyclerView.setVisibility(View.GONE);
        }
        if (emptyStateText != null) {
            emptyStateText.setVisibility(View.GONE);
        }
    }

    private void hideError() {
        if (errorMessage != null) {
            errorMessage.setText("");
            errorMessage.setVisibility(View.GONE);
        }
    }
}
