# StockExplorer

An Android stock market explorer app built with **Java** and **XML Views**. Search for any US stock symbol, view daily OHLCV data (Open, High, Low, Close, Volume), and quickly identify market trends with visual indicators.

Built as a course project for **CS 5520** by **Group 5**.

## Course / grading notes

- **Group assignment:** one shared app; **MainActivity** shows the group name and a button that opens **StockSearchActivity** (this week’s stock search flow).
- **Web service:** public, free-tier **[Alpha Vantage](https://www.alphavantage.co/)** — real market data (not synthetic). The user **does not** enter the API URL; the app builds the request in code.
- **Networking:** **`HttpURLConnection`** only (no Volley, Retrofit, or other HTTP libraries).
- **Threading:** network work runs on a background **`ExecutorService`**; UI updates on the main thread (**`AsyncTask` is not used**).
- **Challenge-oriented UI:** multiple input controls (EditText, Spinner, Switch), **RecyclerView** for a variable-length result list, and trend styling on each row (not a single raw JSON dump).

## Features

- Search stocks by ticker symbol (e.g. AAPL, TSLA, MSFT)
- View up to 100 days of historical daily price data
- Configurable record count (10 / 25 / 50 / 100)
- Volume filter to show only high-volume trading days (> 1M)
- Trend indicators on each record (green up / red down)
- Clean card-based UI with professional navy and amber theme
- Handles errors gracefully (network issues, invalid symbols, rate limits)
- Landscape and portrait orientation support

## Screenshots

| Main Screen | Search Results |
|:-----------:|:--------------:|
| Dark navy landing page with app icon, title, and "Explore Stocks" button | Stock cards showing symbol, date, OHLCV data, and trend badges |

## Tech Stack

- **Language:** Java
- **UI:** Android XML Views with Material Components
- **API:** [Alpha Vantage](https://www.alphavantage.co/) — TIME_SERIES_DAILY
- **Networking:** HttpURLConnection (no third-party libraries)
- **Architecture:** **MainActivity** → **StockSearchActivity**; **RecyclerView** + custom adapter for results
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34

## Project Structure

```
app/src/main/
├── java/com/example/stockexplorer/
│   ├── MainActivity.java          # Landing page with navigation
│   ├── StockSearchActivity.java   # Search screen, API calls, filtering
│   ├── StockAdapter.java          # RecyclerView adapter with ViewHolder
│   └── StockRecord.java           # Data model (symbol, date, OHLCV)
├── res/
│   ├── layout/
│   │   ├── activity_main.xml          # Landing page layout
│   │   ├── activity_stock_search.xml  # Search screen layout
│   │   └── item_stock_record.xml      # Stock card item layout
│   ├── drawable/
│   │   ├── ic_launcher_foreground.xml # Custom app icon (diamond + $)
│   │   ├── btn_rounded.xml            # Rounded amber button
│   │   └── btn_rounded_outline.xml    # Rounded amber button variant
│   └── values/
│       ├── colors.xml       # Navy/amber color palette
│       ├── strings.xml      # All string resources
│       └── themes.xml       # Material theme configuration
└── AndroidManifest.xml      # Permissions and activity declarations
```

## Setup

### Prerequisites

- Android Studio (Arctic Fox or later)
- JDK 8+
- Android SDK 34

### API Key

This app uses the **Alpha Vantage** API. The **free tier has strict limits** (commonly around **25 requests per day** and **5 requests per minute** — see [Alpha Vantage](https://www.alphavantage.co/support/#support) for current rules).

1. Get a free API key at: https://www.alphavantage.co/support/#api-key
2. Open `local.properties` in the project root and add:

```
ALPHA_VANTAGE_API_KEY=YOUR_KEY_HERE
```

3. Sync Gradle in Android Studio (File > Sync Project with Gradle Files)
4. Build and run

> **Note:** `local.properties` is gitignored and should never be committed. Each developer needs their own key.

### Build

```bash
./gradlew assembleDebug
```

## Usage

1. Launch the app to see the landing page
2. Tap **Explore Stocks** to open the search screen
3. Enter a stock symbol (e.g. `AAPL`)
4. Select how many records to display (10, 25, 50, or 100)
5. Optionally enable the volume filter to show only high-volume days
6. Tap **Search** to fetch and display results
7. Each card shows the date, OHLCV values, and a trend indicator:
   - **▲ Up** (green) — closing price >= opening price
   - **▼ Down** (red) — closing price < opening price

## Team

| Member | Role |
|--------|------|
| Person A | Backend: API integration, data parsing, networking, error handling |
| Person B | Frontend: UI layouts, RecyclerView adapter, themes, app icon, polish |

## Rate Limits

Alpha Vantage free tier is limited to **25 requests/day** and **5 requests/minute**. If you see a rate limit error, wait about a minute before searching again.
