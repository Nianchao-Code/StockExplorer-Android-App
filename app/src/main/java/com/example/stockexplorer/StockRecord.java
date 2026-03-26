package com.example.stockexplorer;

/**
 * One row of daily OHLCV data (e.g. Alpha Vantage TIME_SERIES_DAILY).
 */
public class StockRecord {

    private final String symbol;
    private final String date;
    private final double open;
    private final double close;
    private final double high;
    private final double low;
    private final long volume;

    public StockRecord(String symbol, String date, double open, double close,
                       double high, double low, long volume) {
        this.symbol = symbol;
        this.date = date;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
        this.volume = volume;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public long getVolume() {
        return volume;
    }
}
