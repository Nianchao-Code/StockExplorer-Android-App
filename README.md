# StockExplorer (Android)

Android Studio **Java** course project (StockExplorer).

## Running the app (Finnhub API key)

This project reads the Finnhub API key from your local (uncommitted) `local.properties`.

1. In the Finnhub dashboard, copy the **API Key** (use the eye/copy icons).  
   Do **not** use the **Webhook secret** (that is only for webhooks).
2. In the project root, add **exactly one line** to `local.properties` (no quotes, no spaces around `=`):

```
FINNHUB_API_KEY=YOUR_KEY_HERE
```

3. Sync Gradle and run the app.

Notes:
- `local.properties` should not be committed.
- If `FINNHUB_API_KEY` is missing or wrong, requests will fail (often HTTP 401/403) and the app will show an API error.

