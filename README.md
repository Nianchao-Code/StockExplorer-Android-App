# StockExplorer (Android)

Android Studio **Java** course project (StockExplorer).

## Running the app (Finnhub API key)

This project reads the Finnhub API key from your local (uncommitted) `local.properties`.

1. Create a Finnhub API key.
2. In the project root, add this line to `local.properties`:

```
FINNHUB_API_KEY=YOUR_KEY_HERE
```

3. Sync Gradle and run the app.

Notes:
- `local.properties` should not be committed.
- If `FINNHUB_API_KEY` is missing, requests will fail and the app will show an API/error message.

