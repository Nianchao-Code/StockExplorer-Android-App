# StockExplorer (Android)

Android Studio **Java** course project (StockExplorer).

## Running the app (Alpha Vantage API key)

This project reads an **Alpha Vantage** API key from your local (uncommitted) `local.properties`.

1. Get a **free** API key from Alpha Vantage:  
   https://www.alphavantage.co/support/#api-key
2. In the project root, add **exactly one line** to `local.properties` (no quotes, no spaces around `=`):

```
ALPHA_VANTAGE_API_KEY=YOUR_KEY_HERE
```

3. Sync Gradle and run the app.

Notes:
- `local.properties` should not be committed.
- Alpha Vantage **free tier** has strict rate limits (often ~5 calls/minute). If you search repeatedly, you may see a rate-limit message—wait ~1 minute and try again.
- If you previously used `FINNHUB_API_KEY`, remove it and use `ALPHA_VANTAGE_API_KEY` instead (this project no longer calls Finnhub).
